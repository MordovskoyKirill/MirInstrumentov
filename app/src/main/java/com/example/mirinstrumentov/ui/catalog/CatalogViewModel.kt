package com.example.mirinstrumentov.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mirinstrumentov.data.model.CartLine
import com.example.mirinstrumentov.data.model.Tool
import com.example.mirinstrumentov.data.model.ToolCategory
import com.example.mirinstrumentov.data.repository.CatalogRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val loading: Boolean = true,
    val categories: List<ToolCategory> = emptyList(),
    val tools: List<Tool> = emptyList(),
    val favorites: Set<Long> = emptySet(),
    val cart: List<CartLine> = emptyList(),
    val busyToolIds: Set<Long> = emptySet(),
    val error: String? = null,
    val notice: String? = null
)

class CatalogViewModel : ViewModel() {
    private val repository = CatalogRepository()
    private val _state = MutableStateFlow(CatalogUiState())
    val state = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val categories = repository.categories()
                val products = repository.tools()
                val favorites = repository.favorites()
                val cart = repository.cartLines()
                _state.update {
                    it.copy(
                        loading = false,
                        categories = categories,
                        tools = products,
                        favorites = favorites,
                        cart = cart
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(loading = false, error = e.message ?: "Не удалось загрузить каталог")
                }
            }
        }
    }

    fun clearNotice() = _state.update { it.copy(notice = null) }

    fun toggleFavorite(tool: Tool) {
        if (tool.id in _state.value.busyToolIds) return
        val before = tool.id in _state.value.favorites
        perform(tool.id) {
            repository.toggleFavorite(tool.id, before)
            _state.update { current ->
                current.copy(favorites = if (before) {
                    current.favorites - tool.id
                } else {
                    current.favorites + tool.id
                })
            }
        }
    }

    fun addToCart(tool: Tool, quantity: Int = 1) {
        if (tool.id in _state.value.busyToolIds) return
        perform(tool.id) {
            repository.addToCart(tool.id, quantity, tool.stock)
            val newCart = repository.cartLines()
            _state.update { it.copy(cart = newCart, notice = "Добавлено в корзину") }
        }
    }

    fun changeQuantity(line: CartLine, quantity: Int) {
        if (line.toolId in _state.value.busyToolIds) return
        val available = _state.value.tools.firstOrNull { it.id == line.toolId }?.stock ?: 0
        perform(line.toolId) {
            repository.changeCartQuantity(line, quantity, available)
            _state.update { it.copy(cart = repository.cartLines()) }
        }
    }

    private fun perform(toolId: Long, action: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(busyToolIds = it.busyToolIds + toolId) }
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(notice = e.message ?: "Ошибка соединения") }
            } finally {
                _state.update { it.copy(busyToolIds = it.busyToolIds - toolId) }
            }
        }
    }
}
