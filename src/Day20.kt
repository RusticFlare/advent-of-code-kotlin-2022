fun main() {

    fun day20(input: List<String>, decryptionKey: Long, repeat: Int): Long {
        val encryptedFile = input.map { it.toInt() * decryptionKey }.withIndex().toMutableList()

        repeat(repeat) {
            input.indices.forEach { initialIndex ->
                val currentIndex = encryptedFile.indexOfFirst { it.index == initialIndex }
                val indexedValue = encryptedFile.removeAt(currentIndex)
                encryptedFile.add(
                    index = (currentIndex + indexedValue.value).mod(encryptedFile.size),
                    element = indexedValue,
                )
            }
        }

        val decryptedFile = encryptedFile.map { it.value }
        val indexOfZero = decryptedFile.indexOf(0)
        return decryptedFile[(indexOfZero + 1000).mod(decryptedFile.size)] +
                decryptedFile[(indexOfZero + 2000).mod(decryptedFile.size)] +
                decryptedFile[(indexOfZero + 3000).mod(decryptedFile.size)]
    }

    fun part1(input: List<String>): Long {
        return day20(input, decryptionKey = 1, repeat = 1)
    }

    fun part2(input: List<String>): Long {
        return day20(input, decryptionKey = 811589153, repeat = 10)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day20_test")
    check(part1(testInput) == 3L)
    check(part2(testInput) == 1623178306L)

    val input = readLines("Day20")
    with(part1(input)) {
        check(this == 23321L)
        println(this)
    }
    with(part2(input)) {
        check(this == 1428396909280)
        println(this)
    }
}
