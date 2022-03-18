import org.openrndr.application
import org.openrndr.math.Vector2
import kotlin.math.roundToInt

fun plot(
    f: (Double) -> Double,
    xc: Double = 0.0,
    yc: Double = 0.0,
    scale: Double = 40.0,
    weight: Double = 2.0,
    density: Double = 1.0,
) = application {
    configure {
        width = 800
        height = 600
    }
    program {
        // coords <--> pixels:
        // x <--> j
        // y <--> i
        fun toPx(x: Number, y: Number): Pair<Double, Double> {
            return Pair(
                ((x.toDouble() - xc) * scale + width / 2.0),
                (-(y.toDouble() - yc) * scale + height / 2.0)
            )
        }

        fun fromPx(j: Number, i: Number): Pair<Double, Double> {
            return Pair(
                (j.toDouble() - width / 2.0) / scale + xc,
                -(i.toDouble() - height / 2.0) / scale + yc
            )
        }

        val graphPoints = (0..(width * density).roundToInt()).map {
            val j = it / density
            val (x, _) = fromPx(j.toInt(), 0)
            val y = f(x)
            val (_, i) = toPx(x, y)
            Vector2(j, i)
        }
        extend {
            val oldStroke = drawer.stroke
            drawer.stroke = null
            drawer.circles(graphPoints, weight)
            drawer.stroke = oldStroke
        }
    }
}
