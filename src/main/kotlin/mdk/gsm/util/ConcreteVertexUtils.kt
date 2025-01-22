@file:Suppress("unused")

package mdk.gsm.util

/**
 * An implementation of the [IStringVertex] interface included for convenience
 * @property id The [String] identifier of the vertex
 */
data class StringVertex(
    override val id: String,
) : IStringVertex

/**
 * An implementation of the [IIntVertex] interface included for convenience.
 * @property id The [Int] identifier of the vertex.
 */
data class IntVertex(
    override val id: Int
) : IIntVertex

/**
 * An implementation of the [ILongVertex] interface included for convenience.
 * @property id The [Long] identifier of the vertex.
 */
data class LongVertex(
    override val id: Long
) : ILongVertex

/**
 * An implementation of the [ICharVertex] interface included for convenience.
 * @property id The [Char] identifier of the vertex.
 */
data class CharVertex(
    override val id: Char
) : ICharVertex

/**
 * An implementation of the [IByteVertex] interface included for convenience.
 * @property id The [Byte] identifier of the vertex.
 */
data class ByteVertex(
    override val id: Byte
) : IByteVertex

/**
 * An implementation of the [IShortVertex] interface included for convenience.
 * @property id The [Short] identifier of the vertex.
 */
data class ShortVertex(
    override val id: Short
) : IShortVertex