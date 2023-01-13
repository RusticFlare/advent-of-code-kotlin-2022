fun main() {
    fun part1(input: List<String>): Int {
        val points = mutableSetOf<Triple<Int, Int, Int>>()
        var surfaceArea = 0
        fun Triple<Int, Int, Int>.adjacent() = listOf(
            copy(first = first + 1),
            copy(first = first - 1),
            copy(second = second + 1),
            copy(second = second - 1),
            copy(third = third + 1),
            copy(third = third - 1),
        )
        input.asSequence()
            .map { it.split(",").map(String::toInt) }
            .map { (x, y, z) -> Triple(x, y, z) }
            .forEach { point ->
                point.adjacent().forEach { if (it in points) surfaceArea-- else surfaceArea++ }
                points += point
            }
        return surfaceArea
    }

    fun part2(input: List<String>): Int {
        var surfaceArea = 0
        val points = input.asSequence()
            .map { it.split(",").map(String::toInt) }
            .map { (x, y, z) -> Triple(x, y, z) }
            .toSet()
        val xRange = (points.minOf { (x) -> x } - 1)..(points.maxOf { (x) -> x } + 1)
        val yRange = (points.minOf { (_, y) -> y } - 1)..(points.maxOf { (_, y) -> y } + 1)
        val zRange = (points.minOf { (_, _, z) -> z } - 1)..(points.maxOf { (_, _, z) -> z } + 1)
        val visited = mutableSetOf<Triple<Int, Int, Int>>()
        fun Triple<Int, Int, Int>.adjacent() = listOfNotNull(
            (first + 1).takeIf { it in xRange }?.let { copy(first = it) },
            (first - 1).takeIf { it in xRange }?.let { copy(first = it) },
            (second + 1).takeIf { it in yRange }?.let { copy(second = it) },
            (second - 1).takeIf { it in yRange }?.let { copy(second = it) },
            (third + 1).takeIf { it in zRange }?.let { copy(third = it) },
            (third - 1).takeIf { it in zRange }?.let { copy(third = it) },
        ).filterNot { it in visited }
            .filterNot { (it in points).also { border -> if (border) surfaceArea++ } }
        val toVisit = mutableListOf(Triple(xRange.first, yRange.first, zRange.first))
        generateSequence { toVisit.removeFirstOrNull() }
            .filter(visited::add)
            .flatMap { it.adjacent() }
            .forEach(toVisit::add)
        return surfaceArea
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day18_test")
    check(part1(testInput) == 64)
    check(part2(testInput) == 58)

    val input = readLines("Day18")
    with(part1(input)) {
        check(this == 4302)
        println(this)
    }
    with(part2(input)) {
        check(this == 2492)
        println(this)
    }
}
