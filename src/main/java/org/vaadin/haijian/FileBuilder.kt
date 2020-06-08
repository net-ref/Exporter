package org.vaadin.haijian

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.binder.BeanPropertySet
import com.vaadin.flow.data.binder.PropertySet
import com.vaadin.flow.data.provider.DataCommunicator
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream

abstract class FileBuilder<T: Any> internal constructor(private val grid: Grid<T>, private val columnHeaders: Map<Grid.Column<T>, String>?) {
    lateinit var file: File
    private var propertySet: PropertySet<T>? = null

    private var columns: Collection<Grid.Column<T>>
    protected val numberOfColumns: Int
        get() = columns.size

    abstract val fileExtension: String

    init {
        columns = grid.columns.stream().filter { column: Grid.Column<T> -> isExportable(column) }.collect(Collectors.toList())

        try {
            val field = Grid::class.java.getDeclaredField("propertySet")
            field.isAccessible = true
            val propertySetRaw = field[grid]
            if (propertySetRaw != null) {
                propertySet = propertySetRaw as PropertySet<T>
            }
        } catch (e: Exception) {
            throw ExporterException("couldn't read propertyset information from grid", e)
        }

        if (columns.isEmpty()) {
            throw ExporterException("No exportable column found, did you remember to set property name as the key for column")
        }
    }

    private fun isExportable(column: Grid.Column<T>): Boolean {
        return (column.isVisible && column.key != null && column.key.isNotEmpty()
                && (propertySet == null || propertySet?.getProperty(column.key)?.isPresent == true))
    }

    fun build(): InputStream {
        return try {
            initTempFile()
            resetContent()
            buildFileContent()
            writeToFile()
            FileInputStream(file)
        } catch (e: Exception) {
            throw ExporterException("An error happened during exporting your Grid", e)
        }
    }

    private fun initTempFile() {
        file = File(TMP_FILE_NAME, fileExtension)
        if (file.exists()) {
            file.delete()
        }

        file = File.createTempFile(TMP_FILE_NAME, fileExtension)
    }

    private fun buildFileContent() {
        buildHeaderRow()
        buildRows()
        buildFooter()
    }

    protected open fun resetContent() {}
    private fun buildHeaderRow() {
        onNewRow()
        if (columnHeaders == null) {
            columns.forEach(Consumer { column: Grid.Column<*> ->
                val key = column.key
                if (key != null) {
                    onNewCell()
                    buildColumnHeaderCell(key)
                } else {
                    LoggerFactory.getLogger(this.javaClass).warn(String.format("Column key %s is a property which cannot be found", column.key))
                }
            })
        } else {
            columns.forEach(Consumer { column: Grid.Column<*> ->
                val columnHeader = columnHeaders[column]
                if (columnHeader != null) {
                    onNewCell()
                    buildColumnHeaderCell(columnHeader)
                } else {
                    LoggerFactory.getLogger(this.javaClass).warn(String.format("Column with key %s have not column header value defined in map", column.key))
                }
            })
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun buildRows() {
        var filter: Any? = null

        try {
            val method = DataCommunicator::class.java.getDeclaredMethod("getFilter")
            method.isAccessible = true
            filter = method.invoke(grid.dataCommunicator)
        } catch (e: Exception) {
            LoggerFactory.getLogger(this.javaClass).error("Unable to get filter from DataCommunicator")
        }

        val streamQuery: Query<T, *>
        val dataStream: Stream<T>

        if (grid.dataProvider !is HierarchicalDataProvider<T, *>) {
            val dataProvider = grid.dataProvider as DataProvider<T, Any>
            streamQuery = Query<T, Any>(
                    0,
                    dataProvider.size(Query(filter)),
                    grid.dataCommunicator.backEndSorting,
                    grid.dataCommunicator.inMemorySorting,
                    null
            )
            dataStream = getDataStream(streamQuery)
            dataStream.forEach { item: T -> buildRow(item, true) }
        } else {
            val dataProvider = grid.dataProvider as HierarchicalDataProvider<T, Any>
            dataProvider.fetch(HierarchicalQuery<T, Any>(filter, null)).forEach { parent: T ->
                buildRow(parent, true)
                dataProvider.fetch(HierarchicalQuery<T, Any>(null, parent)).forEach { child: T ->
                    onNewRow()
                    addEmptyCell()
                    buildRow(child, false)
                }
            }
        }
    }

    private fun buildRow(item: T, addNewRow: Boolean) {
        if (addNewRow) {
            onNewRow()
        }

        if (propertySet == null) {
            propertySet = BeanPropertySet.get(item::class.java) as PropertySet<T>
            columns = columns.stream().filter { column: Grid.Column<T> -> isExportable(column) }.collect(Collectors.toList())
        }

        columns.forEach(Consumer { column: Grid.Column<*> ->
            val propertyDefinition = propertySet!!.getProperty(column.key)
            if (propertyDefinition.isPresent) {
                onNewCell()
                buildCell(propertyDefinition.get().getter.apply(item))
            } else {
                throw ExporterException("Column key: " + column.key + " is a property which cannot be found")
            }
        })
    }

    open fun buildColumnHeaderCell(header: String) {}
    open fun onNewRow() {}
    open fun onNewCell() {}
    open fun buildFooter() {}

    abstract fun addEmptyCell()
    abstract fun buildCell(value: Any?)

    abstract fun writeToFile()

    @Suppress("UNCHECKED_CAST")
    private fun getDataStream(newQuery: Query<T, Any>): Stream<T> {
        var stream = (grid.dataProvider as DataProvider<T, Any>).fetch(newQuery)
        if (stream.isParallel) {
            LoggerFactory.getLogger(DataCommunicator::class.java)
                    .debug("Data provider {} has returned "
                            + "parallel stream on 'fetch' call",
                            grid.dataProvider.javaClass)
            stream = stream.collect(Collectors.toList()).stream()
            assert(!stream.isParallel)
        }
        return stream
    }

    companion object {
        private const val TMP_FILE_NAME = "tmp"
    }
}