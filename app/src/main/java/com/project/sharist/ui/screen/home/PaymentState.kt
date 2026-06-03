package com.project.sharist.ui.screen.home

data class PaymentState(
    val orderId: String? = null,
    val approvalUrl: String? = null,
    val isProcessing: Boolean = false,
    val hasReturned: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)