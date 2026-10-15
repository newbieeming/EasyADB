package me.newbieeming.util

import com.russhwolf.settings.PreferencesSettings
import me.newbieeming.nodeName
import java.util.prefs.Preferences

object PreferencesUtil {
    private val settings = PreferencesSettings(Preferences.userRoot().node(nodeName))

    /**
     * 选中的adb路径
     */
    const val PREFERENCES_ADB_PATH = "settings.adb_path"

    /**
     * 自定义的adb路径
     */
    const val PREFERENCES_CUSTOMER_ADB_PATH = "settings.customer_adb_path"

    /**
     * 截图保存路径
     */
    const val PREFERENCES_SCREENSHOT_SAVE_PATH = "settings.screenshot_save_path"

    /**
     * 截图保存开关
     */
    const val PREFERENCES_SCREENSHOT_SAVE_ENABLED = "settings.screenshot_save_enabled"

    /**
     * Windows 文件传输窗口自动关闭
     */
    const val PREFERENCES_CMD_AUTO_CLOSE_ENABLED = "settings.cmd_auto_close.enabled"
    const val PREFERENCES_CMD_AUTO_CLOSE_TIMEOUT = "settings.cmd_auto_close.timeout"

    /**
     * 选中的主题
     */
    const val PREFERENCES_THEME = "settings.theme"

    /**
     * 标题栏透明度（0.0f ~ 1.0f）
     */
    const val PREFERENCES_TITLE_BAR_ALPHA = "settings.title_bar_alpha"

    /**
     * Loading最短显示时长（ms），应用本身启动时间之外额外等待的时长
     */
    const val PREFERENCES_LOADING_MIN_DURATION_MS = "settings.loading_min_duration_ms"

    /**
     * 关闭窗口后继续运行
     */
    const val PREFERENCES_KEEP_RUNNING_ON_CLOSE = "settings.window.keep_running_on_close"

    /**
     * 窗口大小模式
     */
    const val PREFERENCES_WINDOW_SIZE_MODE = "settings.window_size.mode"

    /**
     * 自定义窗口宽高（dp）
     */
    const val PREFERENCES_WINDOW_WIDTH_DP = "settings.window_size.width_dp"
    const val PREFERENCES_WINDOW_HEIGHT_DP = "settings.window_size.height_dp"

    /**
     * 记住窗口宽高（dp）
     */
    const val PREFERENCES_WINDOW_REMEMBER_WIDTH_DP = "settings.window_size.remember_width_dp"
    const val PREFERENCES_WINDOW_REMEMBER_HEIGHT_DP = "settings.window_size.remember_height_dp"

    /**
     * 应用管理页面的过滤器
     */
    const val PREFERENCES_APP_FILTER = "app.filter"

    /**
     * 应用管理页面的自动刷新开关
     */
    const val PREFERENCES_APP_AUTO = "app.auto"
    const val PREFERENCES_APP_THIRD = "app.third"

    /**
     * 应用管理页面的显示模式
     */
    const val PREFERENCES_APP_MODE = "app.mode"

    /**
     * 文件管理收藏夹路径列表
     */
    private const val PREFERENCES_FILE_FAVORITES = "file.favorites"

    fun set(key: String, value: Any) {
        when (value) {
            is Boolean -> settings.putBoolean(key, value)
            is Int -> settings.putInt(key, value)
            is Float -> settings.putFloat(key, value)
            is Long -> settings.putLong(key, value)
            is Double -> settings.putDouble(key, value)
            is String -> settings.putString(key, value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, default: T): T {
        return when (default) {
            is Boolean -> settings.getBoolean(key, default)
            is Int -> settings.getInt(key, default)
            is Float -> settings.getFloat(key, default)
            is Long -> settings.getLong(key, default)
            is Double -> settings.getDouble(key, default)
            is String -> settings.getString(key, default)
            else -> default
        } as T
    }

    fun clear() {
        settings.clear()
    }

    /**
     * 获取收藏夹路径列表
     */
    fun getFavorites(): List<String> {
        val favoritesString = get(PREFERENCES_FILE_FAVORITES, "")
        return if (favoritesString.isEmpty()) {
            emptyList()
        } else {
            favoritesString.split("|").filter { it.isNotEmpty() }
        }
    }

    fun setFavorites(paths: Collection<String>) {
        set(PREFERENCES_FILE_FAVORITES, paths.distinct().joinToString("|"))
    }

    /**
     * 添加路径到收藏夹
     */
    fun addFavorite(path: String) {
        val favorites = getFavorites().toMutableList()
        if (!favorites.contains(path)) {
            favorites.add(path)
            set(PREFERENCES_FILE_FAVORITES, favorites.joinToString("|"))
        }
    }

    /**
     * 从收藏夹移除路径
     */
    fun removeFavorite(path: String) {
        val favorites = getFavorites().toMutableList()
        if (favorites.remove(path)) {
            set(PREFERENCES_FILE_FAVORITES, favorites.joinToString("|"))
        }
    }

    /**
     * 检查路径是否在收藏夹中
     */
    fun isFavorite(path: String): Boolean {
        return getFavorites().contains(path)
    }
}
