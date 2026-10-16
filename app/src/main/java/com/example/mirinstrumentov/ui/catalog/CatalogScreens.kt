package com.example.mirinstrumentov.ui.catalog

import android.content.Context
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.SvgDecoder
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mirinstrumentov.data.model.CartLine
import com.example.mirinstrumentov.data.model.Tool
import com.example.mirinstrumentov.data.model.ToolCategory
import com.example.mirinstrumentov.data.remote.StorageImages
import com.example.mirinstrumentov.ui.components.FigmaIcon
import java.text.NumberFormat
import java.util.Locale

val CatalogBlue = Color(0xFF1875EB)
val CatalogBackground = Color(0xFFF5F5F5)
val CatalogGray = Color(0xFF64708A)
val CatalogGreen = Color(0xFF00C925)
val CatalogRed = Color(0xFFF00000)

val categoryDesign = listOf(
    "Ручные инструменты" to "cat_manual",
    "Электроинструменты" to "cat_electric",
    "Измерительные инструменты" to "cat_measuring",
    "Крепёж" to "cat_fasteners",
    "Монтажные инструменты" to "cat_installation",
    "Режущие инструменты" to "cat_cutting"
)

fun rubles(price: Double): String =
    NumberFormat.getIntegerInstance(Locale("ru", "RU")).format(price) + " ₽"

private fun productDrawableName(name: String): String = when {
    "GSB 550" in name -> "tool_bosch_gsb550"
    "DCD771" in name -> "tool_dewalt_dcd771"
    "Спирит-3" in name -> "tool_hipro_spirit3"
    "GA4534" in name -> "tool_makita_ga4534"
    "Stanley" in name -> "tool_stanley_5m"
    else -> "tool_default"
}

@Suppress("DEPRECATION")
private fun drawableId(context: Context, fileName: String): Int =
    context.resources.getIdentifier(fileName, "drawable", context.packageName)

@Composable
fun CatalogAsset(
    fileName: String,
    description: String,
    modifier: Modifier = Modifier
) {

    Box (modifier = modifier, contentAlignment = Alignment.Center)
    {
        Text(
            text = "нет изобр",
            color = CatalogGray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
    /*
    val context = LocalContext.current
    val id = remember(context, fileName) { drawableId(context, fileName) }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (id != 0) {
            Image(
                painter = painterResource(id),
                contentDescription = description,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(5.dp)
            )
        } else {
            FigmaIcon(
                name = "ic_tool",
                contentDescription = description,
                tint = CatalogGray.copy(alpha = .45f),
                modifier = Modifier.size(52.dp),
                fallback = "▢"
            )
        }
    }
    */
}

// image_url хранит путь файла внутри Supabase Storage, например "folder/hammer.svg".
// Полная публичная ссылка формируется через Supabase Storage.
@Composable
fun CatalogRemoteAsset(
    imageUrl: String?,
    bucket: String,
    fallbackFileName: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val publicUrl = remember(imageUrl, bucket) {
        StorageImages.publicUrl(imageUrl, bucket)
    }
    if (publicUrl == null) {
        CatalogAsset(fallbackFileName, description, modifier)
        return
    }

    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    SubcomposeAsyncImage(
        model = publicUrl,
        imageLoader = imageLoader,
        contentDescription = description,
        modifier = modifier.padding(5.dp),
        contentScale = ContentScale.Fit,
        loading = {
            FigmaIcon(
                name = "ic_tool",
                contentDescription = null,
                tint = CatalogGray.copy(alpha = 0.25f),
                modifier = Modifier.size(48.dp),
                fallback = "▢"
            )
        },
        error = {
            CatalogAsset(fallbackFileName, description, Modifier.fillMaxSize())
        }
    )
}

@Composable
fun CategoriesScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    categories: List<ToolCategory>,
    allTools: List<Tool>,
    favorites: Set<Long>,
    busy: Set<Long>,
    onCategory: (String) -> Unit,
    onTool: (Tool) -> Unit,
    onFavorite: (Tool) -> Unit,
    onCart: (Tool) -> Unit
) {
    Column(Modifier.fillMaxSize().background(CatalogBackground)) {
        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)) {
            Text("Мир инструментов", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Все для работы и ремонтов", fontSize = 14.sp, color = CatalogGray)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Поиск инструментов", fontSize = 15.sp, color = CatalogGray) },
                leadingIcon = {
                    FigmaIcon(
                        name = "ic_search",
                        contentDescription = "Поиск",
                        tint = CatalogGray,
                        modifier = Modifier.size(22.dp),
                        fallback = "⌕"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = CatalogGray,
                    focusedBorderColor = CatalogBlue,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
            Spacer(Modifier.height(22.dp))
            Text(
                text = if (query.isBlank()) "Категории" else "Результаты поиска",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.height(12.dp))
        }
        if (query.isNotBlank()) {
            val found = allTools.filter {
                it.name.contains(query, true) || it.brand.contains(query, true)
            }
            if (found.isEmpty()) {
                CenteredText("Инструменты не найдены")
            } else {
                ToolList(
                    tools = found,
                    favorites = favorites,
                    busy = busy,
                    onTool = onTool,
                    onFavorite = onFavorite,
                    onCart = onCart
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = categories.sortedBy { category ->
                        categoryDesign.indexOfFirst { it.first == category.name }
                            .let { if (it == -1) Int.MAX_VALUE else it }
                    },
                    key = { it.id }
                ) { category ->
                    val fallback = categoryDesign.firstOrNull {
                        it.first == category.name
                    }?.second ?: "cat_default"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .clickable { onCategory(category.name) }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CatalogRemoteAsset(
                            imageUrl = category.imageUrl,
                            bucket = StorageImages.CATEGORY_BUCKET,
                            fallbackFileName = fallback,
                            description = category.name,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                        Text(
                            text = category.name,
                            color = CatalogGray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryToolsScreen(
    title: String,
    tools: List<Tool>,
    favorites: Set<Long>,
    busy: Set<Long>,
    onBack: () -> Unit,
    onTool: (Tool) -> Unit,
    onFavorite: (Tool) -> Unit,
    onCart: (Tool) -> Unit
) {
    Column(Modifier.fillMaxSize().background(CatalogBackground)) {
        BackHeading(title, onBack)
        if (tools.isEmpty()) CenteredText("В этой категории пока нет инструментов")
        else ToolList(tools, favorites, busy, onTool, onFavorite, onCart)
    }
}

@Composable
private fun ToolList(
    tools: List<Tool>,
    favorites: Set<Long>,
    busy: Set<Long>,
    onTool: (Tool) -> Unit,
    onFavorite: (Tool) -> Unit,
    onCart: (Tool) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(tools, key = { it.id }) { tool ->
            ToolRow(
                tool = tool,
                isFavorite = tool.id in favorites,
                isBusy = tool.id in busy,
                onClick = { onTool(tool) },
                onFavorite = { onFavorite(tool) },
                onCart = { onCart(tool) }
            )
        }
    }
}

@Composable
fun ToolRow(
    tool: Tool,
    isFavorite: Boolean,
    isBusy: Boolean,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onCart: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(Color.White)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CatalogRemoteAsset(
            imageUrl = tool.imageUrl,
            bucket = StorageImages.TOOL_BUCKET,
            fallbackFileName = productDrawableName(tool.name),
            description = tool.name,
            modifier = Modifier.weight(0.95f).fillMaxHeight().padding(end = 5.dp)
        )
        Column(modifier = Modifier.weight(1.12f).fillMaxHeight()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(
                    tool.name,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                FavoriteHeart(
                    selected = isFavorite,
                    enabled = !isBusy,
                    onClick = onFavorite,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(tool.brand, fontSize = 13.sp, color = CatalogGray, maxLines = 1)
            Spacer(Modifier.weight(1f))
            Text(
                rubles(tool.price),
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            AvailabilityText(tool.stock)
            Spacer(Modifier.height(9.dp))
            Button(
                onClick = onCart,
                enabled = tool.stock > 0 && !isBusy,
                modifier = Modifier.fillMaxWidth().height(33.dp),
                shape = RoundedCornerShape(7.dp),
                contentPadding = PaddingValues(horizontal = 5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CatalogBlue,
                    disabledContainerColor = CatalogGray
                )
            ) {
                FigmaIcon("ic_cart", null, Modifier.size(17.dp), tint = Color.White, fallback = "▢")
                Spacer(Modifier.width(9.dp))
                Text("В корзину", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun FavoriteHeart(
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        FigmaIcon(
            name = if (selected) "ic_favorite_filled" else "ic_favorite",
            contentDescription = if (selected) "Убрать из избранного" else "В избранное",
            tint = if (selected) CatalogRed else CatalogGray,
            modifier = Modifier.size(28.dp),
            fallback = if (selected) "♥" else "♡"
        )
    }
}

@Composable
fun AvailabilityText(stock: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(8.dp).clip(RoundedCornerShape(50))
                .background(if (stock > 0) CatalogGreen else CatalogRed)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            if (stock > 0) "В наличии: $stock шт" else "Нет в наличии",
            fontSize = 12.sp,
            color = if (stock > 0) CatalogGreen else CatalogRed,
            maxLines = 1
        )
    }
}

@Composable
fun ToolDetailScreen(
    tool: Tool,
    isFavorite: Boolean,
    isBusy: Boolean,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onCart: (Int) -> Unit
) {
    var quantity by rememberSaveable(tool.id) { mutableIntStateOf(1) }
    Column(Modifier.fillMaxSize().background(CatalogBackground)) {
        BackHeading("", onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            CatalogRemoteAsset(
                imageUrl = tool.imageUrl,
                bucket = StorageImages.TOOL_BUCKET,
                fallbackFileName = productDrawableName(tool.name),
                description = tool.name,
                modifier = Modifier.fillMaxWidth().height(245.dp)
                    .clip(RoundedCornerShape(18.dp)).background(Color.White)
            )
            Spacer(Modifier.height(18.dp))
            Text(tool.name, fontSize = 23.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(tool.brand, color = CatalogGray, fontSize = 14.sp)
                Text("Код товара: ${tool.id}", color = CatalogGray, fontSize = 12.sp)
            }
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(rubles(tool.price), fontSize = 23.sp, fontWeight = FontWeight.Bold)
                AvailabilityText(tool.stock)
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.weight(0.9f).height(42.dp)
                        .border(1.dp, CatalogGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { if (quantity > 1) quantity-- }, enabled = quantity > 1) {
                        FigmaIcon("ic_minus", "Уменьшить", Modifier.size(20.dp), tint = CatalogGray, fallback = "−")
                    }
                    Text("$quantity")
                    IconButton(onClick = { if (quantity < tool.stock) quantity++ },
                        enabled = quantity < tool.stock) {
                        FigmaIcon("ic_plus", "Увеличить", Modifier.size(20.dp), tint = CatalogGray, fallback = "+")
                    }
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = { onCart(quantity) },
                    enabled = tool.stock > 0 && !isBusy,
                    modifier = Modifier.weight(1.3f).height(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CatalogBlue,
                        disabledContainerColor = CatalogGray)
                ) {
                    FigmaIcon("ic_cart", null, Modifier.size(18.dp), tint = Color.White, fallback = "▢")
                    Spacer(Modifier.width(6.dp))
                    Text("В корзину", fontSize = 14.sp)
                }
            }
            TextButton(
                onClick = onFavorite,
                enabled = !isBusy,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                FigmaIcon(
                    name = if (isFavorite) "ic_favorite_filled" else "ic_favorite",
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = if (isFavorite) CatalogRed else CatalogGray,
                    fallback = if (isFavorite) "♥" else "♡"
                )
                Spacer(Modifier.width(5.dp))
                Text(if (isFavorite) "В избранном" else "В избранное", color = CatalogGray)
            }
            HorizontalDivider(color = CatalogGray.copy(alpha = .25f))
            Spacer(Modifier.height(15.dp))
            Text("Описание", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                text = tool.description.ifBlank { "Описание пока не добавлено" },
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
fun CartContent(
    state: CatalogUiState,
    onChange: (CartLine, Int) -> Unit,
    onCatalog: () -> Unit
) {
    val rows = state.cart.mapNotNull { line ->
        state.tools.firstOrNull { it.id == line.toolId }?.let { line to it }
    }
    Column(Modifier.fillMaxSize().background(CatalogBackground)) {
        ScreenHeading("Корзина")
        if (rows.isEmpty()) {
            EmptyScreen("В корзине пока пусто", onCatalog)
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rows, key = { it.first.id }) { (line, tool) ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            .background(Color.White).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CatalogRemoteAsset(
                            imageUrl = tool.imageUrl,
                            bucket = StorageImages.TOOL_BUCKET,
                            fallbackFileName = productDrawableName(tool.name),
                            description = tool.name,
                            modifier = Modifier.size(90.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(tool.name, maxLines = 2, fontSize = 14.sp, lineHeight = 17.sp)
                            Text(rubles(tool.price * line.quantity), fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onChange(line, line.quantity - 1) },
                                    enabled = line.toolId !in state.busyToolIds
                                ) { FigmaIcon("ic_minus", "Уменьшить", Modifier.size(20.dp), tint = CatalogGray, fallback = "−") }
                                Text("${line.quantity}")
                                IconButton(
                                    onClick = { onChange(line, line.quantity + 1) },
                                    enabled = line.quantity < tool.stock && line.toolId !in state.busyToolIds
                                ) { FigmaIcon("ic_plus", "Увеличить", Modifier.size(20.dp), tint = CatalogGray, fallback = "+") }
                            }
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().background(Color.White).padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Итого", fontWeight = FontWeight.SemiBold)
                Text(rubles(rows.sumOf { (line, tool) -> tool.price * line.quantity }),
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyScreen(title: String, onCatalog: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onCatalog, colors = ButtonDefaults.buttonColors(containerColor = CatalogBlue)) {
            Text("Перейти в каталог")
        }
    }
}

@Composable
fun BackHeading(title: String, onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 15.dp, end = 20.dp, top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(35.dp)) {
            FigmaIcon("ic_back", "Назад", Modifier.size(24.dp), tint = Color.Black, fallback = "←")
        }
        Text(title, fontSize = 21.sp, fontWeight = FontWeight.Bold,
            color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun ScreenHeading(title: String) {
    Text(
        text = title,
        color = Color.Black,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 14.dp)
    )
}

@Composable
fun CenteredText(text: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = CatalogGray, textAlign = TextAlign.Center)
    }
}
