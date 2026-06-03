package com.project.sharist.ui.screen.home

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.project.sharist.data.model.user.RoleType
import com.project.sharist.ui.screen.map.OpenStreetMapView
import com.project.sharist.ui.screen.ride_request.RideRequestScreen
import com.project.sharist.ui.screen.weather.WeatherViewModel

import com.project.sharist.ui.screen.favorite.FavoriteViewModel

// Open PayPal
fun openPayPal(context: Context, url: String) {
    CustomTabsIntent.Builder()
        .build()
        .launchUrl(context, Uri.parse(url))
}

@Composable
fun HomeScreen(
    role: RoleType,
    viewModel: HomeViewModel = viewModel(),
    weatherViewModel: WeatherViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel,
    onCreateRideOfferClick: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity

    val paymentState by viewModel.paymentState.collectAsState()

    Box(Modifier.fillMaxSize()) {

        OpenStreetMapView(
            weatherViewModel = weatherViewModel,
            favoriteViewModel = favoriteViewModel
        )

        Button(
            onClick = { viewModel.createPayPalOrder(200.0) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(80.dp)
                .fillMaxWidth()
        ) {
            Text("Pay with PayPal")
        }

        // OPEN PAYPAL
        LaunchedEffect(paymentState.approvalUrl) {
            paymentState.approvalUrl?.let {
                openPayPal(context, it)
            }
        }

        // HANDLE RETURN FROM PAYPAL (IMPORTANT FIX)
        LaunchedEffect(activity.intent?.data) {

            val data = activity.intent?.data
            val orderId = data?.getQueryParameter("token")
            if (data?.scheme == "myapp" &&
                data.host == "paypal-success" &&
                orderId != null
            ) {

                android.util.Log.d("PAYPAL", "Returned orderId = $orderId")
                if (!paymentState.hasReturned) {
                    viewModel.markReturned()
                    viewModel.capturePayment(orderId)
                }
                /*viewModel.capturePayment(
                    orderId = orderId,
                    onSuccess = {
                        android.util.Log.d("PAYPAL", "PAYMENT SUCCESS")
                    },
                    onError = {
                        android.util.Log.e("PAYPAL", it)
                    }
                )*/
            }
        }

        if (role == RoleType.PASSENGER) {
            RideRequestScreen(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }

        if (role == RoleType.DRIVER) {
            Button(
                onClick = onCreateRideOfferClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("Create ride offer")
            }
        }
        if (paymentState.isCompleted) {

            AlertDialog(
                onDismissRequest = {
                    viewModel.clearPaymentResult()
                },

                title = {
                    Text("Payment Successful")
                },

                text = {
                    Text("Your PayPal payment has been completed.")
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            viewModel.clearPaymentResult()
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
        paymentState.error?.let { error ->

            AlertDialog(
                onDismissRequest = {
                    viewModel.clearPaymentResult()
                },

                title = {
                    Text("Payment Failed")
                },

                text = {
                    Text(error)
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            viewModel.clearPaymentResult()
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
        if (paymentState.isProcessing) {

            AlertDialog(
                onDismissRequest = {},

                title = {
                    Text("Processing Payment")
                },

                text = {

                    Column {

                        CircularProgressIndicator()

                        Spacer(
                            Modifier.height(16.dp)
                        )

                        Text(
                            "Please wait while we confirm your payment."
                        )
                    }
                },

                confirmButton = {}
            )
        }
    }
}