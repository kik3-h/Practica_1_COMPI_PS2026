package com.compiladores.practica1.ast

abstract class ASTNode(val line: Int = 0, val col: Int = 0)

abstract class StmtNode(line: Int = 0, col: Int = 0) : ASTNode(line, col)
abstract class ExprNode(line: Int = 0, col: Int = 0) : ASTNode(line, col)
abstract class CondNode(line: Int = 0, col: Int = 0) : ASTNode(line, col)

class ProgramNode(
    val algo: AlgoSection,
    val config: ConfigSection
) : ASTNode()

class AlgoSection(val stmts: List<StmtNode>) : ASTNode()

class ConfigSection(val instrs: List<ConfigInstr>) : ASTNode()

class VarDeclNode(
    val name: String,
    val init: ExprNode?,
    line: Int = 0, col: Int = 0
) : StmtNode(line, col)

class AssignNode(
    val name: String,
    val value: ExprNode,
    line: Int = 0, col: Int = 0
) : StmtNode(line, col)

class IfNode(
    val condition: CondNode,
    val body: List<StmtNode>,
    line: Int = 0, col: Int = 0
) : StmtNode(line, col)

class WhileNode(
    val condition: CondNode,
    val body: List<StmtNode>,
    line: Int = 0, col: Int = 0
) : StmtNode(line, col)

class ShowNode(
    val value: ExprNode,
    line: Int = 0, col: Int = 0
) : StmtNode(line, col)

class ReadNode(
    val variable: String,
    line: Int = 0, col: Int = 0
) : StmtNode(line, col)

class BinOpNode(
    val op: String,
    val left: ExprNode,
    val right: ExprNode,
    line: Int = 0, col: Int = 0
) : ExprNode(line, col)

class NegNode(val expr: ExprNode, line: Int = 0, col: Int = 0) : ExprNode(line, col)

class IntLiteralNode(val value: Int, line: Int = 0, col: Int = 0) : ExprNode(line, col)
class DecLiteralNode(val value: Double, line: Int = 0, col: Int = 0) : ExprNode(line, col)
class StringLiteralNode(val value: String, line: Int = 0, col: Int = 0) : ExprNode(line, col)
class IdNode(val name: String, line: Int = 0, col: Int = 0) : ExprNode(line, col)

class RelOpNode(
    val op: String,
    val left: ExprNode,
    val right: ExprNode,
    line: Int = 0, col: Int = 0
) : CondNode(line, col)

class LogOpNode(
    val op: String,
    val left: CondNode,
    val right: CondNode,
    line: Int = 0, col: Int = 0
) : CondNode(line, col)

class NotNode(val cond: CondNode, line: Int = 0, col: Int = 0) : CondNode(line, col)


data class ColorValue(
    val r: Int = 0,
    val g: Int = 0,
    val b: Int = 0,
    val hex: String? = null
) {
    constructor(hexStr: String) : this(
        r = hexStr.substring(0, 2).toInt(16),
        g = hexStr.substring(2, 4).toInt(16),
        b = hexStr.substring(4, 6).toInt(16),
        hex = hexStr
    )
    fun toAndroidColor(): Int = android.graphics.Color.rgb(r, g, b)
}

data class ConfigInstr(
    val command: String,      // "COLOR_TEXTO_SI"
    val value: Any?,          // ColorValue, String , Double , null
    val index: Int,           // 1-base
    val line: Int = 0,
    val col: Int = 0
)

data class ElementStyle(
    var textColor: Int = android.graphics.Color.BLACK,
    var bgColor: Int = android.graphics.Color.WHITE,
    var figura: String = "RECTANGULO",
    var fontName: String = "ARIAL",
    var fontSize: Float = 14f
)
