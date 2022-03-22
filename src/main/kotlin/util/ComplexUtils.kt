package util

import org.apache.commons.math3.complex.Complex

operator fun Complex.unaryMinus(): Complex = this.negate()

operator fun Complex.plus(addend: Complex): Complex = this.add(addend)

operator fun Complex.plus(addend: Double): Complex = this.add(addend)

operator fun Complex.minus(subtrahend: Complex): Complex = this.minus(subtrahend)

operator fun Complex.minus(subtrahend: Double): Complex = this.minus(subtrahend)

operator fun Complex.times(factor: Complex): Complex = this.multiply(factor)

operator fun Complex.times(factor: Double): Complex = this.multiply(factor)

operator fun Complex.times(factor: Int): Complex = this.multiply(factor)

operator fun Complex.div(divisor: Complex): Complex = this.divide(divisor)

operator fun Complex.div(divisor: Double): Complex = this.divide(divisor)

operator fun Complex.component1(): Double = this.real

operator fun Complex.component2(): Double = this.imaginary
