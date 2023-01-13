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

    var height by Delegates.vetoable( initialValue = Row(0)) { _, old, new -> old < new }
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
    val height: Int,
    val iterations: Int,
)

fun main() {
    fun part1(input: String): Long {
        val jetPattern = input.map { char ->
            when (char) {
                '>' -> Jet.RIGHT
                '<' -> Jet.LEFT
                else -> error("$char not recognised")
            }
        }
        val jets = generateSequence { jetPattern }.flatten().iterator()
        val chamber = Chamber()

        generateSequence { Rock.values().asList() }.flatten().take(2022).forEach { rock ->
            generateSequence(chamber.startPoint()) { point ->
                val jetPoint = point.copy(column = point.column + jets.next().columnDelta)
                    .takeIf { newPoint -> rock.coordinates(newPoint).none { chamber.isSolid(it) } }
                    ?: point

                jetPoint.copy(row = point.row + (-1).rd)
                    .takeIf { newPoint -> rock.coordinates(newPoint).none { chamber.isSolid(it) } }
                    ?: null.also { rock.coordinates(jetPoint).forEach { chamber.setSolid(it) } }
            }.last()
        }

        return chamber.height.value
    }

    fun part2(input: String): Long {
        val jetPattern: Iterable<IndexedValue<(UShort) -> UShort>> = input.map { char ->
            when (char) {
                '>' -> { uShort: UShort -> uShort.rotateRight(bitCount = 1) }
                '<' -> { uShort: UShort -> uShort.rotateLeft(bitCount = 1) }
                else -> error("$char not recognised")
            }
        }.withIndex()
        val jets = generateSequence { jetPattern }.flatten().iterator()
        val floor:UShort = 0b1_1111111_1u
        val chamber = mutableListOf(floor)
        val emptyRow:UShort = 0b1_0000000_1u
        fun List<UShort>.emptyRows() = takeLastWhile { it == emptyRow }.count()
        fun MutableList<UShort>.pad() {
            repeat(7 - emptyRows()) { add(emptyRow) }
        }
        fun List<UShort>.height() = size - emptyRows()
        fun MutableList<UShort>.startRow() = size - 4
        chamber.pad()

        val across = listOf<UShort>(
            0b0_0011110_0u,
        ).reversed()
        val plus = listOf<UShort>(
            0b0_0001000_0u,
            0b0_0011100_0u,
            0b0_0001000_0u,
        ).reversed()
        val arrow = listOf<UShort>(
            0b0_0000100_0u,
            0b0_0000100_0u,
            0b0_0011100_0u,
        ).reversed()
        val down = listOf<UShort>(
            0b0_0010000_0u,
            0b0_0010000_0u,
            0b0_0010000_0u,
            0b0_0010000_0u,
        ).reversed()
        val square = listOf<UShort>(
            0b0_0011000_0u,
            0b0_0011000_0u,
        ).reversed()

        val blocks = listOf(across, plus, arrow, down, square).withIndex()

        val seenStates = mutableMapOf<State, HeightState>()

        var heightToAdd = 0L
        var notSkipped = true

        var iterationsRemaining = 1000000000000
        var lastJetIndex: Int = -1
        generateSequence { blocks }.flatten().takeWhile { --iterationsRemaining > 0 }.forEachIndexed { index, (blockIndex, block) ->
            val startRow = chamber.startRow()
            if (notSkipped) {
                seenStates.putIfAbsent(State(blockIndex, lastJetIndex), HeightState(chamber.height(), index))
                    ?.let { (prevHeight, prevIterations) ->
                        val cyclesRemaining = iterationsRemaining / (index - prevIterations)
                        heightToAdd = cyclesRemaining * (chamber.height() + prevHeight)
                        iterationsRemaining %= (index - prevIterations)
                        notSkipped = false
                    }
            }
            generateSequence(FallingBlock(startRow, block)) { fallingRock ->
                val (row, rock) = fallingRock
                val (jetIndex, jet) = jets.next()
                lastJetIndex = jetIndex
                val postJetRock = rock.map(jet)
                    .takeIf { newRock ->
                        chamber.subList(row, row + block.size)
                            .zip(newRock) { c, b -> c.inv() or b.inv() }
                            .all { it == UShort.MAX_VALUE }
                    } ?: block

                val nextRow = row - 1
                postJetRock.takeIf { newRock ->
                    chamber.subList(nextRow, nextRow + block.size)
                        .zip(newRock) { c, b -> c.inv() or b.inv() }
                        .all { it == UShort.MAX_VALUE }
                }?.let { fallingRock.apply {
                    this.row = nextRow
                    this.block = it
                } } ?: null.also {
                    for ((bi, ci) in (row until (row + block.size)).withIndex()) {
                        chamber[ci] = chamber[ci] or postJetRock[bi]
                        chamber.pad()
                    }
                }
            }.last()
        }

        return heightToAdd + chamber.height()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readText("Day17_test")
    check(part1(testInput) == 3068L)
    println(part2(testInput))
    check(part2(testInput) == 1514285714288)

    val input = readText("Day17")
    with(part1(input)) {
        check(this == 3235L)
        println(this)
    }
    with(part2(input)) {
//        check(this == 569)
        println(this)
    }
}
