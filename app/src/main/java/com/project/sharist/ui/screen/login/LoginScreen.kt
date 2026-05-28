package com.project.sharist.ui.screen.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.data.usecase.auth.LoginUserUseCase

@Composable
fun LoginScreen(
    onSignupClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val loginUserUseCase = remember {
        LoginUserUseCase(UserRepository())
    }
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(loginUserUseCase)
    )

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Login", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.login(email, password)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                onSignupClick()
            },
            enabled = !isLoading
        ) {
            Text("Don't have an account? Sign up")
        }
    }
}
