import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

private enum class Spot { OPEN, WALL }
private enum class Facing(val score: Int) { RIGHT(0), DOWN(1), LEFT(2), UP(3) }
private sealed interface Direction {
    object Left : Direction
    object Right : Direction
    data class Move(val amount: Int) : Direction
}

private val pathGrammar = object : Grammar<List<Direction>>() {
    val number by regexToken("\\d+")
    val l by literalToken("L")
    val r by literalToken("R")

    val move by number use { Direction.Move(text.toInt()) }
    val left by l use { Direction.Left }
    val right by r use { Direction.Right }
    val term by move or left or right
    val terms: Parser<List<Direction>> = zeroOrMore(term)
    override val rootParser by terms
}

fun main() {
    fun part1(input: String): Int {
        val (boardInput, directionInput) = input.split("\n\n")
        val board = boardInput.lines().flatMapIndexed { row, line ->
            line.mapIndexedNotNull { column, char ->
                when (char) {
                    '.' -> ((row + 1) to (column + 1)) to Spot.OPEN
                    '#' -> ((row + 1) to (column + 1)) to Spot.WALL
                    else -> null
                }
            }
        }.toMap()
        val minRow = board.minOf { it.key.first }
        val maxRow = board.maxOf { it.key.first }
        val minColumn = board.minOf { it.key.second }
        val maxColumn = board.maxOf { it.key.second }

        var position = board
            .minWith(compareBy<Map.Entry<Pair<Int, Int>, Spot?>> { it.key.first }.thenBy { it.key.second })
            .key
        var facing = Facing.RIGHT

        fun move() {
            position = when (facing) {
                Facing.UP -> sequence {
                    yield(position.copy(first = position.first - 1))
                    yieldAll(generateSequence(position.copy(first = maxRow)) { it.copy(first = it.first - 1) })
                }

                Facing.DOWN -> sequence {
                    yield(position.copy(first = position.first + 1))
                    yieldAll(generateSequence(position.copy(first = minRow)) { it.copy(first = it.first + 1) })
                }

                Facing.LEFT -> sequence {
                    yield(position.copy(second = position.second - 1))
                    yieldAll(generateSequence(position.copy(second = maxColumn)) { it.copy(second = it.second - 1) })
                }

                Facing.RIGHT -> sequence {
                    yield(position.copy(second = position.second + 1))
                    yieldAll(generateSequence(position.copy(second = minColumn)) { it.copy(second = it.second + 1) })
                }
            }.first { board[it] != null }
                .takeIf { board[it] == Spot.OPEN }
                ?: position
        }

        fun turnLeft() {
            facing = when (facing) {
                Facing.RIGHT -> Facing.UP
                Facing.DOWN -> Facing.RIGHT
                Facing.LEFT -> Facing.DOWN
                Facing.UP -> Facing.LEFT
            }
        }

        fun turnRight() {
            facing = when (facing) {
                Facing.RIGHT -> Facing.DOWN
                Facing.DOWN -> Facing.LEFT
                Facing.LEFT -> Facing.UP
                Facing.UP -> Facing.RIGHT
            }
        }

        pathGrammar.parseToEnd(directionInput).forEach {
            when (it) {
                is Direction.Move -> repeat(it.amount) { move() }
                Direction.Left -> turnLeft()
                Direction.Right -> turnRight()
            }
        }

        return (1000 * position.first) + (4 * position.second) + facing.score
    }

    fun part2(input: String): Int {
        val (boardInput, directionInput) = input.split("\n\n")
        return 0
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readText("Day22_test")
    check(part1(testInput) == 6032)
    check(part2(testInput) == 5031)

    val input = readText("Day22")
    with(part1(input)) {
        check(this == 1428)
        println(this)
    }
    with(part2(input)) {
//        check(this == 569)
        println(this)
    }
}
