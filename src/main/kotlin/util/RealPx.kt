package util

import org.openrndr.Program

// (x, y) --> real
// (j, i) --> px
class RealPx(
    var xc: Double = 0.0,
    var yc: Double = 0.0,
    var scale: Double = 40.0, // how many pixels in one real unit
    val whGetter: () -> Pair<Number, Number> = { Pair(200.0, 200.0) } // get window dimensions
) {
    fun realToPx(x: Number, y: Number): Pair<Double, Double> {
        val (width, height) = whGetter()
        return Pair(
            ((x.toDouble() - xc) * scale + width.toDouble() / 2.0),
            (-(y.toDouble() - yc) * scale + height.toDouble() / 2.0)
        )
    }

    fun pxToReal(j: Number, i: Number): Pair<Double, Double> {
        val (width, height) = whGetter()
        return Pair(
            (j.toDouble() - width.toDouble() / 2.0) / scale + xc,
            -(i.toDouble() - height.toDouble() / 2.0) / scale + yc
        )
    }
}