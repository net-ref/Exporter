package org.vaadin.haijian

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.server.InputStreamFactory

object Exporter {
    fun <T: Any> exportAsExcel(grid: Grid<T>, columnHeaders: Map<Grid.Column<T>, String>? = null): InputStreamFactory {
        return InputStreamFactory { ExcelFileBuilder(grid, columnHeaders).build() }
    }

    fun <T: Any> exportAsCSV(grid: Grid<T>, columnHeaders: Map<Grid.Column<T>, String>? = null): InputStreamFactory {
        return InputStreamFactory { CSVFileBuilder(grid, columnHeaders).build() }
    }
}