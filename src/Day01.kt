fun main() {
    fun part1(input: String): Int {
        return input.splitToSequence("\n\n")
            .maxOf { elf -> elf.lines().sumOf { it.toInt() } }
    }

    fun part2(input: String): Int {
        return input.splitToSequence("\n\n")
            .map { elf -> elf.lines().sumOf { it.toInt() } }
            .sortedDescending()
            .take(3)
            .sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readText("Day01_test")
    check(part1(testInput) == 24000)
    check(part2(testInput) == 45000)

    val input = readText("Day01")
    check(part1(input) == 68802)
    println(part1(input))
    check(part2(input) == 205370)
    println(part2(input))
}
