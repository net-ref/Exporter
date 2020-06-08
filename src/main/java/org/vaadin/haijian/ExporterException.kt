package org.vaadin.haijian

internal class ExporterException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, e: Exception) : super(message, e)
}