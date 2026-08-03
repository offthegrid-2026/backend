package com.event.otg_backend.models;

public enum PaymentStatus {
    CREATED,
    PAID,
    FAILED,
    REFUNDED,        // duplicate payment auto-refunded
    REFUND_FAILED    // duplicate detected but auto-refund failed -> needs manual refund
}
