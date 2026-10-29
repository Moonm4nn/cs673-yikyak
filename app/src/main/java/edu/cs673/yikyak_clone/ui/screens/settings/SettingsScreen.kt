package edu.cs673.yikyak_clone.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Settings")
                },
                navigationIcon = {
                    Button(onClick = onBackClick) {
                        Text(text = "<")
                    }
                }
            )
        }
    )
    { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply innerPadding here
        ) {

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                Button(onClick = onNavigateToEditProfile) {
                    Text("Edit Account")
                }
                Button(onClick = {
                    Toast.makeText(context, "ToDo", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Report Account")
                }
                Button(onClick = {
                    Toast.makeText(context, "ToDo", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Setting 3")
                }
                Button(
                    colors = ButtonDefaults.buttonColors(Color.Red),
                    onClick = {
                    Toast.makeText(context, "ToDo", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Delete Account")
                }

            }
        }
    }
}