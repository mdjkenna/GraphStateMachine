@file:Suppress("unused")

package mdk.gsm.util

import mdk.gsm.graph.IVertex

/**
 * An interface for a addVertex with an [id] of type [String].
 *
 * @property id The [String] identifier of the addVertex.
 */
interface IStringVertex : IVertex<String> {
    override val id: String
}

/**
 * An interface for a addVertex with an [id] of type [Int].
 *
 * @property id The [Int] identifier of the addVertex.
 */
interface IIntVertex : IVertex<Int> {
    override val id: Int
}

/**
 * An interface for a addVertex with an [id] of type [Long].
 *
 * @property id The [Long] identifier of the addVertex.
 */
interface ILongVertex : IVertex<Long> {
    override val id: Long
}

/**
 * An interface for a addVertex with an [id] of type [Char].
 *
 * @property id The [Char] identifier of the addVertex.
 */
interface ICharVertex : IVertex<Char> {
    override val id: Char
}

/**
 * An interface for a addVertex with an [id] of type [Byte].
 *
 * @property id The [Byte] identifier of the addVertex.
 */
interface IByteVertex : IVertex<Byte> {
    override val id: Byte
}

/**
 * An interface for a addVertex with an [id] of type [Short].
 *
 * @property id The [Short] identifier of the addVertex.
 */
interface IShortVertex : IVertex<Short> {
    override val id: Short
}

/**
 * An interface for a addVertex with an [id] of type [Float].
 *
 * @property id The [Float] identifier of the addVertex.
 */
interface IFloatVertex : IVertex<Float> {
    override val id: Float
}

/**
 * An interface for a addVertex with an [id] of type [Double].
 *
 * @property id The [Double] identifier of the addVertex.
 */
interface IDoubleVertex : IVertex<Double> {
    override val id: Double
}
