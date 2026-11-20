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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
 * Alohida Ro'yxatdan o'tish oynasi (Sign Up / Register)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenLogs: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Parollar mos kelmaganini real vaqtda tekshirish
    val passwordsDoNotMatch = password.isNotEmpty() &&
            confirmPassword.isNotEmpty() &&
            password != confirmPassword

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ro'yxatdan o'tish",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Ortga",
                            tint = Color.White
                        )
                    }
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Avatar Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(ShopPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Register",
                            tint = ShopPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "Yangi hisob yaratish",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextDark
                    )

                    Text(
                        text = "Ma'lumotlaringizni to'ldiring",
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

                    // User name (Foydalanuvchi ismi)
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        label = { Text("Foydalanuvchi ismi (User name)") },
                        placeholder = { Text("Masalan: Alisher") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ShopPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Login Field
                    OutlinedTextField(
                        value = login,
                        onValueChange = {
                            login = it
                            errorMessage = null
                        },
                        label = { Text("Login") },
                        placeholder = { Text("Unikal login kiriting") },
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
                        placeholder = { Text("Yangi parol") },
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

                    // Confirm Password Field (Parolni qayta kiritib tasdiqlash)
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Parolni tasdiqlash") },
                        placeholder = { Text("Parolni qayta kiriting") },
                        leadingIcon = {
                            Icon(Icons.Default.LockReset, contentDescription = null, tint = ShopPrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        isError = passwordsDoNotMatch,
                        supportingText = {
                            if (passwordsDoNotMatch) {
                                Text(
                                    text = "Ogohlantirish: Ikkita parol bir-biriga mos kelmadi!",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Submit Button: "Ro'yxatdan o'tish"
                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            errorMessage = null

                            if (username.isBlank() || login.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                errorMessage = "Barcha maydonlarni to'ldiring!"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (password != confirmPassword) {
                                errorMessage = "Kiritilgan parollar mos kelmadi! Qayta tekshiring."
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            isLoading = true
                            coroutineScope.launch {
                                val result = SupabaseRepository.register(
                                    username = username.trim(),
                                    login = login.trim(),
                                    pass = password
                                )
                                isLoading = false
                                when (result) {
                                    is Result.Success -> {
                                        Toast.makeText(context, result.data, Toast.LENGTH_LONG).show()
                                        onRegisterSuccess()
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
                                text = "Ro'yxatdan o'tish",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Navigation to Login Screen
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Profilingiz bormi? ",
                            fontSize = 14.sp,
                            color = ShopTextMuted
                        )
                        Text(
                            text = "Tizimga kirish",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopPrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onNavigateToLogin() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
