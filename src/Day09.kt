import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlin.properties.Delegates.observable

private val start = 0 to 0

private sealed interface Knot {

    var position: Pair<Int, Int>

    class Tail : Knot {
        val positions = mutableSetOf(start)
        override var position by observable(initialValue = start) { _, _, newValue -> positions.add(newValue) }
    }

    class Head(private val next: Knot) : Knot {
        override var position by observable(initialValue = start) { _, _, newValue ->
            val (thisX, thisY) = newValue
            val (nextX, nextY) = next.position
            val diffX = thisX - nextX
            val diffY = thisY - nextY
            if (diffX.absoluteValue > 1 || diffY.absoluteValue > 1) {
                next.position =  (nextX + diffX.sign) to (nextY + diffY.sign)
            }
        }
    }
}

private fun Knot.move(direction: String, amount: Int) {
    repeat(amount) {
        position = when(direction) {
            "U" -> position.copy(second = position.second + 1)
            "D" -> position.copy(second = position.second - 1)
            "L" -> position.copy(first = position.first - 1)
            "R" -> position.copy(first = position.first + 1)
            else -> error(direction)
        }
    }
}

fun main() {

    fun day9(input: List<String>, ropeLength: Int): Int {
        val tail = Knot.Tail()
        val head = generateSequence<Knot>(tail) { Knot.Head(it) }.take(ropeLength).last()
        input.map { it.split(" ") }.forEach { (direction, amount) -> head.move(direction, amount.toInt()) }
        return tail.positions.size
    }

    fun part1(input: List<String>): Int {
        return day9(input, ropeLength = 2)
    }

    fun part2(input: List<String>): Int {
        return day9(input, ropeLength = 10)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day09_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 1)
    val testInput2 = readLines("Day09_test2")
    check(part2(testInput2) == 36)

    val input = readLines("Day09")
    check(part1(input) == 6384)
    println(part1(input))
    check(part2(input) == 2734)
    println(part2(input))
}
