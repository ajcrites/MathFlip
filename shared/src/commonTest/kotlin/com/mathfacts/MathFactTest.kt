package com.mathfacts

import com.mathfacts.domain.MathFact
import com.mathfacts.domain.MathFactGenerator
import com.mathfacts.domain.Operation
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MathFactTest {
    @Test
    fun generatedFactsStayWithinZeroAndTwenty() {
        val random = Random(1234)

        repeat(10_000) {
            val fact = MathFactGenerator(random = random).next()

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
    fun generatorSupportsACustomRange() {
        val range = 4..12
        val generator = MathFactGenerator(range = range, random = Random(5678))

        repeat(1_000) {
            val fact = generator.next()

            assertTrue(fact.first in range)
            assertTrue(fact.second in range)
            assertTrue(fact.answer in range)
        }
    }

    @Test
    fun generatorSupportsNegativeOperandsAndAnswers() {
        val range = -20..20
        val generator = MathFactGenerator(range = range, random = Random(9012))
        var foundNegativeAnswer = false

        repeat(1_000) {
            val fact = generator.next()

            assertTrue(fact.first in range)
            assertTrue(fact.second in range)
            assertTrue(fact.answer in range)
            foundNegativeAnswer = foundNegativeAnswer || fact.answer < 0
        }

        assertTrue(foundNegativeAnswer)
    }

    @Test
    fun factFormatsQuestionAndRevealedEquation() {
        val fact = MathFact(first = 12, second = 7, operation = Operation.Subtraction)

        assertEquals("12 − 7", fact.question)
        assertEquals("12 − 7 = 5", fact.equation)
    }
}
