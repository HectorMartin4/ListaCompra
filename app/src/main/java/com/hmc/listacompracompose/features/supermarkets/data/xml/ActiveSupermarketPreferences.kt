package com.hmc.listacompracompose.features.supermarkets.data.xml

import android.content.Context
import com.hmc.listacompracompose.features.data.local.db.entities.DEFAULT_SUPERMARKET_ID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single
class ActiveSupermarketPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("supermarket_prefs", Context.MODE_PRIVATE)

    private val _activeId = MutableStateFlow(prefs.getInt(KEY_ACTIVE_ID, DEFAULT_SUPERMARKET_ID))
    val activeId: StateFlow<Int> = _activeId.asStateFlow()

    fun setActive(id: Int) {
        prefs.edit().putInt(KEY_ACTIVE_ID, id).apply()
        _activeId.value = id
    }

    private companion object {
        const val KEY_ACTIVE_ID = "active_supermarket_id"
    }
}