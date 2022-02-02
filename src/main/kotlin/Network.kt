import org.openrndr.Fullscreen
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.extra.noise.fastFloor
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import java.lang.IllegalStateException
import kotlin.math.pow
import kotlin.random.Random

const val NEIGH_MIN = 50
const val NEIGH_MAX = 300
const val GOERS = 40

const val PTS_COUNT = 300

const val PERIOD = 1.0
const val PERIOD_VAR = 0.3
const val SHIFT_LAG = 0.55 // threshold = 0.5

// throws if values.size == 0; this happens when a point is isolated
fun <T> Random.nextWeighted(weights: List<Number>, values: List<T>): T {
    val w: List<Double> = weights.map { i -> i.toDouble() }
    val t = this.nextDouble(w.sum())
    var s = 0.0
    for (i in w.indices) {
        s += w[i]
        if (t < s) return values[i]
    }
    throw RuntimeException("wtf")
}

fun distsToNeighs(g: Graph, v: Int, pts: List<Vector2>) = g.getNeighs(v).map { u -> pts[u].distanceTo(pts[v]) }

class Goer(private val g: Graph, private val r: Random, private val pts: List<Vector2>) {
    private var from: Int
    private var to: Int

    private var stage = 0

    private val period: Double

    init {
//        from = r.nextInt(PTS_COUNT)
        from = 1
        do {
            to = r.nextInt(PTS_COUNT)
        } while (to == from)

        period = if (PERIOD_VAR == 0.0) PERIOD else r.nextDouble(PERIOD - PERIOD_VAR, PERIOD + PERIOD_VAR)
    }

    // [0,1] -> [0,1]
    private fun trans(ph: Double): Double {
        return ph.pow(2)
    }

    private fun transStart(ph: Double): Double {
        if (ph < SHIFT_LAG) return 0.0
        return trans((ph - SHIFT_LAG) / (1.0 - SHIFT_LAG))
    }

    private fun transEnd(ph: Double): Double {
        if (ph > 1 - SHIFT_LAG) return 1.0
        return trans(ph / (1.0 - SHIFT_LAG))
    }

    fun draw(p: Program) {
        with(p) {
            val secs = seconds / period
            val st = secs.fastFloor()
            if (st > stage) {
                stage = st
                // regen from / to
                from = to
                val neighs = g.getNeighs(to)
                val dtn = distsToNeighs(g, from, pts)
                do {
                    to = r.nextWeighted(dtn, neighs)
                } while (to == from)
            }
            val ph = secs - st
            val dir = pts[to] - pts[from]

            val lineStart = pts[from] + dir * transStart(ph)
            val lineEnd = pts[from] + dir * transEnd(ph)

            drawer.lineSegment(lineStart, lineEnd)
        }
    }

}

// TODO:
// - intersecting edges map
// - cluster close points
// - fancy color changing
// - ???

fun main() = application {
    configure {
        width = 1920
        height = 1080
        fullscreen = Fullscreen.SET_DISPLAY_MODE
    }
    program {
        // rendering stuff
        val rt = renderTarget(
            width,
            height,
//            multisample = BufferMultisample.SampleCount(4)
        ) {
            colorBuffer()
        }

        // actual stuff
        val random = Random(8)

        val pts: List<Vector2> = (1..PTS_COUNT).map {
            Vector2.uniform(Rectangle(0.0, 0.0, width.toDouble(), height.toDouble()), random)
        }
        val graphTmp: MutableMap<Int, MutableList<Int>> =
            (0 until PTS_COUNT).associateWith { mutableListOf<Int>() }.toMutableMap()
        for (i in pts.indices) {
            for (j in pts.indices) {
                if (i <= j) break
                val dist = pts[i].distanceTo(pts[j])
                if (NEIGH_MIN < dist && dist < NEIGH_MAX) {
                    graphTmp[i]!!.add(j)
                    graphTmp[j]!!.add(i)
                    // making the graph directed results in isolated vertices
                }
            }
        }
        for (l in graphTmp.values) {
            if (l.isEmpty()) throw IllegalStateException("wrong generation")
        }
        val graph = Graph(graphTmp)

        val goers = (1..GOERS).map { Goer(graph, random, pts) }

        extend {
            val bgColor = ColorRGBa(0.01, 0.0, 0.05)
            val lineColor = ColorRGBa(1.0, 0.0, 0.0, 0.7)

            drawer.isolatedWithTarget(rt) {
                drawer.clear(bgColor)

                drawer.stroke = lineColor
                for (goer in goers) {
                    goer.draw(this@extend)
                }

                // graph visualization
//                for (v in 1..PTS_COUNT) {
//                    for (u in graph.getNeighs(v)) {
//                        drawer.lineSegment(pts[u], pts[v])
//                    }
//                }

                drawer.fill = ColorRGBa.WHITE
                drawer.stroke = null
                drawer.circles(pts, 3.0)

            }
            drawer.image(rt.colorBuffer(0))
        }
    }
}