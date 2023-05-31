
package io.agora.beauty.demo.utils

import android.util.Log

import java.lang.reflect.Field


object ReflectUtils {

    fun <T> getStaticFiledValue(clazz: Class<*>, fieldName: String?): T? {
        fieldName ?: return null
        try {
            val tmp: Field = clazz.getDeclaredField(fieldName)
            tmp.isAccessible = true
            return tmp.get(null) as? T
        } catch (e: NoSuchFieldException) {
            Log.e("Field", "Can not find field $fieldName in class ${clazz.simpleName}")
        } catch (e: IllegalAccessException) {
            Log.e("Field", "Could not access $fieldName in class ${clazz.simpleName}")
        }
        return null
    }
}