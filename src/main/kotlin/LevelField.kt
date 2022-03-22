import org.apache.commons.math3.complex.Complex
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.math.*
import kotlin.random.Random

fun main() = application {
    configure {
        width = 800
        height = 600
    }
    program {
        fun resolveColor(z: Complex): ColorRGBa {
            // < |f(z)|, |f'(z)| >
            fun func(z: Complex): Pair<Double, Double> {
                val f = { x: Double, y: Double ->
                    withRng(-2.0, 2.0) {
                        rng * sin(x * rng) + rng * cos(y * rng) + rng * sin(rng * y) * cos(rng * x)
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
            val mod = width * 1e-3
//            val mod = ((dv / 10.0).pow(2) + 0.1) * 5.0

//            println("$z    $v    ${v % mod}")

            val eps = 4e-5 * width
            return if (abs(v % mod) < eps) ColorRGBa.PINK else ColorRGBa.BLACK
        }

        val rp = RealPx(this, 0.0, 0.0, width / 10.0)
        val mimg = ImageWrapper(width, height)
        val exec = Executors.newFixedThreadPool(12)
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

