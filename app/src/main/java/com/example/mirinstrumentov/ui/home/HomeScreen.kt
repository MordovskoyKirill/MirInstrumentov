package com.example.mirinstrumentov.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mirinstrumentov.data.model.Tool
import com.example.mirinstrumentov.ui.catalog.*
import com.example.mirinstrumentov.ui.components.FigmaIcon
import kotlinx.coroutines.launch

enum class ShopTab { CATALOG, FAVORITES, CART, ORDERS, PROFILE }

@Composable
fun HomeScreen(email: String, isLoading: Boolean, message: String?, onLogout: () -> Unit) {
    val model: CatalogViewModel = viewModel(key = "catalog_$email")
    val data by model.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(ShopTab.CATALOG) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var toolId by rememberSaveable { mutableStateOf<Long?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(data.notice) {
        val notice = data.notice ?: return@LaunchedEffect
        model.clearNotice()
        snackbarHostState.showSnackbar(notice)
    }

    Scaffold(
        containerColor = CatalogBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ShopBottomBar(current = tab) { next ->
                tab = next
                category = null
                toolId = null
                query = ""
            }
        }
    ) { insets ->
        Box(Modifier.fillMaxSize().padding(insets)) {
            when {
                data.loading && data.tools.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = CatalogBlue
                    )
                }
                data.error != null && data.tools.isEmpty() -> {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Ошибка загрузки: ${data.error}", textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = model::refresh) { Text("Повторить") }
                    }
                }
                toolId != null -> {
                    val tool = data.tools.firstOrNull { it.id == toolId }
                    if (tool == null) {
                        CenteredText("Инструмент не найден")
                    } else {
                        ToolDetailScreen(
                            tool = tool,
                            isFavorite = tool.id in data.favorites,
                            isBusy = tool.id in data.busyToolIds,
                            onBack = { toolId = null },
                            onFavorite = { model.toggleFavorite(tool) },
                            onCart = { count -> model.addToCart(tool, count) }
                        )
                    }
                }
                tab == ShopTab.CATALOG && category != null -> {
                    val categoryName = category.orEmpty()
                    val categoryId = data.categories.firstOrNull { it.name == categoryName }?.id
                    CategoryToolsScreen(
                        title = categoryName,
                        tools = data.tools.filter { it.categoryId == categoryId },
                        favorites = data.favorites,
                        busy = data.busyToolIds,
                        onBack = { category = null },
                        onTool = { toolId = it.id },
                        onFavorite = model::toggleFavorite,
                        onCart = { model.addToCart(it) }
                    )
                }
                tab == ShopTab.CATALOG -> {
                    CategoriesScreen(
                        query = query,
                        onQueryChange = { query = it },
                        categories = data.categories,
                        allTools = data.tools,
                        favorites = data.favorites,
                        busy = data.busyToolIds,
                        onCategory = { category = it },
                        onTool = { toolId = it.id },
                        onFavorite = model::toggleFavorite,
                        onCart = { model.addToCart(it) }
                    )
                }
                tab == ShopTab.FAVORITES -> {
                    val favoriteTools = data.tools.filter { it.id in data.favorites }
                    if (favoriteTools.isEmpty()) {
                        EmptyScreen("В избранном пока ничего нет") { tab = ShopTab.CATALOG }
                    } else {
                        CategoryToolsScreen(
                            title = "Избранное",
                            tools = favoriteTools,
                            favorites = data.favorites,
                            busy = data.busyToolIds,
                            onBack = { tab = ShopTab.CATALOG },
                            onTool = { toolId = it.id },
                            onFavorite = model::toggleFavorite,
                            onCart = { model.addToCart(it) }
                        )
                    }
                }
                tab == ShopTab.CART -> CartContent(
                    state = data,
                    onChange = model::changeQuantity,
                    onCatalog = { tab = ShopTab.CATALOG }
                )
                tab == ShopTab.ORDERS -> {
                    Column(Modifier.fillMaxSize()) {
                        ScreenHeading("Заказы")
                    }
                }
                tab == ShopTab.PROFILE -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Профиль", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(24.dp))
                        Text(email, color = CatalogGray)
                        if (message != null) {
                            Spacer(Modifier.height(12.dp))
                            Text(message, color = Color.Red)
                        }
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onLogout,
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = CatalogBlue)
                        ) { Text("Выйти") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopBottomBar(current: ShopTab, onChange: (ShopTab) -> Unit) {
    val items = listOf(
        ShopTab.CATALOG to "Каталог",
        ShopTab.FAVORITES to "Избранное",
        ShopTab.CART to "Корзина",
        ShopTab.ORDERS to "Заказы",
        ShopTab.PROFILE to "Профиль"
    )
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        items.forEach { (tab, title) ->
            val selected = current == tab
            val icon = when (tab) {
                ShopTab.CATALOG -> "ic_catalog"
                ShopTab.FAVORITES -> "ic_favorite"
                ShopTab.CART -> "ic_cart"
                ShopTab.ORDERS -> "ic_orders"
                ShopTab.PROFILE -> "ic_profile"
            }
            val fallback = when (tab) {
                ShopTab.CATALOG -> "⌂"
                ShopTab.FAVORITES -> if (selected) "♥" else "♡"
                ShopTab.CART -> "▢"
                ShopTab.ORDERS -> "▤"
                ShopTab.PROFILE -> "○"
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onChange(tab) },
                icon = {
                    FigmaIcon(
                        name = icon,
                        contentDescription = title,
                        modifier = Modifier.size(28.dp),
                        tint = if (selected) CatalogBlue else CatalogGray,
                        fallback = fallback
                    )
                },
                label = { Text(title, maxLines = 1, fontSize = 10.sp) },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CatalogBlue,
                    selectedTextColor = CatalogBlue,
                    unselectedIconColor = CatalogGray,
                    unselectedTextColor = CatalogGray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
