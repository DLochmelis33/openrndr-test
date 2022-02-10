import ch.obermuhlner.math.big.BigFloat
import ch.obermuhlner.math.big.BigFloat.*
import ch.obermuhlner.math.big.kotlin.bigfloat.*
import kotlinx.coroutines.*
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorFormat
import org.openrndr.draw.colorBuffer
import java.nio.ByteBuffer

val numContext: Context = context(150)

val bigTwo: BigFloat = numContext.valueOf(2.0)
val radlim: BigFloat = numContext.valueOf(1 shl 20)
val logtwo: BigFloat = log(bigTwo)

var zoomCount: Int = 0

fun getIter() = 50 + zoomCount * 20

fun mandelValue(cx: BigFloat, cy: BigFloat): BigFloat {
    val ITER = getIter()
    var zx = numContext.ZERO
    var zy = numContext.ZERO
    var i = 0
    while (i < ITER) {
        i++
        if (zx.isInfinity || zy.isInfinity) {
            break
        }
        if (abs(zx) + abs(zy) > radlim) {
            break
        }
        val tmp = zx + zx // mul by 2 >> sum
        zx = (zx * zx) - (zy * zy) + cx
        zy = (tmp * zy) + cy
    }
    val t: BigFloat = if (i < ITER) {
        val log_zn = log(zx * zx + zy * zy) / 2.0
        val nu = log(log_zn / logtwo) / logtwo
        -nu + 1 + i
    } else numContext.valueOf(ITER)
    return t
//    return ((zx * zx) + (zy * zy)) / (btpi)
}

var centerY: BigFloat = numContext.ZERO
var centerX: BigFloat = numContext.valueOf(-0.67)

// px_X * scale = math_X
var scale: BigFloat = numContext.valueOf(2.0 / 300.0)

fun scaleConverter(i: Int, j: Int, w: Int, h: Int): Pair<BigFloat, BigFloat> {
    val pxY = numContext.valueOf(h / 2.0 - i)
    val pxX = numContext.valueOf(j - w / 2.0)
    return Pair(centerY + (pxY * scale), centerX + (pxX * scale))
}

fun colorResolver(value: BigFloat): Triple<Byte, Byte, Byte> {
    if (value.isInfinity || value == numContext.valueOf(getIter())) {
        return Triple(0, 0, 0)
    }
    return try {
        Triple(
            ((sin(value) + 1.0) / 2.0 * 255.0).toDouble().toInt().toByte(),
            ((cos(value / 1.3) + 1.0) / 2.0 * 255.0).toDouble().toInt().toByte(),
            ((sin(sqrt(value) / 1.6) + 1.0) / 2.0 * 255.0).toDouble().toInt().toByte()
        )
    } catch (e: ArithmeticException) {
        Triple(255.toByte(), 255.toByte(), 255.toByte())
    }
}

fun update(bufs: Array<ByteBuffer>, width: Int, height: Int, myScope: CoroutineScope) {
    try {
        myScope.cancel()
    } catch (ignore: IllegalStateException) {
    }
    for (i in 0 until height) {
        myScope.launch {
            val buf = bufs[height - 1 - (i)] // is inverted for some reason
            buf.clear()
            for (j in 0 until width) {
                val (y, x) = scaleConverter(i, j, width, height)
                val man = mandelValue(x, y)
                val (b, g, r) = colorResolver(man)
                if (!isActive) break
                buf.put(b).put(g).put(r)
            }
//            println("done row $i")
        }
    }
}

fun main() = application {
    configure {
        width = 500
        height = 300
    }
    program {
        val buflen = 3 * width
        val buffer = ByteBuffer.allocateDirect(buflen * height)
        val bufs = Array<ByteBuffer>(height) { i -> buffer.slice(i * buflen, buflen) }
        val cb = colorBuffer(width, height)

        val myScope = GlobalScope // CoroutineScope(SupervisorJob() + Dispatchers.Unconfined) // this doesn't work

        update(bufs, width, height, myScope)

        mouse.buttonDown.listen {
            val i = it.position.y.toInt()
            val j = it.position.x.toInt()

            val (ny, nx) = scaleConverter(i, j, width, height)
//            println("click at $i $j")
//            println("center at $centerY $centerX")
//            println("calcs are ${height / 2.0 - i} ${j - width / 2.0}")
            println("new center at $ny $nx")
            scale /= 15.0
            centerY = numContext.valueOf(ny)
            centerX = numContext.valueOf(nx)
            zoomCount++

            update(bufs, width, height, myScope)
        }

        mouse.moved.listen {
            val i = it.position.y.toInt()
            val j = it.position.x.toInt()
            val w = width / 15.0
            val h = width / 15.0
            drawer.fill = null
            drawer.stroke = ColorRGBa.BLACK
            drawer.strokeWeight = 1.0
            drawer.rectangle(i - h / 2.0, j - w / 2.0, i + h / 2.0, j + w / 2.0)
        }

        extend {
            buffer.rewind()

            cb.write(buffer, ColorFormat.RGB)

            drawer.image(cb)
        }
    }
}
