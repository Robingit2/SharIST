package com.project.sharist.ui.screen.payment

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import android.util.Log
import com.project.sharist.ui.screen.home.PaymentState

class PayPalController : ViewModel() {

    private val scope = MainScope()
    private val _paymentState = MutableStateFlow(PaymentState())
    val paymentState: StateFlow<PaymentState> = _paymentState
    private val client = OkHttpClient()

    //  prevent duplicate order calls
    private var isCreatingOrder = false
    private var lastOrderId: String? = null

    fun createPayPalOrder(amount: Double) {
        Log.d("PAY_DEBUG", "HTTP Code:")
        if (isCreatingOrder) return
        isCreatingOrder = true

        _paymentState.value = PaymentState(isProcessing = true)

        val body = JSONObject().apply {
            put("amount", amount)
        }

        val request = Request.Builder()
            .url("https://mntteedoykjrrxeklhlv.supabase.co/functions/v1/paypal-create-order")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("apikey", "sb_publishable_2BjsdexFlrJgN9gOQfiubg_N7vJLzP9")
            .addHeader("Authorization", "Bearer sb_publishable_2BjsdexFlrJgN9gOQfiubg_N7vJLzP9")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {

                isCreatingOrder = false

                val responseBody = response.body?.string()

                Log.d("PAY_DEBUG", "HTTP Code = ${response.code}")
                Log.d("PAY_DEBUG", "Response Body = $responseBody")

                if (!response.isSuccessful) {
                    scope.launch(Dispatchers.Main) {
                        _paymentState.value = PaymentState(
                            error = "HTTP ${response.code}",
                            isProcessing = false
                        )
                    }
                    return
                }

                val json = JSONObject(responseBody ?: "{}")

                val orderId = json.optString("id")
                val links = json.optJSONArray("links")

                var approveUrl = ""

                if (links != null) {
                    for (i in 0 until links.length()) {
                        val obj = links.getJSONObject(i)
                        if (obj.optString("rel") == "approve") {
                            approveUrl = obj.optString("href")
                        }
                    }
                }

                Log.d("PAY_DEBUG", "Approve URL = $approveUrl")

                scope.launch(Dispatchers.Main) {
                    _paymentState.value = PaymentState(
                        orderId = orderId,
                        approvalUrl = approveUrl,
                        isProcessing = false
                    )
                }
            }

            override fun onFailure(call: Call, e: IOException) {

                isCreatingOrder = false

                scope.launch(Dispatchers.Main) {
                    _paymentState.value = PaymentState(
                        error = e.message,
                        isProcessing = false
                    )
                }
            }
        })
    }

    fun capturePayment(orderId: String) {

        if (lastOrderId == orderId && _paymentState.value.isCompleted) return
        _paymentState.value = _paymentState.value.copy(
            isProcessing = true,
            error = null
        )

        val body = JSONObject().apply {
            put("order_id", orderId)
        }

        val request = Request.Builder()
            .url("https://mntteedoykjrrxeklhlv.supabase.co/functions/v1/capture-paypal-order")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("apikey", "sb_publishable_2BjsdexFlrJgN9gOQfiubg_N7vJLzP9")
            .addHeader("Authorization", "Bearer sb_publishable_2BjsdexFlrJgN9gOQfiubg_N7vJLzP9")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {

                val json = JSONObject(response.body?.string() ?: "{}")
                val status = json.optString("status")

                scope.launch(Dispatchers.Main) {

                    if (status == "COMPLETED") {

                        _paymentState.value = _paymentState.value.copy(
                            isProcessing = false,
                            isCompleted = true,
                            hasReturned = true,
                            error = null
                        )

                    } else {

                        _paymentState.value = _paymentState.value.copy(
                            isProcessing = false,
                            error = "Not completed: $status"
                        )
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                scope.launch(Dispatchers.Main) {
                    _paymentState.value = _paymentState.value.copy(
                        isProcessing = false,
                        error = e.message ?: "Network error"
                    )
                }
            }
        })
    }
    fun markReturned() {
        _paymentState.value = _paymentState.value.copy(
            hasReturned = true
        )
    }
    fun clearPaymentResult() {
        _paymentState.value = PaymentState()
    }
    fun consumeApprovalUrl() {
        _paymentState.value = _paymentState.value.copy(
            approvalUrl = "" // Or null, depending on PaymentState
        )
    }
}