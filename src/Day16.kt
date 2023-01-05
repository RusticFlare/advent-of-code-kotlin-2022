data class Valve(
    val name: String,
    val flowRate: Int,
    val adjacentValves: List<String>,
)

data class State(
    val currentValve: Valve,
    val openValves: Set<Valve> = emptySet(),
    val pressureReleased: Int = 0,
)

fun main() {
    fun part1(input: List<String>): Int {
        val valves = input.map { line ->
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

        val maxFlowRate = valves.maxOf { (_, valve) -> valve.flowRate }

        fun State.explore(): List<State> = currentValve.adjacentValves
            .map { copy(currentValve = valves.getValue(it)) }

        return (29 downTo 1).fold(
            initial = setOf(State(currentValve = valves.getValue("AA")))
        ) { states, minutesRemaining ->

            fun State.openCurrentValve() : State? = if (currentValve.flowRate == 0 || currentValve in openValves) {
                null
            } else {
                copy(
                    openValves = openValves + currentValve,
                    pressureReleased = pressureReleased + (currentValve.flowRate * minutesRemaining),
                )
            }

            fun Set<State>.cull() : List<State> {
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
        return input.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day16_test")
    check(part1(testInput) == 1651)
//    check(part2(testInput) == 4)

    val input = readLines("Day16")
    with(part1(input)) {
        check(this == 1673)
        println(this)
    }
//    with(part2(input)) {
//        check(this == 569)
//        println(this)
//    }
}
