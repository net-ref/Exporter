# Exporter

Allows you to export data from Grid to an Excel or CSV file
```
new Anchor(new StreamResource("my-excel.xls", Exporter.exportAsExcel(grid)), "Download As Excel");
new Anchor(new StreamResource("my-csv.csv", Exporter.exportAsCSV(grid)), "Download As CSV");
```
## Development instructions

Starting the test/demo server:
```
mvn jetty:run
```

This deploys demo at http://localhost:8080
