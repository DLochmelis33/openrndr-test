package util

import kotlin.math.*

val ((Double) -> Double).diff: (Double) -> Double
    inline get() = { x: Double ->
        val h = 0.002
        (this(x + h) - this(x - h)) / (2 * h)
    }

typealias Vector<T> = List<T>

val ((Vector<Double>) -> Double).grad: (Vector<Double>) -> Vector<Double>
    inline get() = { xs: List<Double> ->
        xs.indices.map { i ->
            { x: Double ->
                val tmp = xs.toMutableList()
                tmp[i] = x
                this@grad(tmp)
            }.diff(xs[i])
        }
    }

fun <T> List<T>.toPair(): Pair<T, T> {
    val (e1, e2) = if (this.size != 2) throw Exception() else this
    return Pair(e1, e2)
}

fun <T> List<T>.toTriple(): Triple<T, T, T> {
    val (e1, e2, e3) = if (this.size != 3) throw Exception() else this
    return Triple(e1, e2, e3)
}

val ((Double, Double) -> Double).grad: (Double, Double) -> Pair<Double, Double>
    get() = { x, y ->
        { xs: Vector<Double> -> this(xs[0], xs[1]) }.grad(listOf(x, y)).toPair()
    }

val ((Double, Double, Double) -> Double).grad: (Double, Double, Double) -> Triple<Double, Double, Double>
    get() = { x, y, z ->
        { xs: Vector<Double> -> this(xs[0], xs[1], xs[2]) }.grad(listOf(x, y, z)).toTriple()
    }

val Vector<Double>.norm
    get() = sqrt(this.asSequence().map { it.pow(2) }.sum())

val Pair<Double, Double>.norm
    get() = sqrt(first.pow(2) + second.pow(2))

val Triple<Double, Double, Double>.norm
    get() = sqrt(first.pow(2) + second.pow(2) + third.pow(2))
