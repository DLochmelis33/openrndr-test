import org.openrndr.Fullscreen
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ShadeStyle
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.noise.random
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Shape
import org.openrndr.shape.compound
import kotlin.random.Random

fun main() = application {
    configure {
        width = 1280
        height = 720
    }

    program {
        fun shifted(shift: Double, block: () -> Unit) {
            drawer.translate(-shift, -shift)
            block()
            drawer.translate(shift, shift)
        }

        data class YYinfo(
            val rad: Double,
            val rate: Double,
            val xc: Double,
            val yc: Double,
            val rc: Double,
            val ratec: Double,
            val offc: Double
        )

        val cfactor = 1.5
        val yys = IntRange(0, 70).map {
            YYinfo(
                rad = random(30.0, 70.0),
                rate = random(-50.0, 200.0),
                xc = if (Random.nextBoolean())
                    random(-width * cfactor, -width / cfactor)
                else
                    random(width / cfactor, width * cfactor),
                yc = if (Random.nextBoolean())
                    random(-height * cfactor, -height / cfactor)
                else
                    random(height / cfactor, height * cfactor),
                rc = random(0.0, width * 1.5),
                ratec = random(5.0, 20.0),
                offc = random(0.0, 360.0)
            )
        }.toList()

        extend {

            shadeStyle {

            }

            fun drawYingYang(xc: Double, yc: Double, rad: Double, rate: Double) {
                val half = compound {
                    difference {
                        union {
                            difference {
                                shape(Circle(0.0, 0.0, rad).shape)
                                shape(Rectangle(-rad, 0.0, 2.0 * rad, rad).shape)
                                shape(Circle(rad / 2.0, 0.0, rad / 2.0).shape)
                            }
                            shape(Circle(-rad / 2.0, 0.0, rad / 2.0).shape)
                        }
                    }
                }
                val ang = seconds * rate
                with(drawer) {
                    // outline
                    fill = ColorRGBa.BLACK
                    circle(xc, yc, rad + 4)

                    stroke = null
                    translate(xc, yc)
                    rotate(ang)

                    fill = ColorRGBa.WHITE
                    shapes(half)
                    circle(rad / 2.0, 0.0, rad / 4.0)

                    fill = ColorRGBa.BLACK
                    circle(-rad / 2.0, 0.0, rad / 4.0)

                    // revert axis changes
                    rotate(-ang)
                    translate(-xc, -yc)
                }
            }

            drawer.clear(ColorRGBa.fromHex("253031"))
            drawer.translate(width / 2.0, height / 2.0)

            for (info in yys) {
                val angc = seconds * info.ratec + info.offc
                with(drawer) {
                    translate(info.xc, info.yc)
                    rotate(angc)
                    drawYingYang(info.rc, 0.0, info.rad, info.rate)
                    rotate(-angc)
                    translate(-info.xc, -info.yc)
                }
            }

            drawYingYang(0.0, 0.0, 200.0, -40.0)

        }
    }
}