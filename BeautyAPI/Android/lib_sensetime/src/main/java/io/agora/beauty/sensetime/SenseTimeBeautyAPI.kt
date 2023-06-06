package io.agora.beauty.sensetime

import com.sensetime.effects.STRenderKit
import io.agora.base.VideoFrame
import io.agora.rtc2.RtcEngine

enum class ProcessMode{
    Agora, Custom
}

interface IEventCallback{
    fun onBeautyStatus(stats: BeautyStats)
}

data class BeautyStats(
    val totalCostTimeMs : Long
)

data class Config(
    val rtcEngine: RtcEngine,
    val stRenderKit: STRenderKit,
    val eventCallback: IEventCallback? = null,
    val processMode: ProcessMode = ProcessMode.Agora
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

    fun setOptimizedDefault(enable: Boolean = true): Int

    fun release(): Int

}