package com.examples.linak


@Throws(IndexOutOfBoundsException::class)
fun Matrix.getColumn(i: Int): Vector =
    rows.map {
        it[i]
    }.toDoubleArray()

@Throws(IndexOutOfBoundsException::class)
fun Matrix.getColumns(range: IntRange): Matrix =
    range.map { rows[it] }.toTypedArray().toMatrix()

@Throws(IndexOutOfBoundsException::class)
fun Matrix.getRow(i: Int): Vector =
    rows[i]

@Throws(IndexOutOfBoundsException::class)
fun Matrix.getRows(): Matrix {
    val result = Matrix(Array(rows.size) {
        doubleArrayOf()
    })

    for (i in 0 until rows.size)
        result.rows[i] = getRow(i)

    return result
}

@Throws(MatrixException::class)
infix operator fun Matrix.plus(other: Matrix): Matrix =
    doIfCompatibleWith(other) {
        MatrixColumns(rows.size) {
            rows[it].mapIndexed { index, d -> d + other.rows[it][index] }.toDoubleArray()
        }.toMatrix()
    }

@Throws(MatrixException::class)
infix operator fun Matrix.times(other: Matrix): Matrix =
    Array(rows.size) {
        DoubleArray(other.rows[0].size)
    }.apply {
        var iteration = 0
        for (i in 0 until size) {
            for (j in 0 until size) {
                for (k in 0 until rows[0].size) {
                    iteration++
                    println("Position == i$i j$j k$k iteration: $iteration")
                    this[i][j] += rows[i][k] * other.rows[k][j]
                }
            }
        }
    }.toMatrix()


//@Throws(MatrixException::class)
//infix operator fun Matrix.times(other: Matrix): Matrix =
//    doIfCompatibleWith(other) {
//        MatrixColumns(rows.size) {
//            rows[it].mapIndexed { index, d -> d * other.rows[it][index] }.toDoubleArray()
//        }.toMatrix()
//    }

