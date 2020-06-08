package org.vaadin.haijian;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.vaadin.haijian.helpers.GridDemoViewCreator;

@Route("")
public class DemoView extends HorizontalLayout {

    public DemoView() {
        setWidth("100%");
        setHeight("100%");

        VerticalLayout withNormalGrid = new VerticalLayout();
        expand(withNormalGrid);
        withNormalGrid.add(new Span("Grid With List data provider"));
        Component normalGrid = GridDemoViewCreator.createGridWithListDataProviderDemo();
        withNormalGrid.add(normalGrid);

        VerticalLayout withLazyLoadingGrid = new VerticalLayout();
        expand(withLazyLoadingGrid);
        Component lazyGrid = GridDemoViewCreator.createTreeGridDemo();
        withLazyLoadingGrid.add(new Span("Tree Grid with Tree DataProvider"));
        withLazyLoadingGrid.add(lazyGrid);
        add(withNormalGrid, withLazyLoadingGrid);
    }
}
