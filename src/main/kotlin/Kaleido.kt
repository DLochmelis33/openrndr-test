import org.openrndr.Fullscreen
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadImage
import org.openrndr.draw.shadeStyle
import org.openrndr.math.Vector2
import org.openrndr.shape.IntRectangle
import org.openrndr.shape.Triangle
import kotlin.math.sqrt

fun main() = application {
    configure {
        width = 1920
        height = 1080
        fullscreen = Fullscreen.SET_DISPLAY_MODE
    }
    program {
        // acceptable image:
        //    / \
        //   / ё \
        //  /_____\

        val img = loadImage("images/a.png")
        val imgw: Double = img.width.toDouble()
        val cos30: Double = sqrt(3.0) / 2.0
        val sin30 = 0.5

        extend {
            val cropped = img.crop(IntRectangle(0, (30 * seconds).toInt(), imgw.toInt(), (imgw * cos30).toInt()))

            val shs1 = shadeStyle {
                fragmentTransform = """
                        vec2 texCoord = c_boundsPosition.xy;
                        texCoord.y = 1.0 - texCoord.y;
                        vec2 size = textureSize(p_image, 0);
                        // texCoord.x /= size.x/size.y;
                        x_fill = texture(p_image, texCoord);
                    """
                parameter("image", cropped)
            }
            val shs2 = shadeStyle {
                fragmentTransform = """
                        vec2 texCoord = c_boundsPosition.xy;
                        texCoord.y = 1.0 - texCoord.y;
                        texCoord.x = 1.0 - texCoord.x;
                        vec2 size = textureSize(p_image, 0);
                        // texCoord.x /= size.x/size.y;
                        x_fill = texture(p_image, texCoord);
                    """
                parameter("image", cropped)
            }

            val shape = Triangle(
                Vector2(imgw / 2.0, 0.0),
                Vector2(0.0, imgw * cos30),
                Vector2(imgw, imgw * cos30),
            ).contour.shape
            drawer.translate(
                width / 2.0, height / 2.0
            )
            drawer.fill = ColorRGBa.WHITE
            drawer.circle(0.0, 0.0, 10.0)

            for (a in -5..5) {
                for (b in -5..5) {

                    val tvec = Vector2(
                        (a * imgw) + (b * imgw * sin30),
                        -b * imgw * cos30
                    )

                    val rot = (a - b) * 120.0
                    val otherot = 300.0 - rot

                    val centerVec = Vector2(imgw / 2.0, imgw * (cos30 - sqrt(3.0) / 6.0))
                    val shiftVec = Vector2(imgw / 2.0, imgw * sqrt(3.0) * (0.5 - 2.0 / 3.0))

                    drawer.translate(tvec)
                    with(drawer) {
                        // shape 1
                        shadeStyle = shs1

                        translate(centerVec)
                        rotate(rot)
                        translate(-centerVec)

                        shape(shape)

                        translate(centerVec)
                        rotate(-rot)
                        translate(-centerVec)

                        // shape 2
                        shadeStyle = shs2
                        translate(shiftVec)

                        translate(centerVec)
                        rotate(otherot)
                        translate(-centerVec)

                        shape(shape)

                        translate(centerVec)
                        rotate(-otherot)
                        translate(-centerVec)

                        translate(-shiftVec)
                    }
                    drawer.translate(-tvec)
                }
            } // end kaleido for

        }
    }
}
