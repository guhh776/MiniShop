package com.example.minishop.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minishop.data.Result
import com.example.minishop.data.SupabaseRepository
import com.example.minishop.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Alohida Login oynasi (Sign In)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenLogs: () -> Unit
) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MiniShop",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShopPrimary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onOpenUsers) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Foydalanuvchilar ro'yxati"
                        )
                    }
                    IconButton(onClick = onOpenLogs) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Kirishlar tarixi"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ShopBackground)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ShopWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(ShopPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Logo",
                            tint = ShopPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Tizimga kirish",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextDark
                    )

                    Text(
                        text = "Login va parolingizni kiriting",
                        fontSize = 13.sp,
                        color = ShopTextMuted
                    )

                    // Error Message Banner
                    errorMessage?.let { error ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Login Field
                    OutlinedTextField(
                        value = login,
                        onValueChange = {
                            login = it
                            errorMessage = null
                        },
                        label = { Text("Login") },
                        placeholder = { Text("Loginingizni kiriting") },
                        leadingIcon = {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = ShopPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Parol") },
                        placeholder = { Text("Parolingizni kiriting") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ShopPrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Submit Button: "Tizimga kirish"
                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            errorMessage = null

                            if (login.isBlank() || password.isBlank()) {
                                errorMessage = "Login va parolni kiriting!"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true
                            coroutineScope.launch {
                                val result = SupabaseRepository.login(login.trim(), password)
                                isLoading = false
                                when (result) {
                                    is Result.Success -> {
                                        Toast.makeText(context, "Tizimga muvaffaqiyatli kirildi!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(login.trim())
                                    }
                                    is Result.Error -> {
                                        errorMessage = result.message
                                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShopPrimary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Tizimga kirish",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Navigation to Register Screen
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Hisobingiz yo'qmi? ",
                            fontSize = 14.sp,
                            color = ShopTextMuted
                        )
                        Text(
                            text = "Ro'yxatdan o'tish",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onNavigateToRegister() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
