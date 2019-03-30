package com.examples.linak

typealias MatrixColumns = Array<Vector>

data class Matrix
@Throws(IllegalArgumentException::class)
constructor(
    val rows: MatrixColumns = arrayOf(doubleArrayOf(0.0))
) {

    init {
        if (rows.isNotEmpty()) {
            val initialSize = rows[0].size
            rows.forEach {
                if (it.size != initialSize)
                    throw ColumnSizeException()
            }
        }
    }

    val columnAmount: Int
        get() {
            var result = 0
            rows.forEach {
                result += it.size
            }
            return result
        }

    val rowAmount: Int = rows.size

    override fun toString(): String {
        var result = ""
        rows.forEach {
            result += "\n["
            (0 until it.size - 1).forEach { i ->
                result += "${it[i]}, "
            }
            result += "${it.last()}]"
        }

        return result.replaceFirst("\n", "")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix) return false

        if (!rows.contentEquals(other.rows)) return false
        if (columnAmount != other.columnAmount) return false
        if (rowAmount != other.rowAmount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rows.contentHashCode()
        result = 31 * result + columnAmount
        result = 31 * result + rowAmount
        return result
    }
}


//
//    if (size == other.size)
//        mapIndexed { index, v ->
//            other[index] + v
//        }.toMatrix()
//    else throw IllegalArgumentException(
//        "Matrices must be of the same size"
//    )
//}
//
//infix fun Matrix.x(other: Matrix): Matrix =
//    kotlin.collections.mutableListOf<Double>().apply {
//        addAll(this@x)
//        addAll(other.values)
//    }.toMatrix()
//
//infix operator fun Matrix.times(x: Int): Matrix =
//    this * x.toDouble()
//
//infix operator fun Matrix.times(x: Double): Matrix =
//    map { it * x }.toMatrix()
