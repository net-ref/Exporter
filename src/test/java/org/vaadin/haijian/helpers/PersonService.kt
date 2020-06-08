package org.vaadin.haijian.helpers

import com.vaadin.flow.data.provider.QuerySortOrder
import com.vaadin.flow.data.provider.SortDirection
import java.time.LocalDate
import java.util.*
import java.util.stream.Stream

class PersonService {
    private var persons: MutableList<Person> = mutableListOf()

    fun getPersons(offset: Int, limit: Int, filter: AgeGroup?, list: List<QuerySortOrder>?): Stream<Person> {
        ensureTestData()
        var filtered = persons.stream().filter { p: Person -> filter(p, filter) }

        // Sort by using a stream comparator
        if (list != null) {
            for (o in list) {
                var comp: Comparator<Person>? = null
                when (o.sorted) {
                    "name" -> comp = Comparator.comparing(Person::name)
                    "age" -> comp = Comparator.comparing(Person::age)
                    "birthday" -> comp = Comparator.comparing(Person::birthday)
                    "email" -> comp = Comparator.comparing(Person::email)
                    else -> {
                    }
                }
                if (o.direction === SortDirection.DESCENDING) {
                    comp = comp!!.reversed()
                }
                filtered = filtered.sorted(comp)
            }
        }
        return filtered.skip(offset.toLong()).limit(limit.toLong())
    }

    fun totalSize(): Int {
        ensureTestData()
        return persons.size
    }

    fun findUsers(start: Int, end: Int): Collection<Person> {
        return persons.subList(start, end)
    }

    private fun ensureTestData() {
        val r = Random()
        persons = ArrayList()
        for (i in 0..4999) {
            val person = Person()
            person.name = firstName[r.nextInt(firstName.size)] + " " + lastName[r.nextInt(lastName.size)]
            person.age = r.nextInt(50) + 18
            person.email = person.name.replace(" ".toRegex(), ".") + "@example.com"

            var date = LocalDate.now().minusYears(person.age.toLong())
            date = date.withMonth(r.nextInt(12) + 1)
            date = date.withDayOfMonth(r.nextInt(28) + 1)
            person.birthday = date

            persons.add(person)
        }
    }

    private fun filter(p: Person, filter: AgeGroup?): Boolean {
        if (filter == null) {
            return true
        }
        val age = p.age
        return filter.minAge <= age && filter.maxAge >= age
    }

    fun countPersons(offset: Int, limit: Int, filter: AgeGroup?): Int {
        ensureTestData()
        val count = persons.stream().filter { p: Person -> filter(p, filter) }.skip(offset.toLong()).limit(limit.toLong()).count()
        return count.toInt()
    }

    companion object {
        private val firstName = arrayOf("James", "John", "Robert", "Michael", "William", "David", "Richard",
                "Charles", "Joseph", "Christopher", "Mary", "Patricia", "Linda", "Barbara", "Elizabeth", "Jennifer",
                "Maria", "Susan", "Margaret", "Dorothy", "Lisa")
        private val lastName = arrayOf("Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson",
                "Moore", "Taylor", "Andreson", "Thomas", "Jackson", "White")
    }
}