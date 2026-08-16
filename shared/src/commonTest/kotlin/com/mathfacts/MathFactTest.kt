package com.mathfacts

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MathFactTest {
    @Test
    fun generatedFactsStayWithinZeroAndTwenty() {
        val random = Random(1234)

        repeat(10_000) {
            val fact = randomMathFact(random)

            assertTrue(fact.first in 0..20)
            assertTrue(fact.second >= 0)
            assertTrue(fact.answer in 0..20)
            when (fact.operation) {
                Operation.Addition -> assertTrue(fact.second <= 20 - fact.first)
                Operation.Subtraction -> assertTrue(fact.second <= fact.first)
            }
        }
    }

    @Test
    fun factFormatsQuestionAndRevealedEquation() {
        val fact = MathFact(first = 12, second = 7, operation = Operation.Subtraction)

        assertEquals("12 − 7", fact.question)
        assertEquals("12 − 7 = 5", fact.equation)
    }
}
