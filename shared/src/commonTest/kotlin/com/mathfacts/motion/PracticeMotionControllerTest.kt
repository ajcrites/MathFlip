package com.mathfacts.motion

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PracticeMotionControllerTest {
    private val upright = sampleAtDegrees(0.0)

    @Test
    fun questionOnlyRevealsOnFaceDownEdge() {
        val controller = PracticeMotionController()
        controller.onQuestionShown()

        assertNull(controller.process(MotionSample(0.0, 0.0, -1.0, 0.0)))
        assertEquals(
            PracticeMotionEvent.RevealAnswer,
            controller.process(MotionSample(0.0, 0.0, 0.9, 0.0)),
        )
        assertNull(controller.process(MotionSample(0.0, 0.0, 0.9, 0.0)))
    }

    @Test
    fun answerMotionIsIgnoredUntilTheUiUnlocksIt() {
        val controller = PracticeMotionController()
        controller.onAnswerShown()

        repeat(6) { assertNull(controller.process(upright)) }
        repeat(3) { assertNull(controller.process(sampleAtDegrees(90.0))) }

        controller.unlockAnswer()
        repeat(4) { assertNull(controller.process(upright)) }
        assertNull(controller.process(sampleAtDegrees(90.0)))
        assertEquals(PracticeMotionEvent.Correct, controller.process(sampleAtDegrees(90.0)))
    }

    @Test
    fun clockwiseTwistMarksTheAnswerIncorrect() {
        val controller = armedController()

        assertNull(controller.process(sampleAtDegrees(-90.0)))
        assertEquals(PracticeMotionEvent.Incorrect, controller.process(sampleAtDegrees(-90.0)))
    }

    @Test
    fun pitchAndRollDoNotAffectYawScoring() {
        val controller = PracticeMotionController()
        controller.onAnswerShown()
        controller.unlockAnswer()

        repeat(4) {
            assertNull(controller.process(MotionSample(0.6, 0.2, 0.77, 0.0)))
        }
        val turnedAndTilted = MotionSample(-0.5, 0.5, -0.7, PI / 2.0)
        assertNull(controller.process(turnedAndTilted))
        assertEquals(PracticeMotionEvent.Correct, controller.process(turnedAndTilted))
    }

    @Test
    fun scoringEventOnlyFiresOnce() {
        val controller = armedController()

        assertNull(controller.process(sampleAtDegrees(90.0)))
        assertEquals(PracticeMotionEvent.Correct, controller.process(sampleAtDegrees(90.0)))
        assertNull(controller.process(sampleAtDegrees(90.0)))
    }

    @Test
    fun yawDifferenceWrapsAcrossTheAngleBoundary() {
        val controller = PracticeMotionController()
        controller.onAnswerShown()
        controller.unlockAnswer()
        repeat(4) { controller.process(sampleAtDegrees(170.0)) }

        assertNull(controller.process(sampleAtDegrees(-100.0)))
        assertEquals(PracticeMotionEvent.Correct, controller.process(sampleAtDegrees(-100.0)))
    }

    private fun armedController(): PracticeMotionController = PracticeMotionController().also { controller ->
        controller.onAnswerShown()
        controller.unlockAnswer()
        repeat(4) { controller.process(upright) }
    }

    private fun sampleAtDegrees(degrees: Double): MotionSample {
        return MotionSample(
            gravityX = 0.0,
            gravityY = -1.0,
            gravityZ = 0.0,
            yaw = degrees * PI / 180.0,
        )
    }
}
