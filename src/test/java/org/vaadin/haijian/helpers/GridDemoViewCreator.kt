package org.vaadin.haijian.helpers

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.treegrid.TreeGrid
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.hierarchy.TreeData
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider
import com.vaadin.flow.data.renderer.LocalDateRenderer
import com.vaadin.flow.server.StreamResource
import org.vaadin.haijian.Exporter
import java.util.*

object GridDemoViewCreator {
    private val service = PersonService()
    
    fun createGridWithListDataProviderDemo(): Component {
        return createGridDemo(false)
    }

    fun createGridWithLazyLoadingDemo(): Component {
        return createGridDemo(true)
    }

    private fun createGridDemo(lazyLoading: Boolean): Component {
        val result = VerticalLayout()
        result.setSizeFull()
        
        val groups: MutableList<AgeGroup> = ArrayList()
        groups.add(AgeGroup(0, 18))
        groups.add(AgeGroup(19, 26))
        groups.add(AgeGroup(27, 40))
        groups.add(AgeGroup(41, 100))
        
        val filter = ComboBox("Filter", groups)
        result.add(filter)
        
        val grid = Grid<Person>()
        grid.pageSize = 10
        grid.addColumn(Person::name).setHeader("Name").setKey("name").setSortProperty("name")
        grid.addColumn(Person::email).setHeader("Email").key = "email"
        grid.addColumn(Person::age).setHeader("Age").key = "age"
        grid.addColumn(LocalDateRenderer(Person::birthday)).setHeader("Birthday").key = "birthday"
        result.add(grid)
        result.setHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH, grid)
        if (lazyLoading) {
            setupLazyLoadingDataProviderForGrid(grid, filter)
        } else {
            setupListDataProviderForGrid(grid, filter)
        }
        val downloadAsExcel = Anchor(StreamResource("my-excel.xls", Exporter.exportAsExcel(grid, null)), "Download As Excel")
        val downloadAsCSV = Anchor(StreamResource("my-csv.csv", Exporter.exportAsCSV(grid, null)), "Download As CSV")
        result.add(HorizontalLayout(downloadAsExcel, downloadAsCSV))
        return result
    }

    fun createTreeGridDemo(): Component {
        val result = VerticalLayout()
        result.setSizeFull()
        val grid = TreeGrid<Person>()
        grid.addHierarchyColumn(Person::name).setHeader("Name").setKey("name").setSortProperty("name")
        grid.addColumn(Person::email).setHeader("Email").key = "email"
        grid.addColumn(Person::age).setHeader("Age").key = "age"
        grid.addColumn(LocalDateRenderer(Person::birthday)).setHeader("Birthday").key = "birthday"
        result.add(grid)
        result.setHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH, grid)
        setupTreeDataProviderForGrid(grid)
        val downloadAsExcel = Anchor(StreamResource("my-excel.xls", Exporter.exportAsExcel(grid, null)), "Download As Excel")
        val downloadAsCSV = Anchor(StreamResource("my-csv.csv", Exporter.exportAsCSV(grid, null)), "Download As CSV")
        result.add(HorizontalLayout(downloadAsExcel, downloadAsCSV))
        return result
    }

    private fun setupListDataProviderForGrid(grid: Grid<Person>, filter: ComboBox<AgeGroup>) {
        val listDataProvider = DataProvider.fromStream(service.getPersons(0, 100, null, emptyList()))
        grid.dataProvider = listDataProvider
        filter.addValueChangeListener { e: ComponentValueChangeEvent<ComboBox<AgeGroup>, AgeGroup> ->
            val value = e.value
            val filteredDataProvider = DataProvider.fromStream(service.getPersons(0, 100, value, emptyList()))
            grid.setDataProvider(filteredDataProvider)
        }
    }

    private fun setupLazyLoadingDataProviderForGrid(grid: Grid<Person>, filter: ComboBox<AgeGroup>) {
        val dataProvider = DataProvider.fromFilteringCallbacks(
                { q: Query<Person, AgeGroup> -> service.getPersons(q.offset, q.limit, q.filter.orElse(null), q.sortOrders) }
        ) { q: Query<Person, AgeGroup> -> service.countPersons(q.offset, q.limit, q.filter.orElse(null)) }
        val filterProvider = dataProvider.withConfigurableFilter()
        grid.dataProvider = filterProvider
        filter.addValueChangeListener { e: ComponentValueChangeEvent<ComboBox<AgeGroup>, AgeGroup> ->
            val value = e.value
            filterProvider.setFilter(value)
        }
    }

    private fun setupTreeDataProviderForGrid(grid: TreeGrid<Person>) {
        val data = TreeData<Person>()
        data.addRootItems(service.getPersons(0, 20, null, emptyList()))
        var index = 20
        for (root in data.rootItems) {
            data.addItems(root, service.findUsers(index, index + 5))
            index += 5
        }
        val treeDataProvider = TreeDataProvider(data)
        grid.dataProvider = treeDataProvider
    }
}