package org.vaadin.haijian

import com.vaadin.flow.component.grid.Grid
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.slf4j.LoggerFactory
import java.io.FileOutputStream
import java.util.*

class ExcelFileBuilder<T: Any> internal constructor(grid: Grid<T>, columnHeaders: Map<Grid.Column<T>, String>?) : FileBuilder<T>(grid, columnHeaders) {
    override val fileExtension: String
        get() = ".xls"

    private lateinit var workbook: Workbook
    private lateinit var sheet: Sheet

    private var row: Row? = null
    private var cell: Cell? = null

    private var boldStyle: CellStyle? = null

    private var rowNr = 0
    private var colNr = 0


    override fun writeToFile() {
        try {
            workbook.write(FileOutputStream(file))
        } catch (e: Exception) {
            LoggerFactory.getLogger(this.javaClass).error("Error writing excel file", e)
        }
    }

    override fun onNewRow() {
        row = sheet.createRow(rowNr)
        rowNr++
        colNr = 0
    }

    override fun onNewCell() {
        cell = row?.createCell(colNr)
        colNr++
    }

    override fun addEmptyCell() {
        onNewCell()
        cell?.cellType = Cell.CELL_TYPE_BLANK
    }

    override fun buildCell(value: Any?) {
        when (value) {
            null -> {
                cell?.cellType = Cell.CELL_TYPE_BLANK
            }
            is Boolean -> {
                cell?.setCellValue((value))
                cell?.cellType = Cell.CELL_TYPE_BOOLEAN
            }
            is Calendar -> {
                cell?.setCellValue(value.time)
                cell?.cellType = Cell.CELL_TYPE_STRING
            }
            is Double -> {
                cell?.setCellValue((value))
                cell?.cellType = Cell.CELL_TYPE_NUMERIC
            }
            else -> {
                cell?.setCellValue(value.toString())
                cell?.cellType = Cell.CELL_TYPE_STRING
            }
        }
    }

    override fun buildColumnHeaderCell(header: String) {
        buildCell(header)
        cell?.cellStyle = getBoldStyle()
    }

    private fun getBoldStyle(): CellStyle? {
        if (boldStyle == null) {
            val bold = workbook.createFont()
            bold.boldweight = Font.BOLDWEIGHT_BOLD
            boldStyle = workbook.createCellStyle()
            boldStyle?.setFont(bold)
        }

        return boldStyle
    }

    override fun buildFooter() {
        for (i in 0 until numberOfColumns) {
            sheet.autoSizeColumn(i)
        }
    }

    override fun resetContent() {
        workbook = HSSFWorkbook()
        sheet = workbook.createSheet()
        colNr = 0
        rowNr = 0
        row = null
        cell = null
        boldStyle = null
    }
}