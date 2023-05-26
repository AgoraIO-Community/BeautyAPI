package io.agora.beauty.sensetime

import com.sensetime.effects.STRenderer
import io.agora.base.VideoFrame
import io.agora.rtc2.RtcEngine


data class Config(
    val rtcEngine: RtcEngine,
    val stRenderer: STRenderer,
    val useCustom: Boolean = false
)

enum class ErrorCode(val value: Int) {
    ERROR_OK(0),
    ERROR_HAS_NOT_INITIALIZED(101),
    ERROR_HAS_INITIALIZED(102),
    ERROR_HAS_RELEASED(103),
    ERROR_PROCESS_NOT_CUSTOM(104),
    ERROR_PROCESS_DISABLE(105),
}

fun createSenseTimeBeautyAPI(): SenseTimeBeautyAPI = SenseTimeBeautyAPIImpl()

interface SenseTimeBeautyAPI {

    fun initialize(config: Config): Int

    fun enable(enable: Boolean): Int

    fun onFrame(videoFrame: VideoFrame): Int

    fun setOptimizedDefault(): Int

    fun release(): Int

}