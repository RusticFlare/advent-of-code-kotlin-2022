private data class Pos(val row: Int, val col: Int) {
    val n get() = copy(row = row - 1)
    val ne get() = copy(row = row - 1, col = col + 1)
    val e get() = copy(col = col + 1)
    val se get() = copy(row = row + 1, col = col + 1)
    val s get() = copy(row = row + 1)
    val sw get() = copy(row = row + 1, col = col - 1)
    val w get() = copy(col = col - 1)
    val nw get() = copy(row = row - 1, col = col - 1)
}

private interface CardinalDirection {

    val next: CardinalDirection

    fun moveTo(pos: Pos, positions: Set<Pos>): Pos?

    object North : CardinalDirection {
        override val next = South

        override fun moveTo(pos: Pos, positions: Set<Pos>) = pos.n
            .takeUnless { pos.n in positions }
            .takeUnless { pos.ne in positions }
            .takeUnless { pos.nw in positions }
    }

    object South : CardinalDirection {
        override val next = West

        override fun moveTo(pos: Pos, positions: Set<Pos>) = pos.s
            .takeUnless { pos.s in positions }
            .takeUnless { pos.se in positions }
            .takeUnless { pos.sw in positions }
    }

    object West : CardinalDirection {
        override val next = East

        override fun moveTo(pos: Pos, positions: Set<Pos>) = pos.w
            .takeUnless { pos.w in positions }
            .takeUnless { pos.nw in positions }
            .takeUnless { pos.sw in positions }
    }

    object East : CardinalDirection {
        override val next = North

        override fun moveTo(pos: Pos, positions: Set<Pos>) = pos.e
            .takeUnless { pos.e in positions }
            .takeUnless { pos.ne in positions }
            .takeUnless { pos.se in positions }
    }
}

private fun Set<Pos>.next(cardinalDirection: CardinalDirection): Set<Pos> {
    val directions = generateSequence(cardinalDirection) { it.next }.take(4).toList()
    return groupBy { pos ->
        pos.takeUnless {
            it.n in this ||
                    it.ne in this ||
                    it.e in this ||
                    it.se in this ||
                    it.s in this ||
                    it.sw in this ||
                    it.w in this ||
                    it.nw in this
        } ?: directions.firstNotNullOfOrNull { it.moveTo(pos, positions = this) }
        ?: pos
    }
        .flatMap { (destination, starts) ->
            when {
                starts.size > 1 -> starts
                else -> listOf(destination)
            }
        }.toSet()
}

private fun Set<Pos>.print(index: Int) {
    val rows = -2..9
    val cols = -3..10
    println("== End of Round ${index + 1} ==")
    rows.forEach { row -> println(cols.joinToString(separator = "") { if (Pos(row, it) in this) "#" else "." }) }
    println()
}

fun main() {
    fun part1(input: List<String>): Int {
        var positions = input.flatMapIndexed { row, s ->
            s.withIndex().filter { (_, char) -> char == '#' }.map { (col) -> Pos(row, col) }
        }.toSet()
        generateSequence(CardinalDirection.North as CardinalDirection) { it.next }
            .take(10)
            .forEachIndexed { index, cardinalDirection ->
                positions = positions.next(cardinalDirection) // .also { it.print(index) }
            }
        return ((positions.maxOf { it.row } - positions.minOf { it.row } + 1) *
                (positions.maxOf { it.col } - positions.minOf { it.col } + 1)) - positions.size
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day23_test")
    val part1 = part1(testInput)
    println(part1)
    check(part1 == 110)
//    check(part2(testInput) == 4)

    val input = readLines("Day23")
    with(part1(input)) {
        check(this == 4254)
        println(this)
    }
//    with(part2(input)) {
//        check(this == 569)
//        println(this)
//    }
}
