package com.example.storeit.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemeManager(context: Context) {
    private val dataStore = context.dataStore

    val isDarkTheme: Flow<Boolean> = dataStore.data.map {
        it[IS_DARK_THEME] ?: false
    }

    suspend fun setTheme(isDarkTheme: Boolean) {
        dataStore.edit {
            it[IS_DARK_THEME] = isDarkTheme
        }
    }

    companion object {
        private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }
}