package org.vaadin.haijian.helpers

class AgeGroup(var minAge: Int, var maxAge: Int) {
    override fun toString(): String {
        return "$minAge - $maxAge"
    }
}