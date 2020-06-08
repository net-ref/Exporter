package org.vaadin.haijian

import com.vaadin.flow.component.grid.Grid
import java.io.FileWriter
import java.io.IOException

class CSVFileBuilder<T: Any> internal constructor(grid: Grid<T>, columnHeaders: Map<Grid.Column<T>, String>?) : FileBuilder<T>(grid, columnHeaders) {
    private lateinit var writer: FileWriter
    private var rowNr = 0
    private var colNr = 0

    override val fileExtension: String
        get() = ".csv"

    override fun resetContent() {
        colNr = 0
        rowNr = 0
        writer = FileWriter(file)
    }

    override fun addEmptyCell() {
        writer.append(",")
    }

    override fun buildCell(value: Any?) {
        when {
            value == null -> {
                writer.append("")
            }
            value.toString().contains(",") -> {
                writer.append("\"").append(value.toString()).append("\"")
            }
            else -> {
                writer.append(value.toString())
            }
        }
    }

    override fun writeToFile() {
        try {
            writer.flush()
        } catch (e: IOException) {
            throw ExporterException("Failed to write to file", e)
        } finally {
            cleanupResource()
        }
    }

    private fun cleanupResource() {
        writer.close()
    }

    override fun onNewRow() {
        if (rowNr > 0) {
            try {
                writer.append("\n")
            } catch (e: IOException) {
                throw ExporterException("Unable to create a new line", e)
            }
        }
        rowNr++
        colNr = 0
    }

    override fun onNewCell() {
        if (colNr in 1 until numberOfColumns) {
            writer.append(",")
        }
        colNr++
    }

    override fun buildColumnHeaderCell(header: String) {
        writer.append(header)
    }
}