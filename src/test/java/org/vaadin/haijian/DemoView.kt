package org.vaadin.haijian

import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route
import org.vaadin.haijian.helpers.GridDemoViewCreator

@Route("")
class DemoView : HorizontalLayout() {
    init {
        width = "100%"
        height = "100%"

        val withNormalGrid = VerticalLayout()
        expand(withNormalGrid)
        withNormalGrid.add(Span("Grid With List data provider"))
        val normalGrid = GridDemoViewCreator.createGridWithListDataProviderDemo()
        withNormalGrid.add(normalGrid)

        val withLazyLoadingGrid = VerticalLayout()
        expand(withLazyLoadingGrid)
        withLazyLoadingGrid.add(Span("Grid With List data provider"))
        val lazyGrid = GridDemoViewCreator.createGridWithLazyLoadingDemo()
        withLazyLoadingGrid.add(lazyGrid)

        val withTreeGrid = VerticalLayout()
        expand(withTreeGrid)
        val treeGrid = GridDemoViewCreator.createTreeGridDemo()
        withTreeGrid.add(Span("Tree Grid with Tree DataProvider"))
        withTreeGrid.add(treeGrid)

        add(withNormalGrid, withLazyLoadingGrid, withTreeGrid)
    }
}