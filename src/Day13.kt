import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

private sealed class Packet : Comparable<Packet> {
    data class Value(val value: Int) : Packet() {
        override fun compareTo(other: Packet): Int {
            return when (other) {
                is Packets -> Packets(listOf(this)).compareTo(other)
                is Value -> value.compareTo(other.value)
            }
        }
    }

    data class Packets(val packets: List<Packet>): Packet() {
        override fun compareTo(other: Packet): Int {
            return when (other) {
                is Packets -> packets.zip(other.packets)
                    .firstNotNullOfOrNull { (left, right) -> left.compareTo(right).takeUnless { it == 0 } }
                    ?: packets.size.compareTo(other.packets.size)
                is Value -> this.compareTo(Packets(listOf(other)))
            }
        }
    }
}

private val packetGrammar = object : Grammar<Packet.Packets>() {
    val number by regexToken("\\d+")
    val comma by literalToken(",")
    val leftBracket by literalToken("[")
    val rightBracket by literalToken("]")

    val value by number use { Packet.Value(text.toInt()) }
    val term by value or parser { packets }
    val packets: Parser<Packet.Packets> by -leftBracket *
            (separated(term, comma, acceptZero = true) use { Packet.Packets(terms) }) *
            -rightBracket
    override val rootParser by packets
}

fun main() {

    fun part1(input: String): Int {
        return input.splitToSequence("\n\n")
            .map { pair -> pair.lines().map { packetGrammar.parseToEnd(it) } }
            .withIndex()
            .sumOf { (index, pair) -> if (pair.sorted() == pair) index + 1 else 0 }
    }

    fun part2(input: String): Int {
        val dividerPackets = setOf("[[2]]", "[[6]]").map { packetGrammar.parseToEnd(it) }.toSet()
        val packets = input.lineSequence().filter { it.isNotBlank() }.map { packetGrammar.parseToEnd(it) }
            .plus(dividerPackets)
            .sorted()
            .toList()
        return dividerPackets.map { packets.indexOf(it) + 1 }.reduce(Int::times)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readText("Day13_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 140)

    val input = readText("Day13")
    with(part1(input)) {
        check(this == 5208)
        println(this)
    }
    with(part2(input)) {
        check(this == 25792)
        println(this)
    }
}
