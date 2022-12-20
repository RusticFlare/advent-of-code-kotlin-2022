import kotlin.math.sign

fun main() {
    infix fun Int.towards(to: Int) = IntProgression.fromClosedRange(
        rangeStart = this,
        rangeEnd = to,
        step = (to - this).sign.takeUnless { it == 0 } ?: 1,
    )

    fun getCave(input: List<String>) =
        input.flatMap { it.splitToSequence(" -> ", ",").map(String::toInt).chunked(size = 2).zipWithNext() }
            .flatMap { (a, b) -> (a[0] towards b[0]).flatMap { x -> (a[1] towards b[1]).map { y -> x to y } } }
            .toMutableSet()

    fun MutableSet<Pair<Int, Int>>.fallingSand(maxDepth: Int) = generateSequence(seed = 500 to 0) { (x, y) ->
        sequenceOf(x, x - 1, x + 1).map { it to (y + 1) }.filterNot { it in this }.firstOrNull()
    }.take(maxDepth).last()

    fun part1(input: List<String>): Int {
        val cave = getCave(input)
        val maxDepth = cave.maxOf { it.second }

        fun MutableSet<Pair<Int, Int>>.addSand() = fallingSand(maxDepth + 1)
            .takeUnless { (_, y) -> y == maxDepth }
            ?.let { add(it) } ?: false

        return generateSequence { cave.addSand() }
            .takeWhile { it }
            .count()
    }

    fun part2(input: List<String>): Int {
        val cave = getCave(input)
        val maxDepth = cave.maxOf { it.second } + 2

        fun MutableSet<Pair<Int, Int>>.addSand() = add(fallingSand(maxDepth))

        return generateSequence { cave.addSand() }
            .takeWhile { it }
            .count()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day14_test")
    check(part1(testInput) == 24)
    check(part2(testInput) == 93)

    val input = readLines("Day14")
    with(part1(input)) {
        check(this == 698)
        println(this)
    }
    with(part2(input)) {
        check(this == 28594)
        println(this)
    }
}
