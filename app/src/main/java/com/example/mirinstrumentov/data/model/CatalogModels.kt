package com.example.mirinstrumentov.data.model

data class ToolCategory(
    val id: Long,
    val name: String,
    val imageUrl: String? = null
)

data class Tool(
    val id: Long,
    val categoryId: Long,
    val name: String,
    val brand: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String? = null
)

data class CartLine(
    val id: Long,
    val toolId: Long,
    val quantity: Int
)
