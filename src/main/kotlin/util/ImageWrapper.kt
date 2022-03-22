package util

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.ColorFormat
import org.openrndr.draw.colorBuffer
import java.nio.ByteBuffer
import kotlin.math.roundToInt

class ImageWrapper(val width: Int, val height: Int) {

    private val cb = colorBuffer(width, height, format = ColorFormat.RGBa)
    private val colorCount by cb.format::componentCount
    private val colorSize by cb.type::componentSize
    private val buf = ByteBuffer.allocateDirect(
        cb.width * cb.height * cb.format.componentCount * cb.type.componentSize // w * h * 3 * 1
    )

    operator fun get(j: Int, i: Int): ColorRGBa {
        val offset = colorCount * ((height - i) * width + j)
        val rByte = buf.get(offset)
        val gByte = buf.get(offset + colorSize)
        val bByte = buf.get(offset + 2 * colorSize)
        val aByte = buf.get(offset + 3 * colorSize)
        return ColorRGBa(rByte / 255.0, gByte / 255.0, bByte / 255.0, aByte / 255.0)
    }

    operator fun set(j: Int, i: Int, c: ColorRGBa) {
        fun Double.conv() = (this * 255.0).roundToInt().toByte()
        val offset = colorCount * ((height - i) * width + j)
        buf.put(offset, c.r.conv())
        buf.put(offset + colorSize, c.g.conv())
        buf.put(offset + 2 * colorSize, c.b.conv())
        buf.put(offset + 3 * colorSize, c.a.conv())
    }

    fun toColorBuffer(): ColorBuffer {
        cb.write(buf)
        return cb
    }

}
