package com.project.sharist.ui.screen.signup

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.sharist.data.model.user.RoleType
import com.project.sharist.data.repository.UserRepository
import com.project.sharist.data.usecase.auth.RegisterUserUseCase

@Composable
fun SignupScreen(
    onLoginClick: () -> Unit,
    onSignupComplete: () -> Unit
) {
    val context = LocalContext.current
    val registerUserUseCase = remember {
        RegisterUserUseCase(UserRepository())
    }
    val viewModel: SignupViewModel = viewModel(
        factory = SignupViewModelFactory(registerUserUseCase)
    )

    var state by remember { mutableStateOf(SignupState()) }
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            state = state.copy(photoPath = uri.toString())
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
        Text("Sign up", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { state = state.copy(name = it) },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { state = state.copy(email = it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { state = state.copy(password = it) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { photoPicker.launch(arrayOf("image/*")) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (state.photoPath.isBlank()) "Choose photo" else "Change photo")
        }

        if (state.photoPath.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Photo selected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Roles", style = MaterialTheme.typography.titleMedium)

        RoleType.entries.forEach { role ->
            val roleName = role.name.lowercase()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = roleName in state.roles,
                    onCheckedChange = { checked ->
                        state = state.copy(
                            roles = if (checked) {
                                state.roles + roleName
                            } else {
                                state.roles - roleName
                            }
                        )
                    },
                    enabled = !isLoading
                )
                Text(roleName.replaceFirstChar { it.titlecase() })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.registerUser(
                    context = context,
                    state = state,
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Account created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        onSignupComplete()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Create account")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onLoginClick,
            enabled = !isLoading
        ) {
            Text("Already have an account? Login")
        }
    }
}
