import Traveling.*
import java.util.*
import kotlin.math.absoluteValue

@JvmInline
private value class Minute(val value: UInt) : Comparable<Minute> {
    operator fun plus(other: Minute) = Minute(value + other.value)
    operator fun inc() = Minute(value.inc())
    operator fun dec() = Minute(value.dec())
    override fun compareTo(other: Minute) = value.compareTo(other.value)
}

@JvmInline
private value class X(val x: UInt) {
    operator fun inc() = X(x.inc())
    operator fun dec() = X(x.dec())
    infix fun distanceTo(other: X) = (x.toInt() - other.x.toInt()).absoluteValue.toUInt()
}

@JvmInline
private value class Y(val y: UInt) {
    operator fun inc() = Y(y.inc())
    operator fun dec() = Y(y.dec())
    infix fun distanceTo(other: Y) = (y.toInt() - other.y.toInt()).absoluteValue.toUInt()
}

private data class XRange(val xRange: UIntRange) {
    val first = X(xRange.first)
    val last = X(xRange.last)

    operator fun contains(x: X) = x.x in xRange
}

private data class YRange(val yRange: UIntRange) {
    val first = Y(yRange.first)
    val last = Y(yRange.last)

    operator fun contains(y: Y) = y.y in yRange
}

private data class Valley(val xRange: XRange, val yRange: YRange) {
    val entrance = Location(xRange.first, yRange.first.dec())
    val exit = Location(xRange.last, yRange.last.inc())

    operator fun contains(location: Location) =
        (location.x in xRange && location.y in yRange) || location == entrance || location == exit
}

private data class Location(val x: X, val y: Y) {
    val up get() = copy(y = y.dec())
    val down get() = copy(y = y.inc())
    val left get() = copy(x = x.dec())
    val right get() = copy(x = x.inc())

    infix fun distanceTo(other: Location) = (x distanceTo other.x) + (y distanceTo other.y)
}

private enum class Traveling { UP, DOWN, LEFT, RIGHT }

private data class Blizzard(val location: Location, val traveling: Traveling) {
    fun move(valley: Valley) = copy(
        location = when (traveling) {
            UP -> location.up.takeIf { it in valley } ?: location.copy(y = valley.yRange.last)
            DOWN -> location.down.takeIf { it in valley } ?: location.copy(y = valley.yRange.first)
            LEFT -> location.left.takeIf { it in valley } ?: location.copy(x = valley.xRange.last)
            RIGHT -> location.right.takeIf { it in valley } ?: location.copy(x = valley.xRange.first)
        }
    )
}

private data class PartyState(
    val minute: Minute,
    val location: Location,
    val destination: Location,
) : Comparable<PartyState> {
    val bestPossibleResult = minute + Minute(location distanceTo destination)
    override fun compareTo(other: PartyState) = comparator.compare(this, other)

    companion object {
        val comparator = compareBy<PartyState> { it.bestPossibleResult }
    }
}

private data class PartyState2(
    val minute: Minute,
    val location: Location,
    val destinations: List<Location>,
) : Comparable<PartyState2> {
    val bestPossibleResult =
        minute + Minute(location distanceTo (destinations.firstOrNull() ?: location)) +
                Minute(destinations.zipWithNext { a, b -> a distanceTo b }.sum())

    override fun compareTo(other: PartyState2) = comparator.compare(this, other)

    companion object {
        val comparator = compareBy<PartyState2> { it.bestPossibleResult }
    }
}

fun main() {

    fun part1(input: List<String>): Int {
        val valley = Valley(
            xRange = with(input.first().indices) {
                XRange(first.toUInt().inc()..last.toUInt().dec())
            },
            yRange = with(input.indices) {
                YRange(first.toUInt().inc()..last.toUInt().dec())
            },
        )
        val initialMinute = Minute(0u)
        val blizzardStates = mutableMapOf<Minute, Pair<Collection<Blizzard>, Set<Location>>>().apply {
            this[initialMinute] = input.flatMapIndexed { y, s ->
                s.mapIndexedNotNull { x, c ->
                    when (c) {
                        '>' -> RIGHT
                        '^' -> UP
                        'v' -> DOWN
                        '<' -> LEFT
                        else -> null
                    }?.let { traveling ->
                        Blizzard(
                            location = Location(X(x.toUInt()), Y(y.toUInt())),
                            traveling = traveling,
                        )
                    }
                }
            }.let { it to it.map { blizzard -> blizzard.location }.toSet() }
        }
        val queue = PriorityQueue<PartyState>().apply {
            add(
                PartyState(
                    minute = initialMinute,
                    location = valley.entrance,
                    destination = valley.exit
                )
            )
        }

        while (queue.isNotEmpty()) {
            val state = queue.poll()
            if (state.location == state.destination) {
                return state.minute.value.toInt()
            }
            val nextMinute = state.minute.inc()
            val (_, blizzardLocations) = blizzardStates.computeIfAbsent(nextMinute) {
                println(it)
                val (blizzards) = blizzardStates.getValue(state.minute)
                blizzards.map { blizzard -> blizzard.move(valley) }
                    .let { newBlizzards -> newBlizzards to newBlizzards.map { blizzard -> blizzard.location }.toSet() }
            }
            queue.addAll(
                listOfNotNull(
                    state.copy(minute = nextMinute),
                    runCatching { state.copy(minute = nextMinute, location = state.location.up) }.getOrNull(),
                    state.copy(minute = nextMinute, location = state.location.down),
                    state.copy(minute = nextMinute, location = state.location.left),
                    state.copy(minute = nextMinute, location = state.location.right),
                ).filter { it.location in valley && it.location !in blizzardLocations && it !in queue }
            )
        }
        error("No path found")
    }

    fun part2(input: List<String>): Int {
        val valley = Valley(
            xRange = with(input.first().indices) {
                XRange(first.toUInt().inc()..last.toUInt().dec())
            },
            yRange = with(input.indices) {
                YRange(first.toUInt().inc()..last.toUInt().dec())
            },
        )
        val initialMinute = Minute(0u)
        val blizzardStates = mutableMapOf<Minute, Pair<Collection<Blizzard>, Set<Location>>>().apply {
            this[initialMinute] = input.flatMapIndexed { y, s ->
                s.mapIndexedNotNull { x, c ->
                    when (c) {
                        '>' -> RIGHT
                        '^' -> UP
                        'v' -> DOWN
                        '<' -> LEFT
                        else -> null
                    }?.let { traveling ->
                        Blizzard(
                            location = Location(X(x.toUInt()), Y(y.toUInt())),
                            traveling = traveling,
                        )
                    }
                }
            }.let { it to it.map { blizzard -> blizzard.location }.toSet() }
        }
        val queue = PriorityQueue<PartyState2>().apply {
            add(
                PartyState2(
                    minute = initialMinute,
                    location = valley.entrance,
                    destinations = listOf(valley.exit, valley.entrance, valley.exit),
                )
            )
        }

        while (queue.isNotEmpty()) {
            val state = queue.poll()
            val nextMinute = state.minute.inc()
            val (_, blizzardLocations) = blizzardStates.computeIfAbsent(nextMinute) {
                println(it)
                val (blizzards) = blizzardStates.getValue(state.minute)
                blizzards.map { blizzard -> blizzard.move(valley) }
                    .let { newBlizzards -> newBlizzards to newBlizzards.map { blizzard -> blizzard.location }.toSet() }
            }
            queue.addAll(
                listOfNotNull(
                    state.copy(minute = nextMinute),
                    runCatching { state.copy(minute = nextMinute, location = state.location.up) }.getOrNull(),
                    runCatching { state.copy(minute = nextMinute, location = state.location.down) }.getOrNull(),
                    state.copy(minute = nextMinute, location = state.location.left),
                    state.copy(minute = nextMinute, location = state.location.right),
                ).filter { it.location in valley && it.location !in blizzardLocations && it !in queue }
                    .map {
                        when (it.location) {
                            it.destinations.first() -> it.copy(destinations = it.destinations.drop(1))
                                .also { state -> if (state.destinations.isEmpty()) return state.minute.value.toInt() }

                            else -> it
                        }
                    }
            )
        }
        error("No path found")
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day24_test")
    check(part1(testInput) == 18)
    check(part2(testInput) == 54)

    val input = readLines("Day24")
    with(part1(input)) {
        check(this == 305)
        println(this)
    }
    with(part2(input)) {
        check(this == 905)
        println(this)
    }
}
