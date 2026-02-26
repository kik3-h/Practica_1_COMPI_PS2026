package com.compiladores.practica1.reports

// Clase utilizada a partir del codigo Java generado por JFlex/CUP

data class ErrorReport @JvmOverloads constructor(
    val lexeme: String,
    val line: Int,
    val column: Int,
    val type: String,            // Lexico o Sintactico
    val description: String
)

// aca se informa de sobre el tipo del operador

data class OperatorReport(
    val operator: String,        // Suma,Resta,Multiplicación,División
    val line: Int,
    val column: Int,
    val occurrence: String
)

// se obtiene como estan las estrucutaras de las condicionales y ciclos

data class ControlReport(
    val type: String,            // SI o MIENTRAS
    val line: Int,
    val condition: String        // texto de la condicional
)
