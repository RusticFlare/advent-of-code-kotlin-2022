private sealed class Either {
    data class Left(val value: Long) : Either()
    data class Right(val value: Long) : Either()

    companion object {
        fun fromNullables(left: Long?, right: Long?) = when {
            left != null && right != null -> error("left: $left, right: $right")
            left != null -> Left(left)
            right != null -> Right(right)
            else -> error("both null")
        }
    }
}

private inline fun <R> Either.fold(
    onLeft: (Long) -> R,
    onRight: (Long) -> R,
): R = when (this) {
    is Either.Left -> onLeft(value)
    is Either.Right -> onRight(value)
}

private sealed class MonkeyShout {

    abstract fun value(monkeys: Map<String, MonkeyShout>): Long?

    abstract fun findHumn(monkeys: Map<String, MonkeyShout>, target: Long): Long

    data class Value(val value: Long) : MonkeyShout() {
        override fun value(monkeys: Map<String, MonkeyShout>): Long = value
        override fun findHumn(monkeys: Map<String, MonkeyShout>, target: Long) = error("findHumn() on $this")
    }

    sealed class Operation : MonkeyShout() {

        abstract val left: String
        abstract val right: String

        abstract fun operation(a: Long, b: Long): Long
        abstract fun findA(total: Long, b: Long): Long
        abstract fun findB(total: Long, a: Long): Long

        final override fun value(monkeys: Map<String, MonkeyShout>): Long? {
            return monkeys[left]?.value(monkeys)?.let { a ->
                monkeys[right]?.value(monkeys)?.let { b ->
                    operation(a, b)
                }
            }
        }

        final override fun findHumn(monkeys: Map<String, MonkeyShout>, target: Long): Long {
            return Either.fromNullables(
                left = monkeys[left]?.value(monkeys),
                right = monkeys[right]?.value(monkeys),
            ).fold(
                onLeft = { a ->
                    val b = findB(total = target, a = a)
                    monkeys[right]?.findHumn(monkeys, target = b) ?: b
                },
                onRight = { b ->
                    val a = findA(total = target, b = b)
                    monkeys[left]?.findHumn(monkeys, target = a) ?: a
                },
            )
        }

        data class Add(override val left: String, override val right: String) : Operation() {
            override fun operation(a: Long, b: Long) = a + b
            override fun findA(total: Long, b: Long) = total - b
            override fun findB(total: Long, a: Long) = total - a
        }

        data class Subtract(override val left: String, override val right: String) : Operation() {
            override fun operation(a: Long, b: Long) = a - b
            override fun findA(total: Long, b: Long) = total + b
            override fun findB(total: Long, a: Long) = a - total
        }

        data class Times(override val left: String, override val right: String) : Operation() {
            override fun operation(a: Long, b: Long) = a * b
            override fun findA(total: Long, b: Long) = total / b
            override fun findB(total: Long, a: Long) = total / a
        }

        data class Divide(override val left: String, override val right: String) : Operation() {
            override fun operation(a: Long, b: Long) = a / b
            override fun findA(total: Long, b: Long) = total * b
            override fun findB(total: Long, a: Long) = a / total
        }
    }

    data class Equals(val left: String, val right: String) : MonkeyShout() {
        override fun value(monkeys: Map<String, MonkeyShout>) = error("value() on Equals")

        override fun findHumn(monkeys: Map<String, MonkeyShout>, target: Long): Long {
            return Either.fromNullables(
                left = monkeys[left]?.value(monkeys),
                right = monkeys[right]?.value(monkeys),
            ).fold(
                onLeft = { a -> monkeys[right]?.findHumn(monkeys, target = a) ?: a },
                onRight = { b -> monkeys[left]?.findHumn(monkeys, target = b) ?: b },
            )
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
        return checkNotNull(monkeys.getValue("root").value(monkeys)) { "Part 1 is null" }
    }

    fun part2(input: List<String>): Long {
        val monkeys = input.map { it.split(": ") }
            .associate { (monkey, monkeyShout) -> monkey to monkeyShout.toMonkeyShout() }
            .toMutableMap()
            .apply {
                val root = getValue("root") as MonkeyShout.Operation
                this["root"] = MonkeyShout.Equals(left = root.left, right = root.right)
                remove("humn")
            }.toMap()
        return checkNotNull(monkeys.getValue("root").findHumn(monkeys, target = 0)) { "Part 2 is null" }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day21_test")
    check(part1(testInput) == 152L)
    check(part2(testInput) == 301L)

    val input = readLines("Day21")
    with(part1(input)) {
        check(this == 169525884255464)
        println(this)
    }
    with(part2(input)) {
        check(this == 3247317268284)
        println(this)
    }
}
