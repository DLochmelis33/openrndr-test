import org.apache.commons.math3.complex.Complex
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.math.*

fun main() = application {
    configure {
        width = 1200
        height = 800
        title = "LevelField v0.1"
    }
    program {
        val unit = width / 800.0
        fun resolveColor(z: Complex): ColorRGBa {
            // < |f(z)|, |f'(z)| >
            fun func(z: Complex): Pair<Double, Double> {
                val f = { x: Double, y: Double ->
                    withRng(0.4, 1.0) {
                        rng * sin(x * rng).pow(3) + rng * cos(y * rng).pow(3) + rng * sin(rng * y) * cos(rng * x)
//                        sin(x).pow(3) + cos(y).pow(3)
                    }
                }
                val (x, y) = z
                return Pair(
                    f(x, y),
                    f.grad(x, y).norm
                )
            }

            val (v, dv) = func(z)

            // mod has to increase with dv in some way
            // lesser mod ==> more lines
            val mod = unit * 0.4
//            val mod = unit * dv * 1.1

//            println("$z    $v    ${v % mod}")

            val eps = unit * 3e-2
            return if (abs(v % mod) < eps) ColorRGBa.PINK else ColorRGBa.BLACK
        }

        val rp = RealPx(this, 0.0, 0.0, width / 15.0)
        val mimg = ImageWrapper(width, height)
        val exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        val tasks: List<Callable<Unit>> = (0 until height).map { i ->
            (0 until width).map { j ->
                Callable {
                    val (x, y) = rp.pxToReal(j, i)
                    val color = resolveColor(Complex(x, y))
                    mimg[j, i] = color
                }
            }.toList()
        }.flatten()
        exec.invokeAll(tasks)
        exec.shutdown()

        extend {
            drawer.image(mimg.toColorBuffer())
        }
    }
}

