package com.xmbest.locale

import com.xmbest.Config
import java.util.Locale
import java.util.MissingResourceException
import java.util.ResourceBundle

class PropertiesLocalization(private val resourceBundle: ResourceBundle) {
    /**
     * 获取字符串（无参数）
     */
    fun get(key: String): String = resourceBundle.getString(key)

    /**
     * 获取带参数的字符串（支持占位符 {0}, {1}, ...）
     */
    fun get(key: String, vararg args: Any): String {
        var result = resourceBundle.getString(key)
        args.forEachIndexed { index, arg ->
            result = result.replace("{${index}}", arg.toString())
        }
        return result
    }

    companion object {
        /**
         * 加载指定语言的 ResourceBundle
         * @param baseName 资源文件基础名
         * @param locale 语言环境（如 Locale.CHINA）
         */
        @Throws(MissingResourceException::class)
        fun create(baseName: String, locale: Locale = Config.locale.value): PropertiesLocalization {
            return PropertiesLocalization(ResourceBundle.getBundle(baseName, locale))
        }
    }
}