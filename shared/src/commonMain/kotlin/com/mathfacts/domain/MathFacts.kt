package com.mathfacts.domain

import kotlin.random.Random

const val DEFAULT_MIN_BOUND = 0
const val DEFAULT_MAX_BOUND = 20

enum class Operation(val symbol: String) {
    Addition("+"),
    Subtraction("−"),
}

data class MathFact(
    val first: Int,
    val second: Int,
    val operation: Operation,
) {
    val answer: Int
        get() = when (operation) {
            Operation.Addition -> first + second
            Operation.Subtraction -> first - second
        }

    val question: String
        get() = "$first ${operation.symbol} $second"

    val equation: String
        get() = "$question = $answer"
}

class MathFactGenerator(
    private val range: IntRange = DEFAULT_MIN_BOUND..DEFAULT_MAX_BOUND,
    operations: Set<Operation> = Operation.entries.toSet(),
    private val random: Random = Random.Default,
) {
    private val availableOperations: List<Operation>

    init {
        require(!range.isEmpty()) { "Math fact range cannot be empty." }
        require(operations.isNotEmpty()) { "At least one operation must be selected." }
        availableOperations = operations.filter { validFirstBounds(it) != null }
        require(availableOperations.isNotEmpty()) {
            "Math fact range does not contain a valid operand and answer combination."
        }
    }

    fun next(): MathFact {
        val operation = availableOperations.random(random)
        val first = randomIn(validFirstBounds(operation)!!)
        val second = randomIn(validSecondBounds(operation, first))

        return MathFact(first = first, second = second, operation = operation)
    }

    private fun validFirstBounds(operation: Operation): LongRange? {
        val minimum = range.first.toLong()
        val maximum = range.last.toLong()
        val bounds = when (operation) {
            Operation.Addition -> maxOf(minimum, minimum - maximum)..minOf(maximum, maximum - minimum)
            Operation.Subtraction -> maxOf(minimum, minimum * 2)..minOf(maximum, maximum * 2)
        }

        return bounds.takeUnless { it.isEmpty() }
    }

    private fun validSecondBounds(operation: Operation, first: Int): LongRange {
        val minimum = range.first.toLong()
        val maximum = range.last.toLong()
        val firstValue = first.toLong()

        return when (operation) {
            Operation.Addition ->
                maxOf(minimum, minimum - firstValue)..minOf(maximum, maximum - firstValue)
            Operation.Subtraction ->
                maxOf(minimum, firstValue - maximum)..minOf(maximum, firstValue - minimum)
        }
    }

    private fun randomIn(bounds: LongRange): Int =
        random.nextLong(from = bounds.first, until = bounds.last + 1).toInt()
}
