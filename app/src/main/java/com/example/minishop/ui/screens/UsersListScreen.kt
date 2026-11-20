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
import com.example.minishop.data.Result
import com.example.minishop.data.SignupUser
import com.example.minishop.data.SupabaseRepository
import com.example.minishop.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Ro'yxatdan o'tgan foydalanuvchilar ro'yxati (signup jadvali) ekrani
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersListScreen(onBack: () -> Unit) {
    var users by remember { mutableStateOf<List<SignupUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun loadUsers() {
        isLoading = true
        errorMsg = null
        coroutineScope.launch {
            when (val res = SupabaseRepository.fetchRegisteredUsers()) {
                is Result.Success -> {
                    users = res.data
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
        loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Foydalanuvchilar (signup)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Jami: ${users.size} ta foydalanuvchi",
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
                    IconButton(onClick = { loadUsers() }) {
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
                            onClick = { loadUsers() },
                            colors = ButtonDefaults.buttonColors(containerColor = ShopPrimary)
                        ) {
                            Text("Qayta urinish")
                        }
                    }
                }
                users.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Hech qanday foydalanuvchi topilmadi", color = ShopTextMuted)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(users) { user ->
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
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(ShopPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.username.take(1).uppercase().ifEmpty { "?" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = ShopPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.username,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = ShopTextDark
                                        )
                                        Text(
                                            text = "Login: ${user.login}",
                                            fontSize = 13.sp,
                                            color = ShopTextMuted
                                        )
                                        if (user.createdAt.isNotEmpty()) {
                                            Text(
                                                text = "Sana: ${user.createdAt.take(19).replace('T', ' ')}",
                                                fontSize = 11.sp,
                                                color = ShopTextMuted.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    Surface(
                                        color = ShopPrimary.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "#${user.id}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ShopPrimary,
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
