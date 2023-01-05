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
    val openValves: Set<Valve> = emptySet(),
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
            initial = setOf(TwoState(myValve = aa, elephantValve = aa))
        ) { states, minutesRemaining ->

            fun TwoState.openMyValve() : List<TwoState> = if (myValve.flowRate == 0 || myValve in openValves) {
                emptyList()
            } else {
                elephantValve.adjacentValves
                    .map { valves.getValue(it) }
                    .map { newElephantValve ->
                        copy(
                            openValves = openValves + myValve,
                            pressureReleased = pressureReleased + (myValve.flowRate * minutesRemaining),
                            elephantValve = newElephantValve,
                        )
                    }
            }

            fun TwoState.openElephantValve() : List<TwoState> = if (elephantValve.flowRate == 0 || elephantValve in openValves) {
                emptyList()
            } else {
                myValve.adjacentValves
                    .map { valves.getValue(it) }
                    .map { newMyValve ->
                        copy(
                            openValves = openValves + elephantValve,
                            pressureReleased = pressureReleased + (elephantValve.flowRate * minutesRemaining),
                            myValve = newMyValve,
                        )
                    }
            }

            fun TwoState.openBothValves(): List<TwoState> = if (elephantValve == myValve || myValve.flowRate == 0 || elephantValve.flowRate == 0 || myValve in openValves || elephantValve in openValves) {
                emptyList()
            } else {
                listOf(
                    copy(
                        openValves = openValves + elephantValve + myValve,
                        pressureReleased = pressureReleased + ((elephantValve.flowRate + myValve.flowRate) * minutesRemaining),
                    )
                )
            }

            fun Set<TwoState>.cull() : List<TwoState> {
                val targetPressureReleased = maxOf { it.pressureReleased }
                val possibleExtraPressureReleased = (minutesRemaining downTo 1 step 2).sum() * maxFlowRate * 2
                return filter { it.pressureReleased + possibleExtraPressureReleased >= targetPressureReleased }
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
//        check(this == 569)
        println(this)
    }
}
