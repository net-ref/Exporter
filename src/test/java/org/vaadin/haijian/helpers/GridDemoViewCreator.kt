package org.vaadin.haijian.helpers;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.server.StreamResource;
import org.vaadin.haijian.Exporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GridDemoViewCreator {
    final static PersonService service = new PersonService();


    public static Component createGridWithListDataProviderDemo(){
        return createGridDemo(false);
    }

    public static Component createGridWithLazyLoadingDemo(){
        return createGridDemo(true);
    }

    private static Component createGridDemo(boolean lazyLoading) {
        VerticalLayout result = new VerticalLayout();
        result.setSizeFull();

        final List<AgeGroup> groups = new ArrayList<>();
        groups.add(new AgeGroup(0, 18));
        groups.add(new AgeGroup(19, 26));
        groups.add(new AgeGroup(27, 40));
        groups.add(new AgeGroup(41, 100));

        final ComboBox<AgeGroup> filter = new ComboBox<>("Filter", groups);
        result.add(filter);

        final Grid<Person> grid = new Grid<>();
        grid.setPageSize(10);
        grid.addColumn(Person::getName).setHeader("Name").setKey("name").setSortProperty("name");
        grid.addColumn(Person::getEmail).setHeader("Email").setKey("email");
        grid.addColumn(Person::getAge).setHeader("Age").setKey("age");
        grid.addColumn(new LocalDateRenderer<>(Person::getBirthday)).setHeader("Birthday").setKey("birthday");

        result.add(grid);
        result.setHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH, grid);

        if(lazyLoading){
            setupLazyLoadingDataProviderForGrid(grid, filter);
        }else{
            setupListDataProviderForGrid(grid, filter);
        }

        Anchor downloadAsExcel = new Anchor(new StreamResource("my-excel.xls", Exporter.exportAsExcel(grid, null)), "Download As Excel");
        Anchor downloadAsCSV = new Anchor(new StreamResource("my-csv.csv", Exporter.exportAsCSV(grid, null)), "Download As CSV");
        result.add(new HorizontalLayout(downloadAsExcel, downloadAsCSV));

        return result;
    }

    public static Component createTreeGridDemo() {
        VerticalLayout result = new VerticalLayout();
        result.setSizeFull();

        TreeGrid<Person> grid = new TreeGrid<>();
        grid.addHierarchyColumn(Person::getName).setHeader("Name").setKey("name").setSortProperty("name");
        grid.addColumn(Person::getEmail).setHeader("Email").setKey("email");
        grid.addColumn(Person::getAge).setHeader("Age").setKey("age");
        grid.addColumn(new LocalDateRenderer<>(Person::getBirthday)).setHeader("Birthday").setKey("birthday");

        result.add(grid);
        result.setHorizontalComponentAlignment(FlexComponent.Alignment.STRETCH, grid);

        setupTreeDataProviderForGrid(grid);

        Anchor downloadAsExcel = new Anchor(new StreamResource("my-excel.xls", Exporter.exportAsExcel(grid, null)), "Download As Excel");
        Anchor downloadAsCSV = new Anchor(new StreamResource("my-csv.csv", Exporter.exportAsCSV(grid, null)), "Download As CSV");
        result.add(new HorizontalLayout(downloadAsExcel, downloadAsCSV));

        return result;
    }
    private static void setupListDataProviderForGrid(Grid<Person> grid, ComboBox<AgeGroup> filter) {
        ListDataProvider<Person> listDataProvider = DataProvider.fromStream(service.getPersons(0, 100, null, Collections.emptyList()));
        grid.setDataProvider(listDataProvider);

        filter.addValueChangeListener(e -> {
            final AgeGroup value = e.getValue();
            ListDataProvider<Person> filteredDataProvider = DataProvider.fromStream(service.getPersons(0, 100, value, Collections.emptyList()));
            grid.setDataProvider(filteredDataProvider);
        });
    }

    private static void setupLazyLoadingDataProviderForGrid(Grid<Person> grid, ComboBox<AgeGroup> filter) {
        final CallbackDataProvider<Person, AgeGroup> dataProvider = DataProvider.fromFilteringCallbacks(
                q -> service.getPersons(q.getOffset(), q.getLimit(), q.getFilter().orElse(null), q.getSortOrders()),
                q -> service.countPersons(q.getOffset(), q.getLimit(), q.getFilter().orElse(null)));

        ConfigurableFilterDataProvider<Person, Void, AgeGroup> filterProvider = dataProvider.withConfigurableFilter();
        grid.setDataProvider(filterProvider);

        filter.addValueChangeListener(e -> {
            final AgeGroup value = e.getValue();
            filterProvider.setFilter(value);
        });
    }

    private static void setupTreeDataProviderForGrid(TreeGrid<Person> grid) {
        TreeData<Person> data = new TreeData<>();
        data.addRootItems(service.getPersons(0, 20, null, Collections.emptyList()));

        int index = 20;
        for (Person root : data.getRootItems()) {
            data.addItems(root, service.findUsers(index, index + 5));

            index += 5;
        }

        TreeDataProvider<Person> treeDataProvider = new TreeDataProvider<>(data);
        grid.setDataProvider(treeDataProvider);

    }
}
