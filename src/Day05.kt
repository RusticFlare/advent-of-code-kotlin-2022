fun main() {
    fun part1(input: String): String {
        val (initialState, steps) = input.split("\n\n")
        val totalStacks = initialState.lines().last().split(" ").mapNotNull { it.toIntOrNull() }.last()
        val stacks = (1..totalStacks).associateWith { mutableListOf<Char>() }
        initialState.lines().forEach { line ->
            stacks.forEach { (index, stack) ->
                line.getOrNull((index * 4) - 3)?.takeIf { it.isLetter() }
                    ?.let { stack.add(it) }
            }
        }
        steps.lines().forEach { step ->
            val (move, from, to) = step.split(" ").mapNotNull { it.toIntOrNull() }
            repeat(times = move) {
                stacks.getValue(to).add(index = 0, stacks.getValue(from).removeFirst())
            }
        }
        return stacks.values.joinToString(separator = "") { it.first().toString() }
    }

    fun part2(input: String): String {
        val (initialState, steps) = input.split("\n\n")
        val totalStacks = initialState.lines().last().split(" ").mapNotNull { it.toIntOrNull() }.last()
        val stacks = (1..totalStacks)
            .associateWith { mutableListOf<Char>() }
        initialState.lines().forEach { line ->
            stacks.forEach { (index, stack) ->
                line.getOrNull((index * 4) - 3)?.takeIf { it.isLetter() }
                    ?.let { stack.add(it) }
            }
        }
        steps.lines().forEach { step ->
            val (move, from, to) = step.split(" ").mapNotNull { it.toIntOrNull() }
            stacks.getValue(to).addAll(index = 0, stacks.getValue(from).take(move))
            repeat(times = move) { stacks.getValue(from).removeFirst() }
        }
        return stacks.values.joinToString(separator = "") { it.first().toString() }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readText("Day05_test")
    check(part1(testInput) == "CMZ")
    check(part2(testInput) == "MCD")

    val input = readText("Day05")
    check(part1(input) == "GFTNRBZPF")
    println(part1(input))
    check(part2(input) == "VRQWPDSGP")
    println(part2(input))
}
