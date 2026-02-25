package com.compiladores.practica1.ui

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.*
import com.compiladores.practica1.R
import com.compiladores.practica1.compiler.Compiler
import com.compiladores.practica1.compiler.CompileResult
import com.compiladores.practica1.flowchart.FlowchartView
import com.compiladores.practica1.reports.*
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var editor: EditText
    private lateinit var btnCompile: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var contentFrame: FrameLayout
    private lateinit var flowchartView: FlowchartView

    private var lastResult: CompileResult? = null

    // Panels (created lazily and swapped in)
    private val panelFlowchart by lazy { flowchartView }
    private val panelOps       by lazy { buildReportView() }
    private val panelControls  by lazy { buildReportView() }
    private val panelErrors    by lazy { buildReportView() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editor       = findViewById(R.id.editor)
        btnCompile   = findViewById(R.id.btnCompile)
        tabLayout    = findViewById(R.id.tabLayout)
        contentFrame = findViewById(R.id.contentFrame)
        flowchartView = FlowchartView(this)

        // Default code sample
        editor.setText(DEFAULT_CODE)

        btnCompile.setOnClickListener { doCompile() }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab)   = showTab(tab.position)
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        setupTabs(hasErrors = false)
        showTab(0)
    }

    // ─────────────────────────────────────────────────────────
    // Compilation
    // ─────────────────────────────────────────────────────────

    private fun doCompile() {
        val source = editor.text.toString()
        val result = Compiler.compile(source)
        lastResult = result

        if (result.hasErrors) {
            setupTabs(hasErrors = true)
            showTab(0)                     // show errors tab
            populateErrors(result.errors)
        } else {
            setupTabs(hasErrors = false)
            // Populate reports
            populateOps(result.operators)
            populateControls(result.controls)
            result.program?.let { flowchartView.setProgram(result) }
            showTab(0)                     // show flowchart first
        }
    }

    // ─────────────────────────────────────────────────────────
    // Tabs
    // ─────────────────────────────────────────────────────────

    private fun setupTabs(hasErrors: Boolean) {
        tabLayout.removeAllTabs()
        if (hasErrors) {
            tabLayout.addTab(tabLayout.newTab().setText("⚠ Errores"))
        } else {
            tabLayout.addTab(tabLayout.newTab().setText("Diagrama"))
            tabLayout.addTab(tabLayout.newTab().setText("Operadores"))
            tabLayout.addTab(tabLayout.newTab().setText("Control"))
        }
    }

    private fun showTab(pos: Int) {
        val hasErrors = lastResult?.hasErrors ?: false
        contentFrame.removeAllViews()
        val view: View = if (hasErrors) {
            when (pos) { else -> panelErrors }
        } else {
            when (pos) {
                0    -> panelFlowchart
                1    -> panelOps
                else -> panelControls
            }
        }
        if (view.parent != null) (view.parent as ViewGroup).removeView(view)
        contentFrame.addView(view, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT))
    }

    // ─────────────────────────────────────────────────────────
    // Report population
    // ─────────────────────────────────────────────────────────

    private fun populateErrors(errors: List<ErrorReport>) {
        val headers = listOf("Lexema", "Línea", "Columna", "Tipo", "Descripción")
        val rows    = errors.map { listOf(it.lexeme, it.line.toString(), it.column.toString(), it.type, it.description) }
        setTableData(panelErrors, headers, rows)
    }

    private fun populateOps(ops: List<OperatorReport>) {
        val headers = listOf("Operador", "Línea", "Columna", "Ocurrencia")
        val rows    = ops.map { listOf(it.operator, it.line.toString(), it.column.toString(), it.occurrence) }
        setTableData(panelOps, headers, rows)
    }

    private fun populateControls(ctrls: List<ControlReport>) {
        val headers = listOf("Objeto", "Línea", "Condición")
        val rows    = ctrls.map { listOf(it.type, it.line.toString(), it.condition) }
        setTableData(panelControls, headers, rows)
    }

    private fun setTableData(rv: RecyclerView, headers: List<String>, rows: List<List<String>>) {
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = TableAdapter(headers, rows)
    }

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

    private fun buildReportView(): RecyclerView {
        return RecyclerView(this).apply {
            setPadding(8, 8, 8, 8)
            setBackgroundColor(android.graphics.Color.WHITE)
        }
    }

    companion object {
        val DEFAULT_CODE = """
INICIO
    VAR a = 10
    VAR b = 20
    SI (a < b) ENTONCES
        MOSTRAR "a es menor que b"
    FINSI
    MIENTRAS (a < 15) HACER
        a = a + 1
        MOSTRAR a
    FINMIENTRAS
    MOSTRAR "Fin del programa"
FIN
%%%%
%DEFAULT=1
%COLOR_TEXTO_SI=12,45-5,1|1
%FIGURA_MIENTRAS=CIRCULO|1
%DEFAULT=3
        """.trimIndent()
    }
}

// ─────────────────────────────────────────────────────────────────
// TableAdapter – simple RecyclerView table
// ─────────────────────────────────────────────────────────────────

class TableAdapter(
    private val headers: List<String>,
    private val rows: List<List<String>>
) : RecyclerView.Adapter<TableAdapter.RowVH>() {

    inner class RowVH(val ll: LinearLayout) : RecyclerView.ViewHolder(ll)

    override fun getItemCount() = rows.size + 1   // +1 for header

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowVH {
        val ll = LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT)
            setPadding(0, 2, 0, 2)
        }
        return RowVH(ll)
    }

    override fun onBindViewHolder(holder: RowVH, position: Int) {
        holder.ll.removeAllViews()
        val isHeader = position == 0
        val cells    = if (isHeader) headers else rows[position - 1]
        val weight   = 1f / cells.size

        cells.forEach { text ->
            val tv = TextView(holder.ll.context).apply {
                this.text = text
                textSize  = if (isHeader) 13f else 12f
                if (isHeader) setTypeface(null, android.graphics.Typeface.BOLD)
                setBackgroundColor(
                    if (isHeader) android.graphics.Color.parseColor("#1976D2")
                    else android.graphics.Color.WHITE
                )
                setTextColor(
                    if (isHeader) android.graphics.Color.WHITE
                    else android.graphics.Color.DKGRAY
                )
                setPadding(8, 6, 8, 6)
                layoutParams = LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, weight)
            }
            holder.ll.addView(tv)
        }

        // Divider color for rows
        if (!isHeader && position % 2 == 0) {
            holder.ll.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"))
        } else if (!isHeader) {
            holder.ll.setBackgroundColor(android.graphics.Color.WHITE)
        }
    }
}
