import org.openrndr.Fullscreen
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.hsv
import org.openrndr.draw.LineCap
import org.openrndr.ffmpeg.ScreenRecorder
import kotlin.math.*

fun tgsin(x: Double) = 1.0 / (exp(-7.0 * cos(x)) + 1)

fun frac(x: Double) = x - floor(x)

fun fracSmoothTaylor(x: Double) =
    IntRange(1, 4).map { k -> sin(2 * k.toDouble() * PI * x) / k.toDouble() }.sum() / PI + 0.5

fun fracSmoothCustom(x: Double): Double {
    val ratio = 0.8
    val t = x - floor(x)
    if (t > ratio) {
        val tmp = ratio / (1.0 - ratio)
        return (tmp - tmp * t) / ratio
    }
    return t / ratio
}


fun f(block: () -> Unit) {
    block()
}


fun main() = application {

    configure {
        width = 1920
        height = 1080
        fullscreen = Fullscreen.SET_DISPLAY_MODE
    }
    program {

//        extend(ScreenRecorder())

        val barCount = 120
        val angPart = 360 / barCount.toDouble()

        val funcList = listOf(
            { t: Double -> cos(t) },
            { t: Double -> fracSmoothCustom(t / PI * 0.5) },
            { t: Double -> tgsin(t) },
            { t: Double -> sin(t) + sin(3.0 * t) / 2.0 },
            { t: Double -> cos(2.0 * t).pow(3) }
        )
//        val funcList = IntRange(1, 4).map { { t: Double -> cos(t) } }
        val radiiList = listOf(
            100.0, 200.0, 300.0, 490.0, 690.0
        )
        val dlList = listOf(
            90.0, 90.0, 90.0, 90.0, 100.0
        )

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.translate(width / 2.0, height / 2.0)

            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = null
            drawer.circle(0.0, 0.0, 2.0)

            drawer.strokeWeight = 1.3
            drawer.lineCap = LineCap.ROUND

            for (i in 0 until barCount) {
                drawer.rotate(angPart)

                drawer.stroke = hsv(360.0 * i.toDouble() / barCount.toDouble() - seconds * 20.0, 1.0, 1.0).toRGBa()

                val arg = i.toDouble() / barCount.toDouble() * 2.0 * PI
                val timeShift = seconds * 1.5

                for (t in funcList.indices) {
                    val movarg = arg * (t.toDouble() + 3) + timeShift // + seconds * t.toDouble() * -1.4
                    drawer.lineSegment(0.0, radiiList[t], 0.0, radiiList[t] + funcList[t](movarg) * dlList[t])
                }
            }
//            мурино центральная 9
        }
    }
}

