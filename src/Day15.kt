import kotlin.math.absoluteValue
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

private data class Coord(
    val x: Long,
    val y: Long,
) {
    fun tuningFrequency() = (x * 4000000) + y
}

private data class Sensor(
    val x: Long,
    val y: Long,
    val maxDistance: Long,
) {
    operator fun contains(point: Coord): Boolean {
        return (point.x - x).absoluteValue + (point.y - y).absoluteValue <= maxDistance
    }

    fun pointsJustOutOfRange(maxCoordinate: Long): Sequence<Coord> = sequence {
        val coordRange = 0..maxCoordinate
        fun Sequence<Coord>.limits() = take(maxDistance.toInt())
            .dropWhile { it.x !in coordRange || it.y !in coordRange }
            .takeWhile { it.x in coordRange && it.y in coordRange }
        yieldAll(generateSequence(seed = Coord(x = x + maxDistance + 1, y = y)) { Coord(x = it.x - 1, y = it.y + 1) }.limits())
        yieldAll(generateSequence(seed = Coord(x = x, y = y + maxDistance + 1)) { Coord(x = it.x - 1, y = it.y - 1) }.limits())
        yieldAll(generateSequence(seed = Coord(x = x - maxDistance - 1, y = y)) { Coord(x = it.x + 1, y = it.y - 1) }.limits())
        yieldAll(generateSequence(seed = Coord(x = x, y = y - maxDistance - 1)) { Coord(x = it.x + 1, y = it.y + 1) }.limits())
    }
}

fun main() {
    fun part1(input: List<String>, y: Long): Int {
        return input.map { it.split("=", ",", ":").mapNotNull(String::toLongOrNull) }
            .flatMap { (sx, sy, bx, by) ->
                val maxDistance = (sx - bx).absoluteValue + (sy - by).absoluteValue
                val yDistance = (y - sy).absoluteValue
                val maxXDistance = maxDistance - yDistance
                (sx - maxXDistance).rangeTo(sx + maxXDistance).filterNot { (it to y) == (bx to by) }
            }.toSet().size
    }

    fun part2(input: List<String>, maxCoordinate: Long): Long {
        val sensors = input.map { it.split("=", ",", ":").mapNotNull(String::toLongOrNull) }
                .map { (sx, sy, bx, by) -> Sensor(x = sx, y = sy, maxDistance = (sx - bx).absoluteValue + (sy - by).absoluteValue) }
        return sensors.asSequence().flatMap { it.pointsJustOutOfRange(maxCoordinate) }
            .first { point -> sensors.none { point in it } }
            .tuningFrequency()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day15_test")
    check(part1(testInput, y = 10) == 26)
    check(part2(testInput, maxCoordinate = 20) == 56000011L)

    val input = readLines("Day15")
    with(part1(input, y = 2000000)) {
        check(this == 5511201)
        println(this)
    }
    with(part2(input, maxCoordinate = 4000000)) {
        check(this == 11318723411840)
        println(this)
    }
}
