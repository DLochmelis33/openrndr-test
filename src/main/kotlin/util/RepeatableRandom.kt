package util

import kotlin.random.Random

interface RandomContext {
    val rng: Double
}

private val defaultSeed = Random.nextInt()

private class RandomContextImpl(
    seed: Int = defaultSeed,
    private val from: Double = 0.0,
    private val until: Double = 1.0,
) : RandomContext {

    private val random = Random(seed)

    override val rng: Double
        get() = random.nextDouble(from, until)
}

fun <T> withRng(block: RandomContext.() -> T): T = RandomContextImpl().block()

fun <T> withRng(seed: Int, block: RandomContext.() -> T): T = RandomContextImpl(seed).block()

fun <T> withRng(from: Double, until: Double, block: RandomContext.() -> T): T =
    RandomContextImpl(from = from, until = until).block()

fun <T> withRng(seed: Int, from: Double, until: Double, block: RandomContext.() -> T): T =
    RandomContextImpl(seed, from, until).block()
