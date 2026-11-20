package com.example.minishop.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minishop.ui.theme.*

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val category: String,
    val iconEmoji: String
)

/**
 * Tizimga kirgandan so'ng ochiladigan Asosiy MiniShop ekrani
 */
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
                        Text(userLogin, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShopPrimary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                actions = {
                    // Foydalanuvchilar ro'yxati (signup jadvali)
                    IconButton(onClick = onOpenUsers) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Foydalanuvchilar"
                        )
                    }

                    // Kirishlar tarixi (login jadvali)
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
                            contentDescription = "Chiqish",
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

            // 2-ustunli mahsulotlar tarmog'i
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
