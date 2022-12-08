fun main() {
    fun part1(input: List<String>): Int {
        val forrestHeights = input.map { it.map { tree -> tree.digitToInt() } }
        return forrestHeights.withIndex().drop(1).dropLast(1).sumOf { (row, rowTreesHeights) ->
            rowTreesHeights.withIndex().drop(1).dropLast(1).count { (column, treeHeight) ->
                val columnTreesHeights = forrestHeights.map { it[column] }
                listOf(
                    rowTreesHeights.take(column),
                    rowTreesHeights.drop(column + 1),
                    columnTreesHeights.take(row),
                    columnTreesHeights.drop(row + 1),
                ).any { treeHeights -> treeHeights.all { it < treeHeight } }
            }
        } + ((forrestHeights.size + forrestHeights.size - 2) * 2)
    }

    fun part2(input: List<String>): Int {
        val forrestHeights = input.map { it.map { tree -> tree.digitToInt() } }
        return forrestHeights.withIndex().drop(1).dropLast(1).maxOf { (row, rowTreesHeights) ->
            rowTreesHeights.withIndex().drop(1).dropLast(1).maxOf { (column, treeHeight) ->
                val columnTreesHeights = forrestHeights.map { it[column] }
                listOf(
                    rowTreesHeights.take(column).reversed(),
                    rowTreesHeights.drop(column + 1),
                    columnTreesHeights.take(row).reversed(),
                    columnTreesHeights.drop(row + 1),
                ).map { treeHeights -> treeHeights.takeWhile { it < treeHeight }.size.let { if (it < treeHeights.size) it + 1 else it } }
                    .reduce(Int::times)
            }
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readLines("Day08_test")
    check(part1(testInput) == 21)
    check(part2(testInput) == 8)

    val input = readLines("Day08")
    check(part1(input) == 1825)
    println(part1(input))
    check(part2(input) == 235200)
    println(part2(input))
}
