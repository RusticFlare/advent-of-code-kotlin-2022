import kotlin.properties.Delegates

private val Int.rd get() = RowDelta(this.toLong())
private val Int.cd get() = ColumnDelta(this)
private val Int.r get() = Row(this.toLong())
private val Int.c get() = Column(this)
private fun delta(rowDelta: RowDelta, columnDelta: ColumnDelta) = CoordinateDelta(rowDelta, columnDelta)

@JvmInline
private value class RowDelta(val delta: Long)

@JvmInline
private value class ColumnDelta(val delta: Int)

private data class CoordinateDelta(val rowDelta: RowDelta, val columnDelta: ColumnDelta)

@JvmInline
private value class Row(val value: Long) : Comparable<Row> {
    override fun compareTo(other: Row): Int  = this.value.compareTo(other.value)
}

@JvmInline
private value class Column(val value: Int) : Comparable<Column> {
    override fun compareTo(other: Column): Int  = this.value.compareTo(other.value)
}

private data class Coordinate(val row: Row, val column: Column)

private operator fun Row.plus(other: RowDelta) = Row(value + other.delta)
private operator fun Column.plus(other: ColumnDelta) = Column(value + other.delta)
private operator fun Coordinate.plus(other: CoordinateDelta) = Coordinate(row + other.rowDelta, column + other.columnDelta)


private enum class Rock(val solidPoints: List<CoordinateDelta>) {
    ACROSS(solidPoints = listOf(
        delta(0.rd, 0.cd), delta(0.rd, 1.cd), delta(0.rd, 2.cd), delta(0.rd, 3.cd),
    )),
    PLUS(solidPoints = listOf(
                           delta(2.rd, 1.cd),
        delta(1.rd, 0.cd), delta(1.rd, 1.cd), delta(1.rd, 2.cd),
                           delta(0.rd, 1.cd),
    )),
    ARROW(solidPoints = listOf(
                                              delta(2.rd, 2.cd),
                                              delta(1.rd, 2.cd),
        delta(0.rd, 0.cd), delta(0.rd, 1.cd), delta(0.rd, 2.cd),
    )),
    DOWN(solidPoints = listOf(
        delta(3.rd, 0.cd),
        delta(2.rd, 0.cd),
        delta(1.rd, 0.cd),
        delta(0.rd, 0.cd),
    )),
    SQUARE(solidPoints = listOf(
        delta(1.rd, 0.cd), delta(1.rd, 1.cd),
        delta(0.rd, 0.cd), delta(0.rd, 1.cd),
    )),;

    fun coordinates(coordinate: Coordinate) = solidPoints.map { coordinate + it }
}

private class Chamber {

    var height by Delegates.vetoable( initialValue = 0.r) { _, old, new -> old < new }
        private set

    private val solids = mutableSetOf<Coordinate>()

    fun startPoint() = Coordinate(height + startingRowDelta, startingColumn)

    fun isSolid(coordinate: Coordinate) =
        coordinate.row <= floorRow || coordinate.column.value !in validColumns || coordinate in solids

    fun setSolid(coordinate: Coordinate) {
        require(coordinate.row > floorRow) { "$coordinate's row is not positive" }
        require(coordinate.column.value in validColumns) { "$coordinate's column is not in $validColumns" }
        require(solids.add(coordinate)) { "$coordinate already in $solids" }
        height = coordinate.row
    }

    private companion object {
        val validColumns = 1..7
        val floorRow = 0.r
        val startingColumn = 3.c
        val startingRowDelta = 4.rd
    }
}

private enum class Jet(val columnDelta: ColumnDelta) {
    LEFT((-1).cd), RIGHT(1.cd),
}

private data class FallingBlock(
    var row: Int,
    var block: List<UShort>,
)

private data class State(
    val blockIndex: Int,
    val flowIndex: Int,
)

private data class HeightState(
    val height: Row,
    val iterations: Int,
)

fun main() {
    fun day17(input: String, iterations: Long): Long {
        var iterationsRemaining = iterations
        val jetPattern = input.map { char ->
            when (char) {
                '>' -> Jet.RIGHT
                '<' -> Jet.LEFT
                else -> error("$char not recognised")
            }
        }.withIndex()
        val jets = generateSequence { jetPattern }.flatten().iterator()
        val chamber = Chamber()
        val seenStates = mutableMapOf<State, HeightState>()
        var lookForCycle = false
        var lastJetIndex = -1
        var heightToAdd = 0L

        val indexedRocks = Rock.values().withIndex()
        generateSequence { indexedRocks }.flatten().takeWhile { iterationsRemaining-- > 0 }
            .forEachIndexed { index, (rockIndex, rock) ->
                if (index == 500) {
                    lookForCycle = true
                }
                if (lookForCycle) {
                    seenStates.putIfAbsent(State(rockIndex, lastJetIndex), HeightState(chamber.height, index))
                        ?.let { (prevHeight, prevIndex) ->
                            val cycleLength = index - prevIndex
                            val cyclesRemaining = iterationsRemaining / cycleLength
                            iterationsRemaining %= cycleLength
                            heightToAdd = (chamber.height.value - prevHeight.value) * cyclesRemaining
                            lookForCycle = false
                        }
                }

                generateSequence(chamber.startPoint()) { point ->
                    val (jetIndex, value) = jets.next()
                    lastJetIndex = jetIndex
                    val jetPoint = point.copy(column = point.column + value.columnDelta)
                        .takeIf { newPoint -> rock.coordinates(newPoint).none { chamber.isSolid(it) } }
                        ?: point

                    jetPoint.copy(row = point.row + (-1).rd)
                        .takeIf { newPoint -> rock.coordinates(newPoint).none { chamber.isSolid(it) } }
                        ?: null.also { rock.coordinates(jetPoint).forEach { chamber.setSolid(it) } }
                }.last()
            }

        return chamber.height.value + heightToAdd
    }

    fun part1(input: String): Long {
        return day17(input, iterations = 2022)
    }

    fun part2(input: String): Long {
        return day17(input, iterations = 1000000000000)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readText("Day17_test")
    check(part1(testInput) == 3068L) { "Part 1 should be 3068, was ${part1(testInput)}" }
    check(part2(testInput) == 1514285714288) { "Part 2 should be 1514285714288, was ${part2(testInput)}" }

    val input = readText("Day17")
    with(part1(input)) {
        check(this == 3235L)
        println(this)
    }
    with(part2(input)) {
        check(this == 1591860465110)
        println(this)
    }
}
