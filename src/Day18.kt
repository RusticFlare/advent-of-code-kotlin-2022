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
            .distinct()
            .forEach { point ->
                point.adjacent().forEach { if (it in points) surfaceArea -= 1 else surfaceArea += 1 }
                points.add(point)
            }
        return surfaceArea
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day18_test")
    check(part1(testInput) == 64)
//    check(part2(testInput) == 4)

    val input = readLines("Day18")
    with(part1(input)) {
        check(this == 4302)
        println(this)
    }
//    with(part2(input)) {
//        check(this == 569)
//        println(this)
//    }
}
