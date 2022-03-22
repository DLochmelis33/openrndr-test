package util

import org.openrndr.Program

// (x, y) --> real
// (j, i) --> px
class RealPx(
    val width: Number,
    val height: Number,
    var xc: Double,
    var yc: Double,
    var scale: Double, // how many pixels in one real unit
) {
    constructor(p: Program, xc: Double, yc: Double, scale: Double) : this(p.width, p.height, xc, yc, scale)

    fun realToPx(x: Number, y: Number): Pair<Double, Double> {
        return Pair(
            ((x.toDouble() - xc) * scale + width.toDouble() / 2.0),
            (-(y.toDouble() - yc) * scale + height.toDouble() / 2.0)
        )
    }

    fun pxToReal(j: Number, i: Number): Pair<Double, Double> {
        return Pair(
            (j.toDouble() - width.toDouble() / 2.0) / scale + xc,
            -(i.toDouble() - height.toDouble() / 2.0) / scale + yc
        )
    }
}