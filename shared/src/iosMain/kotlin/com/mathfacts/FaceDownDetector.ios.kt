package com.mathfacts

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue

@OptIn(ExperimentalForeignApi::class)
actual class FaceDownDetector actual constructor() {
    private val motionManager = CMMotionManager()
    private var wasFaceDown = false

    actual fun start(onFaceDown: () -> Unit) {
        if (!motionManager.accelerometerAvailable) return
        motionManager.accelerometerUpdateInterval = 0.15
        motionManager.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue) { data, _ ->
            val zAcceleration = data?.acceleration?.useContents { z }
                ?: return@startAccelerometerUpdatesToQueue
            val isFaceDown = zAcceleration > 0.75

            if (isFaceDown && !wasFaceDown) {
                onFaceDown()
            }
            wasFaceDown = isFaceDown
        }
    }

    actual fun stop() {
        motionManager.stopAccelerometerUpdates()
        wasFaceDown = false
    }
}
