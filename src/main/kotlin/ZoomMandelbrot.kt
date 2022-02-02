import ch.obermuhlner.math.big.BigFloat
import ch.obermuhlner.math.big.BigFloat.*
import ch.obermuhlner.math.big.kotlin.bigfloat.*
import kotlinx.coroutines.*
import org.openrndr.application
import org.openrndr.draw.ColorFormat
import org.openrndr.draw.colorBuffer
import java.lang.IllegalStateException
import java.nio.ByteBuffer

const val ITER = 300
val numContext = context(100)

val bigTwo: BigFloat = numContext.valueOf(2.0)
val btpi: BigFloat = bigTwo pow ITER
val radlim = numContext.valueOf(1 shl 20)
val logtwo = log(numContext.valueOf(2.0))
val bigiter = numContext.valueOf(ITER)

fun mandelValue(cx: BigFloat, cy: BigFloat): BigFloat {
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
    } else bigiter
    return t
//    return ((zx * zx) + (zy * zy)) / (btpi)
}

var centerY = numContext.ZERO
var centerX = numContext.valueOf(-0.67)

// px_X * scale = math_X
var scale = numContext.valueOf(2.0 / 300.0)

fun scaleConverter(i: Int, j: Int, w: Int, h: Int): Pair<BigFloat, BigFloat> {
    val px_y = numContext.valueOf(h / 2.0 - i)
    val px_x = numContext.valueOf(j - w / 2.0)
    return Pair(centerY + (px_y * scale), centerX + (px_x * scale))
}

fun colorResolver(value: BigFloat): Triple<Byte, Byte, Byte> {
    if (value.isInfinity || value == bigiter) {
        return Triple(0, 0, 0)
    }
    try {
        return Triple(
            ((sin(value) + 1.0) / 2.0 * 255.0).toDouble().toInt().toByte(),
            if ((value * 1.3).isInfinity)
                ((cos(value * 1.3) + 1.0) / 2.0 * 255.0).toDouble().toInt().toByte()
            else 0,
            ((sin(sqrt(value) * 3.0) + 1.0) / 2.0 * 255.0).toDouble().toInt().toByte()
        )
    } catch (e: ArithmeticException) {
        return Triple(255.toByte(), 255.toByte(), 255.toByte())
    }
}

fun update(bufs: Array<ByteBuffer>, width: Int, height: Int, myScope: CoroutineScope) {
    try {
        myScope.cancel()
    } catch (ignore: IllegalStateException) {
    }
    for (i in 0 until height) {
        myScope.launch {
            val buf = bufs[height - 1 - i] // is inverted for some reason
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

        var myScope = GlobalScope // CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

        update(bufs, width, height, myScope)

        mouse.buttonDown.listen {
            val i = it.position.y.toInt()
            val j = it.position.x.toInt()
            val (ny, nx) = scaleConverter(i, j, width, height)
//            println("click at $i $j")
//            println("center at $centerY $centerX")
//            println("calcs are ${height / 2.0 - i} ${j - width / 2.0}")
//            println("NEW center at $ny $nx")
            scale /= 8.0
            centerY = numContext.valueOf(ny)
            centerX = numContext.valueOf(nx)

            update(bufs, width, height, myScope)
        }

        extend {
            buffer.rewind()

            cb.write(buffer, ColorFormat.RGB)

            drawer.image(cb)
        }
    }
}
