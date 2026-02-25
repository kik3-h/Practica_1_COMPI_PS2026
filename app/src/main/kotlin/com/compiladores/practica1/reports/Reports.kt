package com.compiladores.practica1.reports

// ─────────────────────────────────────────────────────────────────
// Java-visible class (used from JFlex / CUP generated Java code)
// ─────────────────────────────────────────────────────────────────

data class ErrorReport @JvmOverloads constructor(
    val lexeme: String,
    val line: Int,
    val column: Int,
    val type: String,            // "Léxico" | "Sintáctico"
    val description: String
)

// ─────────────────────────────────────────────────────────────────
// Operator occurrence report
// ─────────────────────────────────────────────────────────────────

data class OperatorReport(
    val operator: String,        // "Suma" | "Resta" | "Multiplicación" | "División"
    val line: Int,
    val column: Int,
    val occurrence: String       // textual context  e.g. "12 + 2"
)

// ─────────────────────────────────────────────────────────────────
// Control structure report
// ─────────────────────────────────────────────────────────────────

data class ControlReport(
    val type: String,            // "SI" | "MIENTRAS"
    val line: Int,
    val condition: String        // textual condition
)
