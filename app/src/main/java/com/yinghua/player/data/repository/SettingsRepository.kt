package com.yinghua.player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.yinghua.player.data.model.AppSettings
import com.yinghua.player.data.model.DecoderMode
import com.yinghua.player.data.model.PlayOrientation
import com.yinghua.player.data.model.SubtitleColor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val DECODER_MODE = stringPreferencesKey("decoder_mode")
        val DEFAULT_ORIENTATION = stringPreferencesKey("default_orientation")
        val SHOW_THUMBNAIL = booleanPreferencesKey("show_thumbnail")
        val CONTINUOUS_PLAY = booleanPreferencesKey("continuous_play")
        val SUBTITLE_SIZE = intPreferencesKey("subtitle_size")
        val SUBTITLE_COLOR = stringPreferencesKey("subtitle_color")
        val LAST_SCAN_TIME = longPreferencesKey("last_scan_time")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            AppSettings(
                decoderMode = DecoderMode.valueOf(
                    prefs[Keys.DECODER_MODE] ?: DecoderMode.AUTO.name
                ),
                defaultOrientation = PlayOrientation.valueOf(
                    prefs[Keys.DEFAULT_ORIENTATION] ?: PlayOrientation.AUTO.name
                ),
                showThumbnail = prefs[Keys.SHOW_THUMBNAIL] ?: true,
                continuousPlay = prefs[Keys.CONTINUOUS_PLAY] ?: true,
                subtitleSize = prefs[Keys.SUBTITLE_SIZE] ?: 16,
                subtitleColor = SubtitleColor.valueOf(
                    prefs[Keys.SUBTITLE_COLOR] ?: SubtitleColor.WHITE.name
                ),
                lastScanTime = prefs[Keys.LAST_SCAN_TIME] ?: 0L,
            )
        }

    suspend fun updateDecoderMode(mode: DecoderMode) {
        context.dataStore.edit { it[Keys.DECODER_MODE] = mode.name }
    }

    suspend fun updateOrientation(orientation: PlayOrientation) {
        context.dataStore.edit { it[Keys.DEFAULT_ORIENTATION] = orientation.name }
    }

    suspend fun updateShowThumbnail(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_THUMBNAIL] = show }
    }

    suspend fun updateContinuousPlay(continuous: Boolean) {
        context.dataStore.edit { it[Keys.CONTINUOUS_PLAY] = continuous }
    }

    suspend fun updateSubtitleSize(size: Int) {
        context.dataStore.edit { it[Keys.SUBTITLE_SIZE] = size }
    }

    suspend fun updateSubtitleColor(color: SubtitleColor) {
        context.dataStore.edit { it[Keys.SUBTITLE_COLOR] = color.name }
    }

    suspend fun updateLastScanTime(time: Long) {
        context.dataStore.edit { it[Keys.LAST_SCAN_TIME] = time }
    }
}
