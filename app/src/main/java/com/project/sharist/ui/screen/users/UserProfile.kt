package com.project.sharist.ui.screen.users

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle


@Composable
fun ProfileScreen(onHistoryClick: () -> Unit, onDetailsClick: () -> Unit,
                  onStatsClick: () -> Unit, onSettingsClick: () -> Unit,
                  onLogoutClick: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile",
            modifier = Modifier.size(120.dp),
            tint = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "John Doe",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "john@gmail.com",
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        BalanceCard()

        Spacer(modifier = Modifier.height(24.dp))

        ProfileMenuItem("View Personal Details") { onDetailsClick() }
        ProfileMenuItem("View History") { onHistoryClick() }
        ProfileMenuItem("View Statistics") { onStatsClick() }
        ProfileMenuItem("Settings") { onSettingsClick() }
        ProfileMenuItem("Logout") { onLogoutClick() }
    }
}
@Composable
fun BalanceCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text("Current Balance")
                Text(
                    text = "$120.00",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Button(onClick = { }) {
                Text("Top Up")
            }
        }
    }
}
@Composable
fun ProfileMenuItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
