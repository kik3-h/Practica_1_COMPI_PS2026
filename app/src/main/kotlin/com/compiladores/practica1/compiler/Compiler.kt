package com.compiladores.practica1.compiler

import com.compiladores.practica1.ast.*
import com.compiladores.practica1.generated.Lexer
import com.compiladores.practica1.generated.Parser
import com.compiladores.practica1.reports.*
import java.io.StringReader

// esta clase es lo que la interfaz necesita para funcionar

data class CompileResult(
    val errors: List<ErrorReport>,
    val program: ProgramNode?,
    val operators: List<OperatorReport>,
    val controls: List<ControlReport>,
    val styles: StyleMap
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()
}

/** Asigna el indice del elemento (basado en 1) a su ElementStyle */
typealias StyleMap = Map<Int, ElementStyle>

// lo visual principal del compilador
object Compiler {

    fun compile(source: String): CompileResult {
        val er = Regex("[\\u200B-\\u200D\\uFEFF\\p{C}&&[^\\n\\r\\t]]")
        val cleanSource = source.replace(er, "")
        // 1. Lex + Parse
        val reader = StringReader(source)
        val lexer  = Lexer(reader)
        val par    = Parser(lexer)

        val program: ProgramNode? = try {
            par.parse().value as? ProgramNode
        } catch (e: Exception) {
            null
        }

        // aca es donde se recopilan errores
        val errors = mutableListOf<ErrorReport>()
        errors += lexer.errors
        errors += par.errors
        errors.sortWith(compareBy({ it.line }, { it.column }))

        if (errors.isNotEmpty() || program == null) {
            return CompileResult(errors, null, emptyList(), emptyList(), emptyMap())
        }

        // aca se generan informes
        val opReports      = mutableListOf<OperatorReport>()
        val controlReports = mutableListOf<ControlReport>()

        walkAlgo(program.algo.stmts, opReports, controlReports)

        // configuracion de estilos
        val styles = resolveStyles(program.config)

        return CompileResult(emptyList(), program, opReports, controlReports, styles)
    }

    private fun walkAlgo(
        stmts: List<StmtNode>,
        ops: MutableList<OperatorReport>,
        controls: MutableList<ControlReport>
    ) {
        for (stmt in stmts) {
            when (stmt) {
                is VarDeclNode  -> stmt.init?.let  { collectOps(it, ops) }
                is AssignNode   -> collectOps(stmt.value, ops)
                is ShowNode     -> collectOps(stmt.value, ops)
                is IfNode       -> {
                    collectCondOps(stmt.condition, ops)
                    controls += ControlReport("SI", stmt.line, conditionText(stmt.condition))
                    walkAlgo(stmt.body, ops, controls)   // el recorrido sin anidacion impuesta por la gramatica
                }
                is WhileNode    -> {
                    collectCondOps(stmt.condition, ops)
                    controls += ControlReport("MIENTRAS", stmt.line, conditionText(stmt.condition))
                    walkAlgo(stmt.body, ops, controls)
                }
                else -> { /* ReadNode – no exprs */ }
            }
        }
    }
//detecto operadores
    private fun collectOps(expr: ExprNode, ops: MutableList<OperatorReport>) {
        when (expr) {
            is BinOpNode -> {
                val name = when (expr.op) {
                    "+" -> "Suma"; "-" -> "Resta"; "*" -> "Multiplicación"; "/" -> "División"
                    else -> expr.op
                }
                ops += OperatorReport(name, expr.line, expr.col, exprText(expr))
                collectOps(expr.left, ops)
                collectOps(expr.right, ops)
            }
            is NegNode -> collectOps(expr.expr, ops)
            else -> { /* leaf */ }
        }
    }

    private fun collectCondOps(cond: CondNode, ops: MutableList<OperatorReport>) {
        when (cond) {
            is RelOpNode -> { collectOps(cond.left, ops); collectOps(cond.right, ops) }
            is LogOpNode -> { collectCondOps(cond.left, ops); collectCondOps(cond.right, ops) }
            is NotNode   -> collectCondOps(cond.cond, ops)
        }
    }

//generacion del texto
    fun exprText(expr: ExprNode): String = when (expr) {
        is BinOpNode        -> "${exprText(expr.left)} ${expr.op} ${exprText(expr.right)}"
        is NegNode          -> "-${exprText(expr.expr)}"
        is IntLiteralNode   -> expr.value.toString()
        is DecLiteralNode   -> expr.value.toString()
        is StringLiteralNode -> "\"${expr.value}\""
        is IdNode           -> expr.name
        else                -> "?"
    }

    fun conditionText(cond: CondNode): String = when (cond) {
        is RelOpNode -> "${exprText(cond.left)} ${cond.op} ${exprText(cond.right)}"
        is LogOpNode -> "${conditionText(cond.left)} ${cond.op} ${conditionText(cond.right)}"
        is NotNode   -> "!${conditionText(cond.cond)}"
        else         -> "?"
    }

    // Resoluciones del estilo

    private fun resolveStyles(cfg: ConfigSection): StyleMap {
        // aca se cuenta cuantas condiciones o ciclos hay para validar
        val map = mutableMapOf<Int, ElementStyle>()

        fun getOrDefault(idx: Int): ElementStyle =
            map.getOrPut(idx) { ElementStyle() }

        for (instr in cfg.instrs) {
            val idx = instr.index
            val style = getOrDefault(idx)
            when (instr.command) {
                "DEFAULT"               -> { /* reset – already defaults */ }
                "COLOR_TEXTO_SI",
                "COLOR_TEXTO_MIENTRAS",
                "COLOR_TEXTO_BLOQUE"    -> (instr.value as? ColorValue)?.let {
                                            style.textColor = it.toAndroidColor() }
                "COLOR_SI",
                "COLOR_MIENTRAS",
                "COLOR_BLOQUE"          -> (instr.value as? ColorValue)?.let {
                                            style.bgColor = it.toAndroidColor() }
                "FIGURA_SI",
                "FIGURA_MIENTRAS",
                "FIGURA_BLOQUE"         -> (instr.value as? String)?.let {
                                            style.figura = it }
                "LETRA_SI",
                "LETRA_MIENTRAS",
                "LETRA_BLOQUE"          -> (instr.value as? String)?.let {
                                            style.fontName = it }
                "LETRA_SIZE_SI",
                "LETRA_SIZE_MIENTRAS",
                "LETRA_SIZE_BLOQUE"     -> (instr.value as? Double)?.let {
                                            style.fontSize = it.toFloat() }
            }
            map[idx] = style
        }
        return map
    }
}
