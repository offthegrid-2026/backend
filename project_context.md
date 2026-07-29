# OTG Backend — Project Context

## Stack
- Spring Boot 4.1.0, Java 21
- PostgreSQL (primary DB), Redis (OTP storage, JWT logout blocklist) — Redis running via Docker
- Layered architecture: Controller → Service interface → ServiceImpl → Repository
- DTOs + ModelMapper for entity/DTO conversion
- Lombok (`@Getter`, `@Setter`, `@AllArgsConstructor`, `@NoArgsConstructor`, `@Builder`, `@RequiredArgsConstructor`)
- Global exception handling via `@RestControllerAdvice`

## Project purpose
Backend for an event ticketing system. Users register, complete a profile, pay, and receive a ticket with a QR-code-scannable hashcode for entry.

## Package structure (`com.event.otg_backend`)
```
config/        RedisConfig, SecurityConfig, JwtAuthFilter
controllers/    AuthController, UserController, PaymentController (mostly commented out)
dtos/           UserDto, ProfileUpdateDto, OtpRequestDto, OtpVerifyDto, AuthResponseDto, ErrorResponse
exceptions/     GlobalExceptionHandler, ResourceNotFoundException, InvalidTokenException,
                OtpExpiredException, OtpInvalidException, OtpMaxAttemptsExceededException,
                OtpRateLimitExceededException, EmailSendException
helpers/        HashcodeGenerator, QrCodeGenerator, OtpGenerator, OtpHasher, JwtService, SecurityUtil
models/         User, Ticket
repository/     UserRepository (TicketRepository not yet created)
services/       AuthService, UserService, OtpService, EmailService (+ impl/ subpackage)
```

## Entities

**User** (`models/User.java`)
- `id`: Long, Postgres SEQUENCE, `initialValue = 20260`, `allocationSize = 1`
- `email`: unique, nullable = false (the ONLY required field at DB level)
- `firstName`, `lastName`, `phoneNumber`, `city`, `collegeOrOrg`: nullable (filled in profile-completion step, not at registration)
- `paymentStatus`: Boolean, default false
- `createdAt` / `updatedAt`: `@CreationTimestamp` / `@UpdateTimestamp`
- No `role` field yet (was discussed — deferred, not yet implemented)

**Ticket** (`models/Ticket.java`)
- `id`: int, IDENTITY
- `user`: `@OneToOne`, unique FK to User (one ticket per user)
- `price`: Double
- `timestamp`: `@CreationTimestamp`
- `ticketCode`: unique, alphanumeric, length 20 — this is the QR-code payload

## Auth flow (email OTP + JWT, decided and implemented)

**Design decisions made:**
1. Passwordless — email + OTP only, no passwords.
2. OTP required only on first login per session; after verification, a JWT is issued and the user stays logged in for **30 days** until they explicitly log out (not OTP-every-login).
3. Registration and login use the **same endpoints** — backend distinguishes new vs returning user by checking if the email already exists.
4. **Registration flow**: user submits email only → OTP sent → OTP verified → a bare `User` row is created (email only, other fields null) → JWT issued.
5. **Profile completion flow** (separate step, after JWT obtained): frontend calls `GET /api/v1/users/me` to fetch the authenticated user's email (read-only, pre-filled) → user fills in `firstName`, `lastName`, `phoneNumber`, `city`, `collegeOrOrg` → submits via `PATCH /api/v1/users/me`. The email can NEVER be resubmitted/changed here — `ProfileUpdateDto` has no `email` field at all, and the target user is resolved from the JWT (`SecurityUtil.getCurrentUserId()`), never from client input.
6. **OTP storage: Redis, not Postgres** — chosen for native TTL/expiry (no manual cleanup jobs needed), atomic rate-limit counters, and because OTPs are ephemeral verification data, not durable business records.
7. **Logout**: implemented via a Redis blocklist keyed by the JWT's `jti` claim (TTL = token's remaining life), so logout actually invalidates the token immediately rather than being cosmetic client-side-only.
8. OTP: 6-digit numeric, `SecureRandom`, BCrypt-hashed before storing in Redis, 5-minute expiry, max 3 requests per 10-minute window, max 5 wrong-verify attempts before forced re-request.
9. JWT: HS256 via `jjwt` 0.12.6, 30-day expiry, subject = userId, claim = email, `jti` = UUID for revocation tracking.

**Implemented endpoints:**
- `POST /api/v1/auth/request-otp` — `{ email }` → sends OTP, generic response (no user-enumeration leak)
- `POST /api/v1/auth/verify-otp` — `{ email, otp }` → returns `AuthResponseDto { token, userId, email, profileCompleted }`
- `POST /api/v1/auth/logout` — revokes the current JWT via Redis blocklist
- `GET /api/v1/users/me` — returns current user's profile (from JWT)
- `PATCH /api/v1/users/me` — updates profile fields only (no email)

**Security config**: stateless (`SessionCreationPolicy.STATELESS`), CSRF disabled, `/api/v1/auth/**` is `permitAll()`, everything else requires a valid JWT via `JwtAuthFilter` (custom `OncePerRequestFilter`, no Spring Security `UserDetailsService` since there's no password-based login).

## Known gaps / explicitly flagged as NOT yet solved (production concerns)
1. **No role/admin authorization** — `User` entity has no `role` field currently (was discussed earlier — delegate/speaker roles, enum-based, `EnumType.STRING` recommended — but not yet added back to the entity). Right now ANY authenticated user's JWT can call `GET /api/v1/users` (list all users) and `DELETE /api/v1/users/{id}` (delete any user). This must be locked down once roles are reintroduced.
2. **`POST /api/v1/users` (raw createUser) still exists** as a leftover direct-creation endpoint, now sitting behind auth — conceptually redundant/risky now that registration goes through the OTP flow exclusively. Recommended: remove or restrict it.
3. **Payment webhook** (`PaymentController`, currently fully commented out) will need to be `permitAll()` in Spring Security (since Razorpay won't send a JWT) but secured via webhook signature verification instead — not yet implemented.
4. `TicketRepository`, `TicketService`, `TicketController` were designed/discussed earlier (hashcode generation via `SecureRandom`, QR generation via ZXing, seat-limit/early-bird pricing rules) but **not yet created as actual files** — only `HashcodeGenerator` and `QrCodeGenerator` helper classes exist so far.
5. `application-prod.yml` is currently empty — needs real production values (not the dev placeholders for `JWT_SECRET`, `MAIL_PASSWORD`, etc., which must come from real environment variables in production).

## Currently mid-discussion
The user was about to discuss **Step 5 (the DTOs)** created for the auth flow — specifically `OtpRequestDto`, `OtpVerifyDto`, `AuthResponseDto`, `ProfileUpdateDto` — when they paused to ask for this context transfer. Whatever they raise next about these DTOs is the immediate open thread.

## Implementation progress in the user's actual IntelliJ project
The auth flow was built out in this chat as a numbered sequence of steps (pom.xml → application-dev.yml → exceptions → GlobalExceptionHandler → DTOs → helpers → config → services → AuthService/AuthServiceImpl → UserService/UserServiceImpl → AuthController → UserController), and a full working zip with every step applied was generated and shared.

However, **in their actual IntelliJ project, the user has only manually applied/completed up through Step 4 (updating `GlobalExceptionHandler` with the new OTP/token/email exception handlers)**. Steps 5 onward — DTOs (`OtpRequestDto`, `OtpVerifyDto`, `AuthResponseDto`, `ProfileUpdateDto`), helpers (`OtpGenerator`, `OtpHasher`, `JwtService`, `SecurityUtil`), config (`RedisConfig`, `JwtAuthFilter`, `SecurityConfig`), `OtpService`/`EmailService`, the rewritten `AuthService`/`AuthServiceImpl`, the updated `UserService`/`UserServiceImpl`, and the updated `AuthController`/`UserController` — exist in the generated zip but have **not yet been transferred into their real project**.

So: exceptions (Steps 3–4) are done in their live codebase; everything from Step 5 (DTOs) onward is still pending manual integration, starting with the DTO discussion they were about to have.

## Working style notes
- User is building this production project in IntelliJ, pasting code from chat manually (until now — most recent step delivered a full zip with all changes applied directly to their uploaded project).
- User wants thorough explanations before implementation, explicit callouts of edge cases/security gaps, and no hand-waved "it depends" answers.
- Package/file placement should follow their existing conventions exactly (interface in `services/`, impl in `services/impl/`, DTOs in `dtos/`, custom exceptions in `exceptions/`, stateless utilities in `helpers/`).
