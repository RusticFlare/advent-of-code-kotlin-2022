fun main() {
    fun part1(input: String): Int {
        return 4 + input.asSequence()
            .windowed(size = 4)
            .indexOfFirst { it.distinct() == it }
    }

    fun part2(input: String): Int {
        return 14 + input.asSequence()
            .windowed(size = 14)
            .indexOfFirst { it.distinct() == it }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readText("Day06_test")
    check(part1(testInput) == 7)
    check(part1("bvwbjplbgvbhsrlpgdmjqwftvncz") == 5)
    check(part1("nppdvjthqldpwncqszvftbrmjlhg") == 6)
    check(part1("nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg") == 10)
    check(part1("zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw") == 11)
    check(part2(testInput) == 19)
    check(part2("bvwbjplbgvbhsrlpgdmjqwftvncz") == 23)
    check(part2("nppdvjthqldpwncqszvftbrmjlhg") == 23)
    check(part2("nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg") == 29)
    check(part2("zcfzfwzzqfrljwzlrfnpqdbhtmscgvjw") == 26)

    val input = readText("Day06")
    check(part1(input) == 1210)
    println(part1(input))
    check(part2(input) == 3476)
    println(part2(input))
}
