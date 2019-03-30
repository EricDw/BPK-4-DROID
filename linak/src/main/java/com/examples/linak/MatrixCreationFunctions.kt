package com.examples.linak

fun Vector.toMatrix(): Matrix =
    Matrix(arrayOf(this))

fun MatrixColumns.toMatrix(): Matrix =
    Matrix(this)

fun matrixOf(vararg n: Double) = Matrix(arrayOf(n))

fun matrixOf(columns: MatrixColumns) = Matrix(columns)

fun matrixOfNRows(vararg n: Vector) = Matrix(Array(n.size) {
    n[it]
})
