import org.apache.commons.math3.complex.Complex
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import util.ImageWrapper
import util.RealPx
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.math.roundToInt

//fun plot(
//    f: (Double) -> Double,
//    xc: Double = 0.0,
//    yc: Double = 0.0,
//    scale: Double = 40.0,
//    weight: Double = 2.0,
//    density: Double = 1.0,
//) = application {
//    configure {
//        width = 800
//        height = 600
//    }
//    program {
//        // coords <--> pixels:
//        // x <--> j
//        // y <--> i
//        val rpconv = RealPx(whGetter = { Pair(width, height) })
//
//        val graphPoints = (0..(width * density).roundToInt()).map {
//            val j = it / density
//            val (x, _) = rpconv.pxToReal(j.toInt(), 0)
//            val y = f(x)
//            val (_, i) = rpconv.realToPx(x, y)
//            Vector2(j, i)
//        }
//        extend {
//            val oldStroke = drawer.stroke
//            drawer.stroke = null
//            drawer.circles(graphPoints, weight)
//            drawer.stroke = oldStroke
//        }
//    }
//}

fun plot(
    f: (Complex) -> Complex,
    xc: Double,
    yc: Double,
    scale: Double,
    colorResolver: (Complex) -> ColorRGBa
) = application {
    configure {
        width = 400
        height = 300
    }
    program {
        val rp = RealPx(width, height, xc, yc, scale)
        val exec = Executors.newFixedThreadPool(12)

        val mimg = ImageWrapper(width, height)

        val tasks = mutableListOf<Callable<Unit>>()
        for (i in 0 until height) {
            for (j in 0 until width) {
                tasks.add {
                    val (x, y) = rp.pxToReal(j, i)
                    val c = colorResolver(f(Complex(x, y)))
                    mimg[j, i] = c
                }
            }
        }
        exec.invokeAll(tasks)
        exec.shutdown()

        extend {
            drawer.image(mimg.toColorBuffer())
        }
    }
}

fun simpleResolver(absMax: Double): (Complex) -> ColorRGBa = { z ->
    val g = z.abs() / absMax
//    val b = z.sin().abs() / absMax
    if (z.subtract(Complex.I).abs() <= 0.3) ColorRGBa.WHITE
    else if (z.subtract(Complex.ONE).abs() <= 0.2) ColorRGBa.PINK
    else ColorRGBa(0.0, g, 0.0)
}

fun main() {
    plot({ z: Complex -> z }, 1.0, 2.0, 50.0, simpleResolver(1.0))
}