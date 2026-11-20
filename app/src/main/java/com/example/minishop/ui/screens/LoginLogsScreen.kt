package com.example.minishop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minishop.data.LoginLog
import com.example.minishop.data.Result
import com.example.minishop.data.SupabaseRepository
import com.example.minishop.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Har bir kirish urinishlari tarixi (login jadvali) ekrani
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginLogsScreen(onBack: () -> Unit) {
    var logs by remember { mutableStateOf<List<LoginLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun loadLogs() {
        isLoading = true
        errorMsg = null
        coroutineScope.launch {
            when (val res = SupabaseRepository.fetchLoginLogs()) {
                is Result.Success -> {
                    logs = res.data
                    isLoading = false
                }
                is Result.Error -> {
                    errorMsg = res.message
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadLogs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Kirishlar tarixi (login)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Jami urinishlar: ${logs.size} ta",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Ortga",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { loadLogs() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yangilash",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShopPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ShopBackground)
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ShopPrimary)
                    }
                }
                errorMsg != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMsg ?: "Xatolik yuz berdi",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { loadLogs() },
                            colors = ButtonDefaults.buttonColors(containerColor = ShopPrimary)
                        ) {
                            Text("Qayta urinish")
                        }
                    }
                }
                logs.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hech qanday kirish logi topilmadi", color = ShopTextMuted)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(logs) { log ->
                            val isSuccess = log.status.equals("SUCCESS", ignoreCase = true)
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = ShopWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ShopCardBorder, RoundedCornerShape(14.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(if (isSuccess) ShopSuccess.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                            contentDescription = null,
                                            tint = if (isSuccess) ShopSuccess else Color.Red,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Login: ${log.login}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = ShopTextDark
                                        )
                                        Text(
                                            text = "Parol: ${log.password}",
                                            fontSize = 12.sp,
                                            color = ShopTextMuted
                                        )
                                        Text(
                                            text = log.status,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (isSuccess) ShopSuccess else Color.Red
                                        )
                                        if (log.createdAt.isNotEmpty()) {
                                            Text(
                                                text = "Vaqt: ${log.createdAt.take(19).replace('T', ' ')}",
                                                fontSize = 11.sp,
                                                color = ShopTextMuted.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    Surface(
                                        color = if (isSuccess) ShopSuccess.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = if (isSuccess) "Muvaffaqiyatli" else "Xato",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSuccess) ShopSuccess else Color.Red,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
