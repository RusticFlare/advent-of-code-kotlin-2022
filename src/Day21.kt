private sealed class MonkeyShout {

    abstract fun value(monkeys: Map<String, MonkeyShout>): Long

    data class Value(val value: Long) : MonkeyShout() {
        override fun value(monkeys: Map<String, MonkeyShout>) = value
    }

    sealed class Operation : MonkeyShout() {
        abstract val left: String
        abstract val right: String
        abstract fun operation(a: Long, b: Long): Long
        final override fun value(monkeys: Map<String, MonkeyShout>): Long {
            return operation(monkeys.getValue(left).value(monkeys), monkeys.getValue(right).value(monkeys))
        }

        data class Add(override val left: String, override val right: String) : Operation() {
            override fun operation(a: Long, b: Long) = a + b
        }

        data class Subtract(override val left: String, override val right: String) : Operation() {
            override fun operation(a: Long, b: Long) = a - b
        }

        data class Times(override val left: String, override val right: String) : Operation() {
            override fun operation(a: Long, b: Long) = a * b
        }

        data class Divide(override val left: String, override val right: String) : Operation() {
            override fun operation(a: Long, b: Long) = a / b
        }
    }
}

private fun String.toMonkeyShout() = when {
    '+' in this -> MonkeyShout.Operation.Add(substring(0..3), substring(7..10))
    '-' in this -> MonkeyShout.Operation.Subtract(substring(0..3), substring(7..10))
    '*' in this -> MonkeyShout.Operation.Times(substring(0..3), substring(7..10))
    '/' in this -> MonkeyShout.Operation.Divide(substring(0..3), substring(7..10))
    else -> MonkeyShout.Value(toLong())
}

fun main() {
    fun part1(input: List<String>): Long {
        val monkeys = input.map { it.split(": ") }
            .associate { (monkey, monkeyShout) -> monkey to monkeyShout.toMonkeyShout() }
        return monkeys.getValue("root").value(monkeys)
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day21_test")
    check(part1(testInput) == 152L)
//    check(part2(testInput) == 4)

    val input = readLines("Day21")
    with(part1(input)) {
        check(this == 169525884255464)
        println(this)
    }
//    with(part2(input)) {
//        check(this == 569)
//        println(this)
//    }
}
