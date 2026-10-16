package com.example.mirinstrumentov.ui.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.mirinstrumentov.data.model.Tool
import com.example.mirinstrumentov.data.model.ToolCategory

private val sample = listOf(
    Tool(1, 2, "Дрель ударная Bosch GSB 550", "Bosch", "Ударная дрель для сверления", 6990.0, 60),
    Tool(2, 2, "Шуруповёрт DeWalt DCD771", "DeWalt", "Аккумуляторный шуруповёрт", 8990.0, 60),
    Tool(3, 2, "Гайковерт ударный HI-Pro Спирит-3", "HI-Pro", "", 159500.0, 0),
    Tool(4, 2, "Болгарка Makita GA4534", "Makita", "", 11090.0, 60)
)

@Preview(name = "Каталог", showBackground = true, widthDp = 412, heightDp = 850)
@Composable
private fun CatalogPreview() {
    CategoriesScreen(
        query = "", onQueryChange = {},
        categories = categoryDesign.mapIndexed { index, item -> ToolCategory(index.toLong() + 1, item.first) },
        allTools = sample, favorites = emptySet(), busy = emptySet(),
        onCategory = {}, onTool = {}, onFavorite = {}, onCart = {}
    )
}

@Preview(name = "Список инструментов", showBackground = true, widthDp = 412, heightDp = 850)
@Composable
private fun ToolsPreview() {
    CategoryToolsScreen(
        title = "Электроинструменты", tools = sample,
        favorites = setOf(1, 2), busy = emptySet(),
        onBack = {}, onTool = {}, onFavorite = {}, onCart = {}
    )
}

@Preview(name = "Карточка товара", showBackground = true, widthDp = 412, heightDp = 850)
@Composable
private fun DetailsPreview() {
    ToolDetailScreen(
        tool = sample.first(), isFavorite = false, isBusy = false,
        onBack = {}, onFavorite = {}, onCart = {}
    )
}
