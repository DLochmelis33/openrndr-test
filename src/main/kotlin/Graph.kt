class Graph(graph: MutableMap<Int, MutableList<Int>>) {

    val g: Map<Int, List<Int>> = graph

    fun getNeighs(v: Int): List<Int> = g[v] ?: listOf()

}
