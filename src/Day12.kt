import kotlin.properties.Delegates



@JvmInline value class Height(private val height: Int) : Comparable<Height> {
    override fun compareTo(other: Height) = this.height.compareTo(other.height)
}

@JvmInline value class Distance(private val distance: Int) : Comparable<Distance> {
    override fun compareTo(other: Distance) = this.distance.compareTo(other.distance)
}


private class Point(val height: Height) {
    var distance: Distance by Delegates.vetoable(initialValue = Distance(Int.MAX_VALUE)) { _, old, new -> new < old }
}

fun main() {
    fun part1(input: List<String>): Int {
        var start by Delegates.notNull<Pair<Int, Int>>()
        var end by Delegates.notNull<Pair<Int, Int>>()
        val heights = input.mapIndexed { row, line ->
            line.mapIndexed { column, height ->
                if (height.isLowerCase()) {
                    height.code - 'a'.code
                } else if (height == 'S') {
                    start = row to column
                    'a'.code - 'a'.code
                } else /* if (height == 'E') */ {
                    end = row to column
                    'z'.code - 'a'.code
                }
            }
        }
        val distances = heights.map { it.map { Int.MAX_VALUE }.toMutableList() }
        distances[start.first][start.second] = 0
        val pointQueue = mutableListOf(start)
        generateSequence { pointQueue.removeFirstOrNull() }.forEach { (row, column) ->
            val height = heights[row][column]
            val distance = distances[row][column]
            listOf(
                (row + 1) to column,
                (row - 1) to column,
                row to (column + 1),
                row to (column - 1),
            ).filter { distances.getOrNull(it.first)?.getOrNull(it.second) != null }
                .filter { height + 1 >= heights[it.first][it.second] }
                .filter { distance + 1 < distances[it.first][it.second] }
                .forEach {
                    distances[it.first][it.second] = distance + 1
                    pointQueue.add(it)
                }
        }
        return distances[end.first][end.second]
    }

    fun part2(input: List<String>): Int {
        var start by Delegates.notNull<Pair<Int, Int>>()
        var end by Delegates.notNull<Pair<Int, Int>>()
        val heights = input.mapIndexed { row, line ->
            line.mapIndexed { column, height ->
                if (height.isLowerCase()) {
                    height.code - 'a'.code
                } else if (height == 'S') {
                    start = row to column
                    'a'.code - 'a'.code
                } else /* if (height == 'E') */ {
                    end = row to column
                    'z'.code - 'a'.code
                }
            }
        }
        val distances = heights.map { it.map { Int.MAX_VALUE }.toMutableList() }
        val zeros = heights.withIndex().flatMap { (row, line) -> line.withIndex().filter { (_, height) -> height == 0 }.map { (column) -> row to column } }
        zeros.forEach { distances[it.first][it.second] = 0 }
        val pointQueue = zeros.toMutableList()
        generateSequence { pointQueue.removeFirstOrNull() }.forEach { (row, column) ->
            val height = heights[row][column]
            val distance = distances[row][column]
            listOf(
                (row + 1) to column,
                (row - 1) to column,
                row to (column + 1),
                row to (column - 1),
            ).filter { distances.getOrNull(it.first)?.getOrNull(it.second) != null }
                .filter { height + 1 >= heights[it.first][it.second] }
                .filter { distance + 1 < distances[it.first][it.second] }
                .forEach {
                    distances[it.first][it.second] = distance + 1
                    pointQueue.add(it)
                }
        }
        return distances[end.first][end.second]
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day12_test")
    check(part1(testInput) == 31)
    check(part2(testInput) == 29)

    val input = readLines("Day12")
    with(part1(input)) {
        check(this == 394)
        println(this)
    }
    with(part2(input)) {
        check(this == 388)
        println(this)
    }
}
