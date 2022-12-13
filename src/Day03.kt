fun main() {
    val alphabet = "abcdefghijklmnopqrstuvwxyz"
    val score = (" " + alphabet + alphabet.uppercase()).toList()

    fun Char.priority() = score.indexOf(this)

    fun part1(input: List<String>): Int {
        return input.sumOf {
            val mid = it.length / 2
            val first = it.substring(0 until mid).asIterable()
            val second = it.substring(startIndex = mid).toSet()
            (first intersect second).single().priority()
        }
    }

    fun part2(input: List<String>): Int {
        return input.chunked(3).sumOf { elves ->
            elves.map { it.toSet() }
                .reduce(Set<Char>::intersect)
                .single()
                .priority()
        }
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
