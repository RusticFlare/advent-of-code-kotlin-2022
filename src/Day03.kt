fun main() {
    val alphabet = "abcdefghijklmnopqrstuvwxyz"
    val score = (" " + alphabet + alphabet.uppercase()).toList()
    fun part1(input: List<String>): Int {
        return input.sumOf {
            val mid = it.length / 2
            val char = (it.substring(0 until mid).toSet() intersect it.substring(startIndex = mid).toSet())
                .single()
            score.indexOf(char)
        }
    }

    fun part2(input: List<String>): Int {
        return input.chunked(3)
            .sumOf { (a, b, c) -> score.indexOf((a.toSet() intersect b.toSet() intersect c.toSet()).single()) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day03_test")
    check(part1(testInput) == 157)
    check(part2(testInput) == 70)

    val input = readLines("Day03")
    check(part1(input) == 7980)
    println(part1(input))
    check(part2(input) == 2881)
    println(part2(input))
}
