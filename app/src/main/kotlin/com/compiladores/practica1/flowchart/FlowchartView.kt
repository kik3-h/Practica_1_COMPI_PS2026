package com.compiladores.practica1.flowchart

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.compiladores.practica1.ast.*
import com.compiladores.practica1.compiler.StyleMap
import com.compiladores.practica1.compiler.CompileResult
import kotlin.math.max

// esta es la clase de vista de diagrama de flujo, zoom y desplazamiento

class FlowchartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var nodes: List<FlowNode> = emptyList()
    private var edges: List<FlowEdge> = emptyList()

    // aca se permite el desplazamiento y zoom
    private var scaleFactor = 1f
    private var translateX  = 0f
    private var translateY  = 0f
    private var lastTouchX  = 0f
    private var lastTouchY  = 0f

    private val scaleDetector = ScaleGestureDetector(context, object :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector): Boolean {
            scaleFactor *= d.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.2f, 4f)
            invalidate(); return true
        }
    })

    private val gestureDetector = GestureDetector(context, object :
        GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            translateX -= dx; translateY -= dy; invalidate(); return true
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    // La api publica

    fun setProgram(result: CompileResult) {
        val layout = FlowchartLayout(result)
        nodes = layout.nodes
        edges = layout.edges
        // Reset pan/zoom
        scaleFactor = 1f; translateX = 0f; translateY = 20f
        invalidate()
    }

    fun clear() { nodes = emptyList(); edges = emptyList(); invalidate() }

    // aca se dibuja el diagrama
    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY; strokeWidth = 2f; style = Paint.Style.STROKE
    }
    private val arrowFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY; style = Paint.Style.FILL
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY; textSize = 11f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(translateX, translateY)
        canvas.scale(scaleFactor, scaleFactor)

        edges.forEach { drawEdge(canvas, it) }
        nodes.forEach { drawNode(canvas, it) }

        canvas.restore()
    }

    private fun drawEdge(canvas: Canvas, edge: FlowEdge) {
        val x1 = edge.from.cx; val y1 = edge.from.bottom
        val x2 = edge.to.cx;   val y2 = edge.to.top

        canvas.drawLine(x1, y1, x2, y2, arrowPaint)

        val angle = Math.atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())
        val arrowLen = 10f
        val arrowAngle = 0.4
        val path = Path()
        path.moveTo(x2, y2)
        path.lineTo(
            (x2 - arrowLen * Math.cos(angle - arrowAngle)).toFloat(),
            (y2 - arrowLen * Math.sin(angle - arrowAngle)).toFloat()
        )
        path.lineTo(
            (x2 - arrowLen * Math.cos(angle + arrowAngle)).toFloat(),
            (y2 - arrowLen * Math.sin(angle + arrowAngle)).toFloat()
        )
        path.close()
        canvas.drawPath(path, arrowFill)

        // etiquetas para bordes de condiciones si o no
        edge.label?.let {
            canvas.drawText(it, (x1 + x2) / 2f + 4, (y1 + y2) / 2f, labelPaint)
        }
    }

    private fun drawNode(canvas: Canvas, node: FlowNode) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = node.style.bgColor; style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY; strokeWidth = 2f; style = Paint.Style.STROKE
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = node.style.textColor
            textSize = node.style.fontSize
            typeface = fontForName(node.style.fontName)
            textAlign = Paint.Align.CENTER
        }

        val rect = RectF(node.left, node.top, node.right, node.bottom)

        when (node.style.figura.uppercase()) {
            "CIRCULO", "ELIPSE" -> {
                canvas.drawOval(rect, fillPaint)
                canvas.drawOval(rect, strokePaint)
            }
            "ROMBO" -> {
                val path = diamondPath(rect)
                canvas.drawPath(path, fillPaint)
                canvas.drawPath(path, strokePaint)
            }
            "RECTANGULO_REDONDEADO" -> {
                canvas.drawRoundRect(rect, 20f, 20f, fillPaint)
                canvas.drawRoundRect(rect, 20f, 20f, strokePaint)
            }
            "PARALELOGRAMO" -> {
                val path = parallelogramPath(rect)
                canvas.drawPath(path, fillPaint)
                canvas.drawPath(path, strokePaint)
            }
            else -> { //por predeterminado el rectangulo
                canvas.drawRect(rect, fillPaint)
                canvas.drawRect(rect, strokePaint)
            }
        }

        // texto de varias lineas
        val lines = node.label.split("\n")
        val lineH = textPaint.textSize + 2f
        val totalH = lineH * lines.size
        var ty = node.cy - totalH / 2f + textPaint.textSize
        for (line in lines) {
            canvas.drawText(line, node.cx, ty, textPaint)
            ty += lineH
        }
    }

    private fun fontForName(name: String): Typeface = when (name.uppercase()) {
        "TIMES_NEW_ROMAN" -> Typeface.SERIF
        "COMIC_SANS"      -> Typeface.create("cursive", Typeface.NORMAL)
        "VERDANA"         -> Typeface.create("sans-serif-condensed", Typeface.NORMAL)
        else              -> Typeface.SANS_SERIF
    }

    private fun diamondPath(r: RectF) = Path().apply {
        moveTo(r.centerX(), r.top)
        lineTo(r.right, r.centerY())
        lineTo(r.centerX(), r.bottom)
        lineTo(r.left, r.centerY())
        close()
    }

    private fun parallelogramPath(r: RectF) = Path().apply {
        val skew = 15f
        moveTo(r.left + skew, r.top)
        lineTo(r.right, r.top)
        lineTo(r.right - skew, r.bottom)
        lineTo(r.left, r.bottom)
        close()
    }
}

// Nodo de flujo y sus bordes

data class FlowNode(
    val id: Int,
    val label: String,
    var cx: Float, var cy: Float,
    val width: Float, val height: Float,
    val style: com.compiladores.practica1.ast.ElementStyle
) {
    val left   get() = cx - width / 2f
    val right  get() = cx + width / 2f
    val top    get() = cy - height / 2f
    val bottom get() = cy + height / 2f
}

data class FlowEdge(
    val from: FlowNode,
    val to: FlowNode,
    val label: String? = null
)

// constructor de disenios

class FlowchartLayout(private val result: CompileResult) {

    val nodes = mutableListOf<FlowNode>()
    val edges = mutableListOf<FlowEdge>()

    private var nodeId    = 0
    private val CX        = 300f
    private val NODE_W    = 200f
    private val NODE_H    = 50f
    private val V_GAP     = 30f
    private var currentY  = 50f

    private var ifCount    = 0
    private var whileCount = 0
    private var blockCount = 0

    init { build() }

    private fun nextId() = nodeId++

    private fun defaultStyle(figura: String = "RECTANGULO") =
        ElementStyle(figura = figura)

    private fun addNode(label: String, style: ElementStyle): FlowNode {
        val n = FlowNode(nextId(), label, CX, currentY + NODE_H / 2f, NODE_W, NODE_H, style)
        nodes += n
        currentY += NODE_H + V_GAP
        return n
    }

    private fun addEdge(from: FlowNode, to: FlowNode, label: String? = null) {
        edges += FlowEdge(from, to, label)
    }

    private fun build() {
        val prog = result.program ?: return

        // aca es la figura de inicio ovalada
        val startStyle = ElementStyle(figura = "ELIPSE", bgColor = android.graphics.Color.parseColor("#90EE90"))
        val startNode  = addNode("INICIO", startStyle)
        var prev = startNode

        // Declaraciones del camino (walk)
        prev = walkStmts(prog.algo.stmts, prev)

        // se inserta el fin con un ovalo
        val endStyle = ElementStyle(figura = "ELIPSE", bgColor = android.graphics.Color.parseColor("#FF9999"))
        val endNode  = addNode("FIN", endStyle)
        addEdge(prev, endNode)
    }

    private fun walkStmts(stmts: List<StmtNode>, incoming: FlowNode): FlowNode {
        var prev = incoming
        for (stmt in stmts) {
            prev = when (stmt) {
                is VarDeclNode  -> {
                    blockCount++
                    val s = result.styles[blockCount] ?: defaultStyle("RECTANGULO")
                    val lbl = if (stmt.init != null)
                        "VAR ${stmt.name} = ${com.compiladores.practica1.compiler.Compiler.exprText(stmt.init)}"
                    else "VAR ${stmt.name}"
                    val n = addNode(lbl, s)
                    addEdge(prev, n); n
                }
                is AssignNode   -> {
                    blockCount++
                    val s = result.styles[blockCount] ?: defaultStyle("RECTANGULO")
                    val n = addNode("${stmt.name} = ${com.compiladores.practica1.compiler.Compiler.exprText(stmt.value)}", s)
                    addEdge(prev, n); n
                }
                is ShowNode     -> {
                    blockCount++
                    val s = result.styles[blockCount] ?: defaultStyle("PARALELOGRAMO")
                    val n = addNode("MOSTRAR ${com.compiladores.practica1.compiler.Compiler.exprText(stmt.value)}", s)
                    addEdge(prev, n); n
                }
                is ReadNode     -> {
                    blockCount++
                    val s = result.styles[blockCount] ?: defaultStyle("PARALELOGRAMO")
                    val n = addNode("LEER ${stmt.variable}", s)
                    addEdge(prev, n); n
                }
                is IfNode       -> buildIf(stmt, prev)
                is WhileNode    -> buildWhile(stmt, prev)
                else            -> prev
            }
        }
        return prev
    }

    private fun buildIf(stmt: IfNode, incoming: FlowNode): FlowNode {
        ifCount++
        val s  = result.styles[ifCount] ?: defaultStyle("ROMBO")
        val condNode = addNode("SI\n${com.compiladores.practica1.compiler.Compiler.conditionText(stmt.condition)}", s)
        addEdge(incoming, condNode)

        // Rama del cuerpo
        val bodyLast = walkStmts(stmt.body, condNode)

        // punto de fusion nodo invisible o vacio
        val mergeStyle = ElementStyle(bgColor = android.graphics.Color.TRANSPARENT, figura = "CIRCULO")
        val mergeNode  = addNode("", mergeStyle)

        addEdge(bodyLast, mergeNode, "SÍ")
        addEdge(condNode, mergeNode, "NO")  // direct skip

        return mergeNode
    }

    private fun buildWhile(stmt: WhileNode, incoming: FlowNode): FlowNode {
        whileCount++
        val s = result.styles[whileCount] ?: defaultStyle("ROMBO")
        val condNode = addNode("MIENTRAS\n${com.compiladores.practica1.compiler.Compiler.conditionText(stmt.condition)}", s)
        addEdge(incoming, condNode)

        val bodyLast = walkStmts(stmt.body, condNode)

        addEdge(bodyLast, condNode, "SÍ")

        val exitStyle = ElementStyle(figura = "CIRCULO")
        val exitNode  = addNode("", exitStyle)
        addEdge(condNode, exitNode, "NO")

        return exitNode
    }
}
