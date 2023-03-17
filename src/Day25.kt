import kotlin.math.max

private data class CarryOverState(val previousDigit: Int, val carryOver: Int) {
    init {
        require(previousDigit in Snafu.validDigits) { "Invalid previousValue <$previousDigit>" }
    }

    companion object {
        val INITIAL = CarryOverState(previousDigit = 0, carryOver = 0)
    }
}

private fun List<Int>.padZeros(newSize: Int) = plus(List(newSize - size) { 0 })

private data class Snafu(val digits: List<Int>) {
    init {
        require(digits.all { it in validDigits }) { digits }
    }

    operator fun plus(other: Snafu): Snafu {
        val zipSize = max(digits.size, other.digits.size)
        val carryOverStates = digits.padZeros(zipSize).zip(other.digits.padZeros(zipSize), transform = Int::plus)
            .runningFold(CarryOverState.INITIAL) { (_, carryOver), digit ->
                var value = digit + carryOver
                var toCarryOver = 0
                while (value !in validDigits) {
                    when {
                        value < validDigits.first -> {
                            value += 5
                            toCarryOver -= 1
                        }

                        value > validDigits.last -> {
                            value -= 5
                            toCarryOver += 1
                        }
                    }
                }
                CarryOverState(previousDigit = value, carryOver = toCarryOver)
            }.drop(1)
        return Snafu(
            carryOverStates.map { it.previousDigit } + carryOverStates.last().carryOver
        )
    }

    override fun toString(): String = digits.reversed()
        .dropWhile { it == 0 }
        .joinToString(separator = "") {
            when (it) {
                0, 1, 2 -> it.toString()
                -1 -> "-"
                -2 -> "="
                else -> error(it)
            }
        }

    companion object {
        val validDigits = -2..2

        fun parse(input: String) = Snafu(
            input.reversed().map {
                it.digitToIntOrNull() ?: when (it) {
                    '-' -> -1
                    '=' -> -2
                    else -> error(it)
                }
            }
        )
    }
}

fun main() {
    fun part1(input: List<String>): String {
        return input.map { Snafu.parse(it) }
            .reduce(Snafu::plus)
            .toString()
    }

    fun part2(input: List<String>): Int {
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day25_test")
    check(part1(testInput) == "2=-1=0")
//    check(part2(testInput) == 4)

    val input = readLines("Day25")
    with(part1(input)) {
        check(this == "2-2--02=1---1200=0-1")
        println(this)
    }
//    with(part2(input)) {
//        check(this == 569)
//        println(this)
//    }
}
