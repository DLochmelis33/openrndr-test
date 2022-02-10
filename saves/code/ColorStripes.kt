import org.openrndr.application
import org.openrndr.color.hsv
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import java.lang.Math.pow
import kotlin.math.*

//fun sigmoid(x: Double): Double = 1 / (exp(-x) + 1)
fun oneg(x: Double): Double = if (floor(x).toInt() and 0b1 == 0) 1.0 else -1.0 // (-1)^floor(x)

fun main() = application {
    configure {
        width = 800
        height = 600
    }

    program {
        fun rectCenter(xc: Double, yc: Double, w: Double, h: Double): Rectangle {
            return Rectangle.fromCenter(Vector2(xc, yc), w, h)
        }

        class TanhOsc(private val feel: Double, private val period: Double) {
            operator fun invoke(): Double {
                return tanh(feel * sin(seconds / period))
            }
        }

        class LinearOsc(private val period: Double = 1.0) {
            operator fun invoke(): Double {
                return (seconds / period - floor(seconds / period))
            }
        }

        class TeethOsc(private val feel: Double, private val period: Double) {
            private fun teeth(x: Double, p: Double = 2.0): Double =
                (oneg(x) + 1.0) / 2.0 - oneg(x) * (-x - floor(-x)).pow(p)

            operator fun invoke(): Double {
                return teeth(seconds / period, feel)
            }
        }

        class JumpingOsc(private val slope: Double, private val period: Double, private val offset: Double = 0.0) {
            private fun jumper(x: Double) = sin(1000 * floor(x) + offset) + floor(x)
            private fun helper(x: Double) = jumper(x) + (x - floor(x)) * slope

            operator fun invoke(): Double {
                return helper(seconds / period)
            }
        }

        val xOsc = TeethOsc(3.0, 1.7)
        val colorOsc = JumpingOsc(0.5, 1.7)

        val stripeWidth = 25
        val stripes = mutableListOf<Rectangle>()
        for (i in -2..(width / stripeWidth + 1)) {
            stripes.add(Rectangle(i * stripeWidth * 1.0, 0.0, stripeWidth * 1.0, height * 1.0))
        }
        val stripeColors = stripes.map { hsv(0.0, 0.0, 0.0) }.toMutableList()

        extend {
            with(drawer) {
                stroke = null

                val stripeInd = round(xOsc() * (stripes.size - 1)).toInt()
                val newColor = hsv(360 * colorOsc(), 1.0, 1.0)
                stripeColors[stripeInd] = newColor

                for ((r, c) in stripes.zip(stripeColors)) {
                    fill = c.toRGBa()
                    rectangle(r)
                }
            }
        }
    }
}
