private data class Valve(
    val name: String,
    val flowRate: Int,
    val adjacentValves: List<String>,
)

private data class OneState(
    val currentValve: Valve,
    val openValves: Set<Valve> = emptySet(),
    val pressureReleased: Int = 0,
)

private data class TwoState(
    val myValve: Valve,
    val elephantValve: Valve,
    val closedValves: List<Valve>,
    val pressureReleased: Int = 0,
)

private fun parseValves(input: List<String>) = input.map { line ->
    val values = line.split(
        "Valve ",
        " has flow rate=",
        "; tunnels lead to valves ",
        "; tunnel leads to valve ",
        ", ",
    ).drop(1)
    Valve(
        name = values[0],
        flowRate = values[1].toInt(),
        adjacentValves = values.drop(2),
    )
}.associateBy { it.name }

fun main() {

    fun part1(input: List<String>): Int {
        val valves = parseValves(input)

        val maxFlowRate = valves.maxOf { (_, valve) -> valve.flowRate }

        fun OneState.explore(): List<OneState> = currentValve.adjacentValves
            .map { copy(currentValve = valves.getValue(it)) }

        return (29 downTo 1).fold(
            initial = setOf(OneState(currentValve = valves.getValue("AA")))
        ) { states, minutesRemaining ->

            fun OneState.openCurrentValve() : OneState? = if (currentValve.flowRate == 0 || currentValve in openValves) {
                null
            } else {
                copy(
                    openValves = openValves + currentValve,
                    pressureReleased = pressureReleased + (currentValve.flowRate * minutesRemaining),
                )
            }

            fun Set<OneState>.cull() : List<OneState> {
                val targetPressureReleased = maxOf { it.pressureReleased }
                val possibleExtraPressureReleased = (minutesRemaining downTo 1 step 2).sum() * maxFlowRate
                return filter { it.pressureReleased + possibleExtraPressureReleased >= targetPressureReleased }
            }

            states.cull()
                .flatMap { state -> (state.explore() + state.openCurrentValve()).filterNotNull() }
                .toSet()
        }.maxOf { it.pressureReleased }
    }

    fun part2(input: List<String>): Int {
        val valves = parseValves(input)

        val maxFlowRate = valves.maxOf { (_, valve) -> valve.flowRate }

        fun TwoState.weExplore(): List<TwoState> = myValve.adjacentValves
            .map { valves.getValue(it) }
            .flatMap { newMyValve ->
                elephantValve.adjacentValves
                    .map { valves.getValue(it) }
                    .map { newElephantValve ->
                        copy(
                            myValve = newMyValve,
                            elephantValve = newElephantValve,
                        )
                    }

            }

        val aa = valves.getValue("AA")
        return (25 downTo 1).fold(
            initial = setOf(TwoState(myValve = aa, elephantValve = aa, closedValves = valves.values.filter { it.flowRate > 0 }.sortedByDescending { it.flowRate }))
        ) { states, minutesRemaining ->

            fun TwoState.openMyValve() : List<TwoState> = if (myValve !in closedValves) {
                emptyList()
            } else {
                elephantValve.adjacentValves
                    .map { valves.getValue(it) }
                    .map { newElephantValve ->
                        copy(
                            closedValves = closedValves - myValve,
                            pressureReleased = pressureReleased + (myValve.flowRate * minutesRemaining),
                            elephantValve = newElephantValve,
                        )
                    }
            }

            fun TwoState.openElephantValve() : List<TwoState> = if (elephantValve !in closedValves) {
                emptyList()
            } else {
                myValve.adjacentValves
                    .map { valves.getValue(it) }
                    .map { newMyValve ->
                        copy(
                            closedValves = closedValves - elephantValve,
                            pressureReleased = pressureReleased + (elephantValve.flowRate * minutesRemaining),
                            myValve = newMyValve,
                        )
                    }
            }

            fun TwoState.openBothValves(): List<TwoState> = if (elephantValve == myValve || myValve !in closedValves || elephantValve !in closedValves) {
                emptyList()
            } else {
                listOf(
                    copy(
                        closedValves = closedValves - elephantValve - myValve,
                        pressureReleased = pressureReleased + ((elephantValve.flowRate + myValve.flowRate) * minutesRemaining),
                    )
                )
            }

            fun Set<TwoState>.cull() : List<TwoState> {
                val targetPressureReleased = maxOf { it.pressureReleased }
                return filter {
                    val possibleExtraPressureReleased = (minutesRemaining downTo 1 step 2).mapIndexed { index, i -> ((it.closedValves.getOrNull(index * 2)?.flowRate ?: 0) + (it.closedValves.getOrNull((index * 2) + 1)?.flowRate ?: 0)) * i }.sum()
                    it.pressureReleased + possibleExtraPressureReleased >= targetPressureReleased
                }
            }

            states.cull()
                .flatMap { state -> state.weExplore() + state.openMyValve() + state.openElephantValve() + state.openBothValves() }
                .toSet()
        }.maxOf { it.pressureReleased }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day16_test")
    check(part1(testInput) == 1651)
    println("Test: " + part2(testInput))
    check(part2(testInput) == 1707)

    val input = readLines("Day16")
    with(part1(input)) {
        check(this == 1673)
        println(this)
    }
    with(part2(input)) {
        check(this == 2343)
        println(this)
    }
}
