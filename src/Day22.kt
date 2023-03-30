import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import java.util.*
import java.util.Objects.checkIndex

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

private class NetPosition(
    val row: Int,
    val column: Int,
) : Comparable<NetPosition> {

    override fun compareTo(other: NetPosition) = compareValuesBy(this, other, { it.row }, { it.column })
}

private data class NetState(
    val position: NetPosition,
    val facing: Facing,
) {
    fun faceState(size: Int) = Face(
        size = size, positionOfFace = Face.Position(
            row = position.row / size,
            column = position.column / size,
        )
    ).State(

    )
}

private data class Face(
    val size: Int,
    val positionOfFace: Position,
) {
    lateinit var up: Nothing
    lateinit var down: Nothing
    lateinit var left: Nothing
    lateinit var right: Nothing

    class Position(
        val row: Int,
        val column: Int,
    ) : Comparable<Position> {

        override fun compareTo(other: Position) = compareValuesBy(this, other, { it.row }, { it.column })
    }

    inner class State(
        val positionOnFace: Position,
        val facing: Facing
    ) {
        val netState
            get() = NetState(
                position = NetPosition(
                    row = (positionOfFace.row * size) + positionOnFace.row,
                    column = (positionOfFace.column * size) + positionOnFace.column,
                ),
                facing = facing
            )
    }
}


private class Cube(val size: Int) {
    lateinit var top: Face
    lateinit var bottom: Face
    lateinit var front: Face
    lateinit var back: Face
    lateinit var left: Face
    lateinit var right: Face

    data class Vector(
        val x: Int,
        val y: Int,
        val z: Int,
    )

    enum class Cardinal { X, Y, Z }

    enum class UnitVector(val vector: Vector, val cardinal: Cardinal) {
        PlusX(Vector(x = 1, y = 0, z = 0), Cardinal.X),
        PlusY(Vector(x = 0, y = 1, z = 0), Cardinal.Y),
        PlusZ(Vector(x = 0, y = 0, z = 1), Cardinal.Z),
        MinusX(Vector(x = -1, y = -0, z = -0), Cardinal.X),
        MinusY(Vector(x = -0, y = -1, z = -0), Cardinal.Y),
        MinusZ(Vector(x = -0, y = -0, z = -1), Cardinal.Z),
    }

    inner class State(
        val position: Vector,
        val direction: UnitVector,
        val side: UnitVector,
    ) {
        init {
            checkIndex(position.x, size)
            checkIndex(position.y, size)
            checkIndex(position.z, size)
            require(direction.cardinal != side.cardinal)
        }
    }
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

    fun part2(input: String, size: Int): Int {
        val (boardInput, directionInput) = input.split("\n\n")
        val net = boardInput.lines().flatMapIndexed { row, line ->
            line.mapIndexedNotNull { column, char ->
                when (char) {
                    '.' -> NetPosition(row = row, column = column) to Spot.OPEN
                    '#' -> NetPosition(row = row, column = column) to Spot.WALL
                    else -> null
                }
            }
        }.toMap()

        fun NetPosition.facePosition() = FacePosition(row = row / size, column = column / size)
        fun FacePosition.face() = Face(size = size, position = this)
        fun NetPosition.face() = facePosition().face()

        val faces = net.keys
            .associate { it.facePosition() to it.face() }
        check(faces.size == 6) { "${faces.size} faces" }

        val cube = Cube(size)

        val firstFace = faces.values.minBy { it.position }

        cube.top = firstFace



        return 0
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readText("Day22_test")
    check(part1(testInput) == 6032)
    check(part2(testInput, size = 4) == 5031) { "${part2(testInput, size = 4)} should be 5031" }

    val input = readText("Day22")
    with(part1(input)) {
        check(this == 1428)
        println(this)
    }
    with(part2(input, size = 50)) {
//        check(this == 569)
        println(this)
    }
}
