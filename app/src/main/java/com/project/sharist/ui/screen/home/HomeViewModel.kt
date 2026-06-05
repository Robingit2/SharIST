package com.project.sharist.ui.screen.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Callback
import okhttp3.Call
import okhttp3.Response
import java.io.IOException
import org.json.JSONObject
import kotlin.collections.copy
import com.project.sharist.BuildConfig


class HomeViewModel : ViewModel() {
    private val _paymentState = MutableStateFlow(PaymentState())
    val paymentState: StateFlow<PaymentState> = _paymentState
    private val client = OkHttpClient()
    private val paypalCreateOrderUrl = "${BuildConfig.SUPABASE_URL}/functions/v1/paypal-create-order"
    private val paypalCaptureOrderUrl = "${BuildConfig.SUPABASE_URL}/functions/v1/capture-paypal-order"
    private val paypalFunctionAuthKey = BuildConfig.PAYPAL_FUNCTION_AUTH_KEY

    fun createPayPalOrder(amount: Double) {
        _paymentState.value = PaymentState()
        val body = JSONObject().apply {
            put("amount", amount)
        }
        val request = Request.Builder()
            .url(paypalCreateOrderUrl)
            .post(
                body.toString()
                    .toRequestBody("application/json".toMediaType())
            )
            .addHeader("apikey", paypalFunctionAuthKey)
            .addHeader("Authorization", "Bearer $paypalFunctionAuthKey")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")
                val orderId = json.getString("id")

                val links = json.getJSONArray("links")

                var approveUrl = ""

                for (i in 0 until links.length()) {
                    val obj = links.getJSONObject(i)
                    if (obj.getString("rel") == "approve") {
                        approveUrl = obj.getString("href")
                    }
                }

                _paymentState.value = PaymentState(
                    orderId = orderId,
                    approvalUrl = approveUrl
                )
            }

            override fun onFailure(call: Call, e: IOException) {
                _paymentState.value = PaymentState(error = e.message)
            }
        })
    }

    /* fun capturePayment(
        orderId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val body = JSONObject().apply {
            put("order_id", orderId)
        }

        android.util.Log.d("PAYPAL", "Capturing: $orderId")

        val request = Request.Builder()
            .url(paypalCaptureOrderUrl)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("apikey", paypalFunctionAuthKey)
            .addHeader("Authorization", "Bearer $paypalFunctionAuthKey")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {

                val json = JSONObject(response.body?.string() ?: "")
                val status = json.optString("status")
                android.util.Log.d("PAYPAL", "Status = $status")
                if (status == "COMPLETED") {
                    onSuccess()
                } else {
                    onError("Not completed: $status")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                onError(e.message ?: "Network error")
            }
        })
    }
}
*/
    fun capturePayment(orderId: String) {
        _paymentState.value = _paymentState.value.copy(
            isProcessing = true,
            error = null
        )

        val body = JSONObject().apply {
            put("order_id", orderId)
        }

        android.util.Log.d("PAYPAL", "Capturing: $orderId")

        val request = Request.Builder()
            .url(paypalCaptureOrderUrl)
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("apikey", paypalFunctionAuthKey)
            .addHeader("Authorization", "Bearer $paypalFunctionAuthKey")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {

                val json = JSONObject(response.body?.string() ?: "")
                val status = json.optString("status")

                android.util.Log.d("PAYPAL", "Status = $status")

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

            override fun onFailure(call: Call, e: IOException) {

                _paymentState.value = _paymentState.value.copy(
                    isProcessing = false,
                    error = e.message ?: "Network error"
                )
            }
        })
    }
    fun clearPaymentResult() {
        _paymentState.value = _paymentState.value.copy(
            isCompleted = false,
            error = null
        )
    }

    fun markReturned() {
        _paymentState.value = _paymentState.value.copy(
            hasReturned = true
        )
    }
}
