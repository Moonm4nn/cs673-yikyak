package edu.cs673.yikyak_clone.ui.screens.createpost

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import edu.cs673.yikyak_clone.data.getCurrentLocation
import edu.cs673.yikyak_clone.data.repository.SupabasePostRepository
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import edu.cs673.yikyak_clone.ui.components.ChannelImage

private const val MAX_POST_LENGTH = 200

@Composable
fun CreatePostScreen(
    modifier: Modifier = Modifier,
    channelId: String? = null,
    channelName: String? = null,
    onBackClick: () -> Unit = {},
    onPostSuccess: () -> Unit = {}
) {
    val repository = remember { SupabasePostRepository() }
    val viewModel = remember (channelId) { CreatePostViewModel(repository, channelId) }
    val uiState = viewModel.uiState
    val context = LocalContext.current
    val postTypeExpanded = remember { mutableStateOf(false) }
    val postTypeOptions = listOf("Post", "Event")
    val postTypeSelectedOption = remember { mutableIntStateOf(0) }
    val displayName = if (channelId == null) "Nearby" else (channelName ?: "Channel")


    LaunchedEffect(uiState.postCreated) {
        if (uiState.postCreated) {
            onPostSuccess()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    IconButton(onClick = onBackClick) {
                        Text("←")
                    }
                    // Start
                    Box (modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                    ) {
                        // Button which creates dropdown menu
                        Button(onClick = { postTypeExpanded.value = true }) {
                            Text("New " + postTypeOptions[postTypeSelectedOption.intValue] + " ↓")
                        }
                        // Dropdown menu which responds to button click
                        DropdownMenu(
                            expanded = postTypeExpanded.value,
                            onDismissRequest = { postTypeExpanded.value = false }
                        ) {
                            // Create dropdown menu items for each option
                            postTypeOptions.forEachIndexed { index, option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        postTypeSelectedOption.intValue = index
                                        postTypeExpanded.value = false
                                    }
                                )
                            }
                        }
                    }
                    // End
                    // Start
                    Text(
                        text = "New Post",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                    ChannelImage(displayName)
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp)
                    )
                    // End
                    Button(
                        onClick = {
                            getCurrentLocation(context) { lat, lon ->
                                viewModel.onUiAction(CreatePostUiAction.UpdateLocation(lat, lon))
                                viewModel.onUiAction(CreatePostUiAction.CreatePost())
                            }
                        },
                            enabled = uiState.postContent.isNotBlank() &&
                                    uiState.postContent.length <= MAX_POST_LENGTH &&
                                    !uiState.isLoading,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Post")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.postContent,
                onValueChange = { newText ->
                    if (newText.length <= MAX_POST_LENGTH) {
                        viewModel.onUiAction(
                            CreatePostUiAction.UpdatePostContent(newText)
                        )
                    }
                },
                placeholder = { Text("What's on your mind?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            // Only appears if post type is event
            if (postTypeSelectedOption.intValue == 1) {
                TimeScreen()
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "${uiState.postContent.length} / $MAX_POST_LENGTH",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uiState.postContent.length >= MAX_POST_LENGTH) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreatePostScreenPreview() {
    CreatePostScreen(
        onPostSuccess = {},
        onBackClick = {}
    )
}

// Function to show buttons to pick event start and end times
@Composable
fun TimeScreen() {
    // State variables to hold selected date and time and to show/hide the pickers
    val showStartDateTimeSelector = remember { mutableStateOf(false) }
    val selectedStartDateTime = remember { mutableStateOf("Select Start Date & Time") }
    val showEndDateTimeSelector = remember { mutableStateOf(false) }
    val selectedEndDateTime = remember { mutableStateOf("Select End Date & Time") }

    // Display buttons to pick start and end times
    Column {
        Button(onClick = { showStartDateTimeSelector.value = true }) {
            Text(text = selectedStartDateTime.value)
        }
        Button(onClick = { showEndDateTimeSelector.value = true }) {
            Text(text = selectedEndDateTime.value)
        }
    }

    // Show the date and time pickers for the start date time
    if (showStartDateTimeSelector.value) {
        DateTimeSelector(
            onDismiss = { showStartDateTimeSelector.value = false },
            // TODO
            onDateTimeSelected = { dateMillis, hour, minute ->
                showStartDateTimeSelector.value = false
                selectedStartDateTime.value = "Start: ${formatDateTime(dateMillis, hour, minute)}"
            }
        )
    }

    // Show the date and time pickers for the end date time
    if (showEndDateTimeSelector.value) {
        DateTimeSelector(
            onDismiss = { showEndDateTimeSelector.value = false },
            // TODO
            onDateTimeSelected = { dateMillis, hour, minute ->
                showEndDateTimeSelector.value = false
                selectedEndDateTime.value = "End: ${formatDateTime(dateMillis, hour, minute)}"
            }
        )
    }
}

// Function to show a date and time picker
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeSelector(
    onDismiss: () -> Unit,
    onDateTimeSelected: (Long, Int, Int) -> Unit
) {
    // Get the current time for the time picker
    val currentTime = Calendar.getInstance()

    // State variables to show/hide the date and time pickers
    val showDatePicker = remember { mutableStateOf(true) }
    val showTimePicker = remember { mutableStateOf(false) }

    // Initialize the date and time pickers
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE)
    )

    // Conditionally show the date picker first
    if (showDatePicker.value) {
        // Show the date picker in a date picker dialog
        DatePickerDialog(
            // Hide date picker and return from date time selector
            onDismissRequest = {
                showDatePicker.value = false
                onDismiss()
            },
            // Hide date picker and proceed to time picker
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker.value = false
                    showTimePicker.value = true
                }) { Text("Select Date") }
            }
        ) {
            // Display the date picker
            DatePicker(state = datePickerState)
        }
    }

    // Conditionally show the time picker
    if (showTimePicker.value) {
        // Show the time picker in a time picker dialog
        TimePickerDialog(
            // Hide time picker and return from date time selector
            onDismissRequest = {
                showTimePicker.value = false
                onDismiss()
            },
            // Hide time picker and return from date time selector with selected date and time
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker.value = false
                    // If date is not null, assign date as millis (in midnight UTC)
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Return selected date and time
                        onDateTimeSelected(millis, timePickerState.hour, timePickerState.minute)
                    }
                }) { Text("Select Time") }
            },
            // Title required, leave it as null for default
            title = {  },
            // Display the time picker
        ) {TimePicker(state = timePickerState) }
    }
}

// Function to format date and time to an output text
fun formatDateTime(dateMillis: Long, hour: Int, minute: Int): String {
    // Create a calendar instance in UTC time zone
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        // Set the time to the specified date and time
        timeInMillis = dateMillis
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }

    // Format the date and time according to the pattern and locale language
    val formatter = SimpleDateFormat(
        "MMM dd, yyyy 'at' h:mm a",
        Locale.getDefault()
    // Set the time zone to UTC
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // Return the formatted date and time
    return formatter.format(calendar.time)
}