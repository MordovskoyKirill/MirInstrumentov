package com.example.mirinstrumentov.data.repository

import com.example.mirinstrumentov.data.model.CartLine
import com.example.mirinstrumentov.data.model.Tool
import com.example.mirinstrumentov.data.model.ToolCategory
import com.example.mirinstrumentov.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

class CatalogRepository {
    private val client = SupabaseClient.client

    private fun userId(): String = client.auth.currentUserOrNull()?.id
        ?: error("Сначала войдите в аккаунт")

    suspend fun categories(): List<ToolCategory> =
        client.from("categories").select().decodeList<JsonObject>().map { row ->
            ToolCategory(
                id = row.getValue("category_id").jsonPrimitive.longOrNull ?: 0L,
                name = row.getValue("name").jsonPrimitive.content,
                imageUrl = row["image_url"]?.jsonPrimitive?.contentOrNull
            )
        }

    suspend fun tools(): List<Tool> {
        val inventory = client.from("stock").select()
            .decodeList<JsonObject>().associate { row ->
                (row.getValue("tool_id").jsonPrimitive.longOrNull ?: 0L) to
                        (row.getValue("quantity").jsonPrimitive.intOrNull ?: 0)
            }

        return client.from("tools").select().decodeList<JsonObject>()
            .filter { it["is_active"]?.jsonPrimitive?.content != "false" }
            .map { row ->
                val id = row.getValue("tool_id").jsonPrimitive.longOrNull ?: 0L
                Tool(
                    id = id,
                    categoryId = row.getValue("category_id").jsonPrimitive.longOrNull ?: 0L,
                    name = row.getValue("name").jsonPrimitive.content,
                    brand = row.getValue("brand").jsonPrimitive.content,
                    description = row["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                    price = row.getValue("price").jsonPrimitive.doubleOrNull ?: 0.0,
                    stock = inventory[id] ?: 0,
                    imageUrl = row["image_url"]?.jsonPrimitive?.contentOrNull
                )
            }
    }

    suspend fun favorites(): Set<Long> {
        val user = userId()
        return client.from("favorites").select {
            filter { eq("user_id", user) }
        }.decodeList<JsonObject>().mapNotNull { row ->
            row["tool_id"]?.jsonPrimitive?.longOrNull
        }.toSet()
    }

    suspend fun toggleFavorite(toolId: Long, wasFavorite: Boolean) {
        val user = userId()
        if (wasFavorite) {
            client.from("favorites").delete {
                filter {
                    eq("user_id", user)
                    eq("tool_id", toolId)
                }
            }
        } else {
            client.from("favorites").insert(buildJsonObject {
                put("user_id", user)
                put("tool_id", toolId)
            })
        }
    }

    private suspend fun cartId(): Long {
        val user = userId()
        val rows = client.from("carts").select {
            filter { eq("user_id", user) }
        }.decodeList<JsonObject>()
        return rows.firstOrNull()?.get("cart_id")?.jsonPrimitive?.longOrNull
            ?: error("Корзина не создана. Проверьте SQL-триггер handle_new_user")
    }

    suspend fun cartLines(): List<CartLine> {
        val id = cartId()
        return client.from("cart_items").select {
            filter { eq("cart_id", id) }
        }.decodeList<JsonObject>().map { row ->
            CartLine(
                id = row.getValue("id").jsonPrimitive.longOrNull ?: 0L,
                toolId = row.getValue("tool_id").jsonPrimitive.longOrNull ?: 0L,
                quantity = row.getValue("quantity").jsonPrimitive.intOrNull ?: 1
            )
        }
    }

    suspend fun addToCart(toolId: Long, amount: Int, available: Int) {
        require(amount > 0) { "Укажите количество" }
        val currentCartId = cartId()
        val existing = cartLines().firstOrNull { it.toolId == toolId }
        val newQuantity = (existing?.quantity ?: 0) + amount
        require(newQuantity <= available) { "Недостаточно товара на складе" }
        if (existing == null) {
            client.from("cart_items").insert(buildJsonObject {
                put("cart_id", currentCartId)
                put("tool_id", toolId)
                put("quantity", amount)
            })
        } else {
            client.from("cart_items").update(buildJsonObject {
                put("quantity", newQuantity)
            }) {
                filter { eq("id", existing.id) }
            }
        }
    }

    suspend fun changeCartQuantity(line: CartLine, quantity: Int, available: Int) {
        if (quantity <= 0) {
            client.from("cart_items").delete {
                filter { eq("id", line.id) }
            }
        } else {
            require(quantity <= available) { "Недостаточно товара на складе" }
            client.from("cart_items").update(buildJsonObject {
                put("quantity", quantity)
            }) {
                filter { eq("id", line.id) }
            }
        }
    }
}
