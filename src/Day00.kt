fun main() {
    fun part1(input: List<String>): Int {
        return input.size
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day00_test")
    check(part1(testInput) == 2)
//    check(part2(testInput) == 4)

    val input = readLines("Day00")
    with(part1(input)) {
        check(this == 569)
        println(this)
    }
//    with(part2(input)) {
//        check(this == 569)
//        println(this)
//    }
}
