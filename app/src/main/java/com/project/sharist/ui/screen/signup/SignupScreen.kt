package com.project.sharist.ui.screen.signup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts


import androidx.compose.ui.platform.LocalContext
import com.project.sharist.data.local.DatabaseProvider
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.domain.usecase.RegisterUserUseCase
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.Alignment

@Composable
fun SignupScreen(onLoginClick: () -> Unit, onSignupComplete: () -> Unit) {


    val context = LocalContext.current
    val database = remember {
        DatabaseProvider.getDatabase(context)
    }
    val repository = remember {
        UserRepository(database.userDao())
    }
    val registerUserUseCase = remember {
        RegisterUserUseCase(repository)
    }
    val viewModel: SignupViewModel = viewModel(
        factory = SignupViewModelFactory(registerUserUseCase)
    )
    var state by remember { mutableStateOf(SignupState()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        if (state.step == 1) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "How do you want to use the app?",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))
            val options = listOf(
                "Passenger" to "Book rides quickly and safely",
                "Driver" to "Earn money by driving"
            )

            options.forEach { (title, subtitle) ->
                val isSelected = state.userRole == title
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    onClick = {
                        state = state.copy(userRole = title)
                    },
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (isSelected) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        if (state.step == 2) {

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Tell us about yourself",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Full Name
            OutlinedTextField(
                value = state.fullName,
                onValueChange = {
                    state = state.copy(fullName = it)
                },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            // Phone Number
            OutlinedTextField(
                value = state.phone,
                onValueChange = {
                    state = state.copy(phone = it)
                },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.address,
                onValueChange = { state = state.copy(address = it) },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        /*
        if (state.step == 3) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Verify your email.",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = {
                    state = state.copy(fullName = it)
                },
                label = { Text("Enter your email") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    sendOtpToEmail(state.email)
                    state = state.copy(step = 4)
                }
            ) {
                Text("Send OTP")
            }
        }
        if (state.step == 4) {
            Text(text = "Enter OTP sent to your email", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.otp,
                onValueChange = {
                    state = state.copy(otp = it)
                },
                label = { Text("Enter OTP") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    verifyOtp(
                        email = state.email,
                        otp = state.otp,
                        onSuccess = {
                            state = state.copy(
                                isEmailVerified = true,
                                step = 5
                            )
                        },
                        onFailure = {
                            // show error
                        }
                    )
                }
            ) {
                Text("Verify OTP")
            }
        }*/
        if (state.step == 3 && state.userRole == "Driver") {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Vehicle Details",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.vehicleNumber,
                onValueChange = {
                    state = state.copy(vehicleNumber = it)
                },
                label = { Text("Vehicle Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.vehicleModel,
                onValueChange = {
                    state = state.copy(vehicleModel = it)
                },
                label = { Text("Vehicle Model") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.step == 4) {
            OutlinedTextField(
                value = state.email,
                onValueChange = { state = state.copy(email = it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.step == 5) {
            var selectedFileName by remember { mutableStateOf("") }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri: Uri? ->
                uri?.let {
                    selectedFileName = it.lastPathSegment ?: "Document Selected"
                    // Save URI or filename in state
                    state = state.copy(identity_doc = it.toString())
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedFileName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Identity Document") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        launcher.launch(
                            arrayOf(
                                "application/pdf",
                                "image/*"
                            )
                        )
                    }
                ) {
                    Text("Choose Document")
                }
            }
        }

        if (state.step == 6) {
            OutlinedTextField(
                value = state.password,
                onValueChange = { state = state.copy(password = it) },
                label = { Text("Create Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.confirmpassword,
                onValueChange = { state = state.copy(confirmpassword = it) },
                label = { Text("Retype your password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }


        Spacer(modifier = Modifier.height(20.dp))
        Row( modifier = Modifier.fillMaxWidth(),  horizontalArrangement = Arrangement.SpaceBetween) {
            // BACK BUTTON
            if (state.step > 1) {
                OutlinedButton(
                    onClick = {
                        state = when {
                            state.userRole == "Passenger" && state.step == 4 ->
                                state.copy(step = 2)
                            else ->
                                state.copy(step = state.step - 1)
                        }
                    }
                ) {
                    Text("Back")
                }
            }

            // NEXT BUTTON
            if (state.step < 6) {
                Button(
                    onClick = {
                        state = when (state.step) {
                            // STEP 1 -> STEP 2
                            1 -> state.copy(step = 2)

                            // STEP 2
                            2 -> {
                                if (state.userRole == "Driver") {
                                    // Driver goes to vehicle details
                                    state.copy(step = 3)
                                } else {
                                    // Passenger skips step 3
                                    state.copy(step = 4)
                                }
                            }
                            // Driver vehicle details -> step 4
                            3 -> state.copy(step = 4)
                            // Continue remaining steps
                            4 -> state.copy(step = 5)
                            5 -> state.copy(step = 6)
                            else -> state
                        }
                    }
                ) {
                    Text("Next")
                }
            } else {
                // FINAL BUTTON
                Button(
                    onClick = {
                        if (state.password == state.confirmpassword) {
                            viewModel.registerUser(
                                state = state,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Account Created Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Navigate to Login
                                    onLoginClick()
                                }
                            )

                        } else {
                            Toast.makeText(
                                context,
                                "Passwords do not match",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text("Create Account")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = onLoginClick) {
            Text("Already have account? Login")
        }
    }
}