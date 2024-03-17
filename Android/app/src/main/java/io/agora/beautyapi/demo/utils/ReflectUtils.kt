
package io.agora.beautyapi.demo.utils

import android.util.Log
import io.agora.rtc2.video.VideoEncoderConfiguration.VideoDimensions

import java.lang.reflect.Field


object ReflectUtils {

    fun <T> getStaticFiledValue(clazz: Class<*>, fieldName: String?): T? {
        fieldName ?: return null
        try {
            val tmp: Field = clazz.getDeclaredField(fieldName)
            tmp.isAccessible = true
            return tmp.get(null) as? T
        } catch (e: NoSuchFieldException) {
            Log.e("ReflectUtils", "getStaticFiledValue >> Can not find field $fieldName in class ${clazz.simpleName}")
        } catch (e: IllegalAccessException) {
            Log.e("ReflectUtils", "getStaticFiledValue >> Could not access $fieldName in class ${clazz.simpleName}")
        }

        if (fieldName.startsWith("VD")) {
            try {
                val split = fieldName.split("_")[1].split("x")
                val width = split[0].toInt()
                val height = split[1].toInt()
                return VideoDimensions(width, height) as? T
            } catch (e: Exception) {
                Log.e("ReflectUtils", "getStaticFiledValue >> The format of video dimension is wrong. $fieldName")
            }
        }

        return null
    }
}