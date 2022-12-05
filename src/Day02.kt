fun main() {
    fun part1(input: List<String>): Int {
        return input.sumOf {
            when (it.last()) {
                'X' -> 1 + when (it.first()) {
                    'A' -> 3
                    'B' -> 0
                    'C' -> 6
                    else -> error("first: ${it.first()}")
                }
                'Y' -> 2 + when (it.first()) {
                    'A' -> 6
                    'B' -> 3
                    'C' -> 0
                    else -> error("first: ${it.first()}")
                }
                'Z' -> 3 + when (it.first()) {
                    'A' -> 0
                    'B' -> 6
                    'C' -> 3
                    else -> error("first: ${it.first()}")
                }
                else -> error("last: ${it.last()}")
            }
        }
    }

    fun part2(input: List<String>): Int {
        return input.sumOf {
            when (it.last()) {
                'X' -> 0 + when (it.first()) {
                    'A' -> 3
                    'B' -> 1
                    'C' -> 2
                    else -> error("first: ${it.first()}")
                }
                'Y' -> 3 + when (it.first()) {
                    'A' -> 1
                    'B' -> 2
                    'C' -> 3
                    else -> error("first: ${it.first()}")
                }
                'Z' -> 6 + when (it.first()) {
                    'A' -> 2
                    'B' -> 3
                    'C' -> 1
                    else -> error("first: ${it.first()}")
                }
                else -> error("last: ${it.last()}")
            }
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day02_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 12)

    val input = readLines("Day02")
    check(part1(input) == 11767)
    println(part1(input))
    check(part2(input) == 13886)
    println(part2(input))
}
