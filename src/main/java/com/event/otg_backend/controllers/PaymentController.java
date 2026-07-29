//package com.event.otg_backend.controllers;
//
//import com.event.otg_backend.services.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/v1/payments")
//@RequiredArgsConstructor
//public class PaymentController {
//
//    private final UserService userService;
//
//    @PostMapping("/webhook")
//    public ResponseEntity<String> handleWebhook(
//            @RequestBody RazorpayWebhookRequest request) {
//
//        // 1. Verify Razorpay webhook signature
//        // 2. Confirm payment is successful
//        // 3. Get the userId associated with the payment
//
//        Long userId = request.getuserId();
//
//        userService.markPaymentAsPaid(userId);
//
//        return ResponseEntity.ok("Payment status updated");
//    }
//}
