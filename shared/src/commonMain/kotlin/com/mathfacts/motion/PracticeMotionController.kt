package com.mathfacts.motion

import kotlin.math.PI
import kotlin.math.abs

data class MotionSample(
    val gravityX: Double,
    val gravityY: Double,
    val gravityZ: Double,
    val yaw: Double,
)

enum class PracticeMotionEvent {
    RevealAnswer,
    Correct,
    Incorrect,
}

/**
 * Interprets platform motion samples according to the current practice-card state.
 * Timing remains with the UI so touch and motion share the same answer lock.
 */
class PracticeMotionController {
    private enum class Mode {
        Question,
        AnswerLocked,
        AwaitingBaseline,
        AnswerArmed,
        Disabled,
    }

    private var mode = Mode.Disabled
    private var wasFaceDown = false
    private var candidateBaseline: Double? = null
    private var stableBaselineSamples = 0
    private var baselineAngle = 0.0
    private var thresholdSamples = 0
    private var thresholdDirection = 0

    fun onQuestionShown() {
        resetTracking()
        mode = Mode.Question
    }

    fun onAnswerShown() {
        resetTracking()
        mode = Mode.AnswerLocked
    }

    fun unlockAnswer() {
        if (mode == Mode.AnswerLocked) {
            mode = Mode.AwaitingBaseline
        }
    }

    fun disable() {
        resetTracking()
        mode = Mode.Disabled
    }

    fun process(sample: MotionSample): PracticeMotionEvent? {
        return when (mode) {
            Mode.Question -> processQuestion(sample)
            Mode.AwaitingBaseline -> establishBaseline(sample)
            Mode.AnswerArmed -> processTwist(sample)
            Mode.AnswerLocked, Mode.Disabled -> null
        }
    }

    private fun processQuestion(sample: MotionSample): PracticeMotionEvent? {
        val isFaceDown = sample.gravityZ >= FACE_DOWN_THRESHOLD
        val event = if (isFaceDown && !wasFaceDown) PracticeMotionEvent.RevealAnswer else null
        wasFaceDown = isFaceDown
        return event
    }

    private fun establishBaseline(sample: MotionSample): PracticeMotionEvent? {
        val angle = sample.yaw
        val candidate = candidateBaseline
        if (candidate == null || abs(shortestAngle(angle - candidate)) > BASELINE_STABILITY_RADIANS) {
            candidateBaseline = angle
            stableBaselineSamples = 1
            return null
        }

        candidateBaseline = circularBlend(candidate, angle, stableBaselineSamples + 1)
        stableBaselineSamples += 1
        if (stableBaselineSamples >= REQUIRED_BASELINE_SAMPLES) {
            baselineAngle = candidateBaseline ?: angle
            thresholdSamples = 0
            thresholdDirection = 0
            mode = Mode.AnswerArmed
        }
        return null
    }

    private fun processTwist(sample: MotionSample): PracticeMotionEvent? {
        val delta = shortestAngle(sample.yaw - baselineAngle)
        val direction = when {
            delta >= TWIST_THRESHOLD_RADIANS -> 1
            delta <= -TWIST_THRESHOLD_RADIANS -> -1
            else -> 0
        }

        if (direction == 0) {
            thresholdSamples = 0
            thresholdDirection = 0
            return null
        }

        if (direction == thresholdDirection) {
            thresholdSamples += 1
        } else {
            thresholdDirection = direction
            thresholdSamples = 1
        }

        if (thresholdSamples < REQUIRED_THRESHOLD_SAMPLES) return null

        mode = Mode.Disabled
        // Core Motion reports positive yaw for counterclockwise rotation around world vertical.
        return if (direction > 0) PracticeMotionEvent.Correct else PracticeMotionEvent.Incorrect
    }

    private fun resetTracking() {
        wasFaceDown = false
        candidateBaseline = null
        stableBaselineSamples = 0
        thresholdSamples = 0
        thresholdDirection = 0
    }

    private fun circularBlend(current: Double, next: Double, sampleCount: Int): Double =
        shortestAngle(current + shortestAngle(next - current) / sampleCount)

    private fun shortestAngle(angle: Double): Double {
        var normalized = angle
        while (normalized > PI) normalized -= 2.0 * PI
        while (normalized < -PI) normalized += 2.0 * PI
        return normalized
    }

    private companion object {
        const val FACE_DOWN_THRESHOLD = 0.75
        const val REQUIRED_BASELINE_SAMPLES = 4
        const val REQUIRED_THRESHOLD_SAMPLES = 2
        val BASELINE_STABILITY_RADIANS = 6.0 * PI / 180.0
        val TWIST_THRESHOLD_RADIANS = 75.0 * PI / 180.0
    }
}

expect class MotionSampleProvider() {
    fun start(onSample: (MotionSample) -> Unit)
    fun stop()
}
