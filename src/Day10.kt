fun main() {
    fun part1(input: List<String>): Int {
        return input.asSequence()
            .flatMap {
                when (it) {
                    "noop" -> sequenceOf(0)
                    else -> sequenceOf(0, it.drop(5).toInt())
                }
            }.scan(initial = 1, operation = Int::plus)
            .withIndex()
            .filter { (index) -> (index + 1) % 40 == 20 }
            .sumOf { (index, value) -> (index + 1) * value }
    }

    fun part2(input: List<String>): String {
        return input.asSequence()
            .flatMap {
                when (it) {
                    "noop" -> sequenceOf(0)
                    else -> sequenceOf(0, it.drop(5).toInt())
                }
            }.scan(initial = 1, operation = Int::plus)
            .chunked(size = 40)
            .take(6)
            .joinToString(separator = System.lineSeparator()) {
                it.withIndex().joinToString(separator = "") { (index, value) -> if (index - value in -1..1) "#" else "."}
            }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day10_test")
    check(part1(testInput) == 13140)
    check(part2(testInput) == """
        ##..##..##..##..##..##..##..##..##..##..
        ###...###...###...###...###...###...###.
        ####....####....####....####....####....
        #####.....#####.....#####.....#####.....
        ######......######......######......####
        #######.......#######.......#######.....
    """.trimIndent())

    val input = readLines("Day10")
    with(part1(input)) {
        check(this == 13860)
        println(this)
    }
    with(part2(input)) {
        check(
            this == """
            ###..####.#..#.####..##....##..##..###..
            #..#....#.#..#.#....#..#....#.#..#.#..#.
            #..#...#..####.###..#.......#.#....###..
            ###...#...#..#.#....#.##....#.#....#..#.
            #.#..#....#..#.#....#..#.#..#.#..#.#..#.
            #..#.####.#..#.#.....###..##...##..###..
        """.trimIndent())
        println(this)
    }
}
