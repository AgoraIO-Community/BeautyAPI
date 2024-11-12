package io.agora.beautyapi.demo.module.faceunity.utils.device;

/**
 * DESC：
 * Created on 2021/3/12
 */
interface DeviceScoreProvider {
    /**
     * Retrieves the CPU score.
     *
     * @param cpuName The name of the CPU.
     * @return The CPU score as a double.
     */
    double getCpuScore(String cpuName);

    /**
     * Retrieves the GPU score.
     *
     * @param glRenderer The name of the GPU renderer.
     * @return The GPU score as a double.
     */
    double getGpuScore(String glRenderer);
}
