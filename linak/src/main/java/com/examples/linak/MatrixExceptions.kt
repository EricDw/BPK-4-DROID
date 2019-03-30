package com.examples.linak

sealed class MatrixException(
    errorMessage: String
) : Exception(errorMessage)

class ColumnSizeException : MatrixException(
    "Columns must be all of the same size"
)

class EmptyRowsException : MatrixException(
    "Operation can not be performed with empty matrices"
)
