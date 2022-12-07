import Filesystem.Directory

private sealed interface Filesystem {

    val size: Int

    data class Directory(
        val name: String,
        val parent: Directory?,
        val files: MutableList<Filesystem> = mutableListOf(),
    ) : Filesystem {

        override val size: Int
            get() = files.sumOf { it.size }
    }

    data class File(
        val name: String,
        override val size: Int,
    ) : Filesystem
}

private fun Directory.deepFlatten():List<Directory> = files
    .filterIsInstance<Directory>()
    .flatMap { it.deepFlatten() } + this

fun main() {
    fun filesystem(input: List<String>): Directory {
        val root = Directory(name = "/", parent = null)
        var currentDirectory: Directory = root
        input.forEach { line ->
            if (line.startsWith("$ cd")) {
                currentDirectory = when (val target = line.split(" ")[2]) {
                    ".." -> currentDirectory.parent!!
                    "/" -> root
                    else -> currentDirectory.files.filterIsInstance<Directory>().singleOrNull { it.name == target }
                        ?: error("$currentDirectory does not contain $target")
                }
            } else if (!line.startsWith("$ ls")) {
                val (x, name) = line.split(" ")
                if (x == "dir") {
                    currentDirectory.files.add(Directory(name, parent = currentDirectory))
                } else {
                    currentDirectory.files.add(Filesystem.File(name, size = x.toInt()))
                }
            }
        }
        return root
    }

    fun part1(input: List<String>): Int {
        return filesystem(input).deepFlatten()
            .map { it.size }
            .filter { it <= 100000 }
            .sum()
    }

    fun part2(input: List<String>): Int {
        val totalDiskSpace = 70000000
        val requiredUnusedSpace = 30000000

        val filesystem = filesystem(input)

        val currentUnusedSpace = totalDiskSpace - filesystem.size

        return filesystem.deepFlatten().map { it.size }
            .filter { currentUnusedSpace + it >= requiredUnusedSpace }
            .min()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day07_test")
    check(part1(testInput) == 95437)
    check(part2(testInput) == 24933642)

    val input = readLines("Day07")
    check(part1(input) == 1391690)
    println(part1(input))
    check(part2(input) == 5469168)
    println(part2(input))
}
