package org.vaadin.haijian.helpers

import java.time.LocalDate

class Person {
    var name: String = ""
    var birthday: LocalDate = LocalDate.MIN
    var email: String = ""
    var age = 0

    constructor(name: String, birthday: LocalDate, email: String, age: Int) : super() {
        this.name = name
        this.birthday = birthday
        this.email = email
        this.age = age
    }

    constructor() {}

}