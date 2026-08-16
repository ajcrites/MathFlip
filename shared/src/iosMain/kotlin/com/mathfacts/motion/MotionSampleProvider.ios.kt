package com.mathfacts.motion

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

@OptIn(ExperimentalForeignApi::class)
actual class MotionSampleProvider actual constructor() {
    private val motionManager = CMMotionManager()

    actual fun start(onSample: (MotionSample) -> Unit) {
        if (!motionManager.deviceMotionAvailable) return

        motionManager.deviceMotionUpdateInterval = 0.05
        motionManager.startDeviceMotionUpdatesToQueue(NSOperationQueue.mainQueue) { data, _ ->
            val gravity = data?.gravity ?: return@startDeviceMotionUpdatesToQueue
            val yaw = data.attitude.yaw
            gravity.useContents {
                onSample(
                    MotionSample(
                        gravityX = x,
                        gravityY = y,
                        gravityZ = z,
                        yaw = yaw,
                    ),
                )
            }
        }
    }

    actual fun stop() {
        motionManager.stopDeviceMotionUpdates()
    }
}
