fun main() {
    fun part1(input: List<String>): Int {
        return input.count { line ->
            val (a, b, x, y) = line.split(',', '-').map { it.toInt() }
            (a <= x && y <= b) || (x <= a && b <= y)
        }
    }

    fun part2(input: List<String>): Int {
        return input.count { line ->
            val (x, y) = line.split(',', '-').map { it.toInt() }.chunked(2) { (a, b) -> a..b }
            (x intersect y).isNotEmpty()
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day04_test")
    check(part1(testInput) == 2)
    check(part2(testInput) == 4)

    val input = readLines("Day04")
    check(part1(input) == 569)
    println(part1(input))
    check(part2(input) == 936)
    println(part2(input))
}
