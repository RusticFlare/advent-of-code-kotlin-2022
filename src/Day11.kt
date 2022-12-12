private class Monkey(
    val items: MutableList<Long>,
    val operation: (Long) -> Long,
    val test: Long,
    val ifTrue: Int,
    val ifFalse: Int,
) {
    var inspected : Long = 0
}

private fun monkeys(input: String) = input.split("\n\n").map { it.lines() }.map { monkey ->
    Monkey(
        items = monkey[1].dropWhile { !it.isDigit() }.split(", ").map { it.toLong() }.toMutableList(),
        operation = monkey[2].split(" ").takeLast(2).let { (op, value) ->
            when (op) {
                "+" -> { old -> old + (value.toLongOrNull() ?: old) }
                "*" -> { old -> old * (value.toLongOrNull() ?: old) }
                else -> error(op)
            }
        },
        test = monkey[3].split(" ").last().toLong(),
        ifTrue = monkey[4].split(" ").last().toInt(),
        ifFalse = monkey[5].split(" ").last().toInt(),
    )
}

fun main() {

    fun part1(input: String): Long {
        val monkeys = monkeys(input)
        repeat(times = 20) {
            monkeys.forEach { monkey ->
                generateSequence { monkey.items.removeFirstOrNull() }.forEach { item ->
                    monkey.inspected++
                    val worryLevel = monkey.operation(item) / 3
                    monkeys[if (worryLevel % monkey.test == 0L) monkey.ifTrue else monkey.ifFalse].items.add(worryLevel)
                }
            }
        }
        return monkeys.map { it.inspected }.sortedDescending().take(2).reduce(Long::times)
    }

    fun part2(input: String): Long {
        val monkeys = monkeys(input)
        val calmFactor = monkeys.map { it.test }.distinct().reduce(Long::times)
        repeat(times = 10000) {
            monkeys.forEach { monkey ->
                generateSequence { monkey.items.removeFirstOrNull() }.forEach { item ->
                    monkey.inspected++
                    val worryLevel = monkey.operation(item) % calmFactor
                    monkeys[if (worryLevel % monkey.test == 0L) monkey.ifTrue else monkey.ifFalse].items.add(worryLevel)
                }
            }
        }
        return monkeys.map { it.inspected }.sortedDescending().take(2).reduce(Long::times)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readText("Day11_test")
    check(part1(testInput) == 10605L)
    check(part2(testInput) == 2713310158)

    val input = readText("Day11")
    with(part1(input)) {
        check(this == 182293L)
        println(this)
    }
    with(part2(input)) {
        check(this == 54832778815)
        println(this)
    }
}
