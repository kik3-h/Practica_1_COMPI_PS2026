package com.compiladores.practica1.compiler

import com.compiladores.practica1.ast.*
import com.compiladores.practica1.generated.Lexer
import com.compiladores.practica1.generated.Parser
import com.compiladores.practica1.reports.*
import java.io.StringReader

// ─────────────────────────────────────────────────────────────────
// CompileResult – everything the UI needs
// ─────────────────────────────────────────────────────────────────

data class CompileResult(
    val errors: List<ErrorReport>,
    val program: ProgramNode?,
    val operators: List<OperatorReport>,
    val controls: List<ControlReport>,
    val styles: StyleMap
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()
}

/** Maps element index (1-based) to its resolved ElementStyle */
typealias StyleMap = Map<Int, ElementStyle>

// ─────────────────────────────────────────────────────────────────
// Main Compiler façade
// ─────────────────────────────────────────────────────────────────

object Compiler {

    fun compile(source: String): CompileResult {
        // 1. Lex + Parse
        val reader = StringReader(source)
        val lexer  = Lexer(reader)
        val par    = Parser(lexer)

        val program: ProgramNode? = try {
            par.parse().value as? ProgramNode
        } catch (e: Exception) {
            null
        }

        // 2. Collect errors (lexer errors + parser errors)
        val errors = mutableListOf<ErrorReport>()
        errors += lexer.errors
        errors += par.errors
        errors.sortWith(compareBy({ it.line }, { it.column }))

        if (errors.isNotEmpty() || program == null) {
            return CompileResult(errors, null, emptyList(), emptyList(), emptyMap())
        }

        // 3. Walk AST → build reports
        val opReports      = mutableListOf<OperatorReport>()
        val controlReports = mutableListOf<ControlReport>()

        walkAlgo(program.algo.stmts, opReports, controlReports)

        // 4. Resolve styles from config section
        val styles = resolveStyles(program.config)

        return CompileResult(emptyList(), program, opReports, controlReports, styles)
    }

    // ─────────────────────────────────────────────────────────
    // AST walker
    // ─────────────────────────────────────────────────────────

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
                    walkAlgo(stmt.body, ops, controls)   // walk body (no nesting enforced by grammar)
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

    // ─────────────────────────────────────────────────────────
    // Text reconstruction helpers
    // ─────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────
    // Style resolution
    // ─────────────────────────────────────────────────────────

    private fun resolveStyles(cfg: ConfigSection): StyleMap {
        // Count how many SI / MIENTRAS / BLOQUEs we have (to validate indices)
        // For now we apply styles to a shared map indexed per element occurrence
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
