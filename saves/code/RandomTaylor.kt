import java.util.*
import kotlin.Comparator
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

const val EPS = 1e-10
fun Int.factorial(): Int = if (this <= 1) 1 else (2..this).reduce { a, b -> a * b }
fun Double.isZero(): Boolean = abs(this) < EPS

const val SEED = 3

// MUST BE DETERMINISTIC
fun genCoef(k: Double): Double {
    val random = Random(k.roundToInt() + SEED)
    val lim = k.pow(k)
    val t = 1.3
    val randSign = if (random.nextBoolean()) 1.0 else -1.0
    val randNulSign = if (random.nextBoolean()) 0.0 else randSign
//    return randSign * random.nextDouble(lim / t, t * lim)
//    return randNulSign * k.toInt().factorial()
    val m = when (k.roundToInt() % 4) {
        0, 2 -> 0
        1 -> 1
        3 -> -1
        else -> throw Exception()
    }
    return (m * k.roundToInt().factorial()).toDouble()
//        .also { println(it) }
}

fun main() {
    val f = { x: Double ->
        val n = 9
        var res = 0.0
        for (k in 1..n) {
            val coef: Double = (1 / genCoef(k.toDouble())).takeUnless { it.isInfinite() } ?: 0.0
//            println(if (coef.isZero()) 0.0 else 1 / coef)
            res += 1 / x.pow(k) * coef
//            res += x.pow(k) / k.factorial()
        }
        res
    }
    plot(f)
}
