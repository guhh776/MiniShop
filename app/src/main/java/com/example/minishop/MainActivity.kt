package com.example.minishop

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.example.minishop.data.LoginLog
import com.example.minishop.data.Result
import com.example.minishop.data.SignupUser
import com.example.minishop.data.SupabaseRepository
import com.example.minishop.ui.theme.*
import kotlinx.coroutines.launch

// Mock Product model
data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val category: String,
    val iconEmoji: String
)

enum class AppScreen {
    AUTH,
    SHOP,
    USERS_LIST,
    LOGIN_LOGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiniShopTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.AUTH) }
    var previousScreen by remember { mutableStateOf(AppScreen.AUTH) }
    var loggedInUserLogin by remember { mutableStateOf("") }
    var cartCount by remember { mutableStateOf(0) }

    fun navigateTo(screen: AppScreen) {
        if (screen == AppScreen.USERS_LIST || screen == AppScreen.LOGIN_LOGS) {
            previousScreen = currentScreen
        }
        currentScreen = screen
    }

    fun navigateBack() {
        currentScreen = previousScreen
    }

    BackHandler(enabled = currentScreen == AppScreen.USERS_LIST || currentScreen == AppScreen.LOGIN_LOGS) {
        navigateBack()
    }

    when (currentScreen) {
        AppScreen.AUTH -> {
            AuthScreen(
                onLoginSuccess = { login ->
                    loggedInUserLogin = login
                    currentScreen = AppScreen.SHOP
                },
                onOpenUsers = { navigateTo(AppScreen.USERS_LIST) },
                onOpenLogs = { navigateTo(AppScreen.LOGIN_LOGS) }
            )
        }
        AppScreen.SHOP -> {
            ShopHomeScreen(
                userLogin = loggedInUserLogin,
                cartCount = cartCount,
                onAddToCart = { cartCount++ },
                onLogout = {
                    loggedInUserLogin = ""
                    cartCount = 0
                    currentScreen = AppScreen.AUTH
                },
                onOpenUsers = { navigateTo(AppScreen.USERS_LIST) },
                onOpenLogs = { navigateTo(AppScreen.LOGIN_LOGS) }
            )
        }
        AppScreen.USERS_LIST -> {
            UsersListScreen(
                onBack = { navigateBack() }
            )
        }
        AppScreen.LOGIN_LOGS -> {
            LoginLogsScreen(
                onBack = { navigateBack() }
            )
        }
    }
}

// -------------------------------------------------------------------------
// 1. AUTH SCREEN (Login & Register)
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: (String) -> Unit,
    onOpenUsers: () -> Unit,
    onOpenLogs: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // App Logo / Icon
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(ShopPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Logo",
                            tint = ShopPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = if (isLoginMode) "Tizimga kirish" else "Ro'yxatdan o'tish",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShopTextDark
                    )

                    Text(
                        text = if (isLoginMode) "Hisobingizga kiring" else "Yangi hisob yarating",
                        fontSize = 13.sp,
                        color = ShopTextMuted
                    )

                    // Error Message Banner
                    errorMessage?.let { error ->
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Registration Name Field
                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                errorMessage = null
                            },
                            label = { Text("Foydalanuvchi ismi (User name)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Login Field
                    OutlinedTextField(
                        value = login,
                        onValueChange = {
                            login = it
                            errorMessage = null
                        },
                        label = { Text("Login") },
                        leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
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
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
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

                    // Confirm Password Field (Only in Register mode)
                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                errorMessage = null
                            },
                            label = { Text("Parolni tasdiqlash") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Submit Button
                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            errorMessage = null

                            if (isLoginMode) {
                                // Login validation
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
                            } else {
                                // Register validation
                                if (username.isBlank() || login.isBlank() || password.isBlank()) {
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
                                            isLoginMode = true
                                            confirmPassword = ""
                                        }
                                        is Result.Error -> {
                                            errorMessage = result.message
                                            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                        }
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
                                text = if (isLoginMode) "Tizimga kirish" else "Ro'yxatdan o'tish",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Switch between Login and Register
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLoginMode) "Hisobingiz yo'qmi? " else "Profilingiz bormi? ",
                            fontSize = 13.sp,
                            color = ShopTextMuted
                        )
                        Text(
                            text = if (isLoginMode) "Ro'yxatdan o'tish" else "Kirish",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ShopPrimary,
                            modifier = Modifier.clickable {
                                isLoginMode = !isLoginMode
                                errorMessage = null
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------
// 2. USERS LIST SCREEN (signup jadvali)
// -------------------------------------------------------------------------
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
                        Text("Foydalanuvchilar (signup)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Jami: ${users.size}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
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
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(users) { user ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = ShopWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ShopCardBorder, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
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
                                                text = "Vaqt: ${user.createdAt.take(19).replace('T', ' ')}",
                                                fontSize = 11.sp,
                                                color = ShopTextMuted.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "#${user.id}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ShopTextMuted
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

// -------------------------------------------------------------------------
// 3. LOGIN LOGS SCREEN (login jadvali)
// -------------------------------------------------------------------------
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
                        Text("Kirishlar tarixi (login)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Jami urinishlar: ${logs.size}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
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
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(logs) { log ->
                            val isSuccess = log.status.equals("SUCCESS", ignoreCase = true)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = ShopWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, ShopCardBorder, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
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
                                            text = "Status: ${log.status}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = if (isSuccess) ShopSuccess else Color.Red
                                        )
                                        if (log.createdAt.isNotEmpty()) {
                                            Text(
                                                text = "Vaqt: ${log.createdAt.take(19).replace('T', ' ')}",
                                                fontSize = 11.sp,
                                                color = ShopTextMuted
                                            )
                                        }
                                    }
                                    Text(
                                        text = "#${log.id}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ShopTextMuted
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

// -------------------------------------------------------------------------
// 4. MARKETPLACE SHOP SCREEN
// -------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopHomeScreen(
    userLogin: String,
    cartCount: Int,
    onAddToCart: () -> Unit,
    onLogout: () -> Unit,
    onOpenUsers: () -> Unit,
    onOpenLogs: () -> Unit
) {
    val sampleProducts = remember {
        listOf(
            Product(1, "Wireless Headphones", 59.99, "Electronics", "🎧"),
            Product(2, "Running Sneakers", 84.50, "Fashion", "👟"),
            Product(3, "Smart Watch Pro", 129.00, "Electronics", "⌚"),
            Product(4, "Denim Jacket", 49.99, "Fashion", "🧥"),
            Product(5, "Stainless Bottle", 19.99, "Fitness", "🍶"),
            Product(6, "Classic Backpack", 39.50, "Accessories", "🎒")
        )
    }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mini Marketplace", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(userLogin, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShopPrimary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // Users list
                    IconButton(onClick = onOpenUsers) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Foydalanuvchilar"
                        )
                    }

                    // Logs
                    IconButton(onClick = onOpenLogs) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Kirishlar tarixi"
                        )
                    }

                    // Cart Icon with badge
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(containerColor = ShopSuccess) {
                                    Text("$cartCount", color = Color.White)
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color.White
                        )
                    }

                    // Logout Icon
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ShopBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Featured Products",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ShopTextDark
            )
            Text(
                text = "Explore our top picks for you",
                fontSize = 12.sp,
                color = ShopTextMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2-Column Product Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sampleProducts) { product ->
                    ProductGridCard(
                        product = product,
                        onAdd = {
                            onAddToCart()
                            Toast.makeText(context, "${product.name} savatchaga qo'shildi!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    onAdd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ShopWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ShopCardBorder, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Product Emoji Avatar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ShopBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(text = product.iconEmoji, fontSize = 42.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.category.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ShopPrimary
            )

            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ShopTextDark,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${product.price}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShopTextDark
                )

                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ShopPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
