package com.examples.linak

@Throws(MatrixException::class)
inline fun <reified T> Matrix.doIfCompatibleWith(
    other: Matrix,
    operation: Matrix.(other: Matrix) -> T
): T {
    if (rows.isEmpty() && other.rows.isEmpty())
        throw EmptyRowsException()

    val initialSize = rows[0].size
    rows.forEachIndexed { index, _ ->
        if (other.rows[index].size != initialSize)
            throw ColumnSizeException()
    }
    return operation(other)
}

@Throws(MatrixException::class)
fun Matrix.canMultiply(vector: Vector): Boolean {
    if (rows.isEmpty() && vector.isEmpty())
        throw EmptyRowsException()

    val initialSize = rows[0].size
    if (vector.size != initialSize)
        throw ColumnSizeException()

    return true
}
