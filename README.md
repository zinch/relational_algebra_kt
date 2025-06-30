# Relational Algebra DSL in Kotlin
This project demonstrates how to leverage Kotlin DSL support
to construct predicates to use with Relational Algebra operators.

## Defining a relation
Each relation must have a name which helps to later understand where other relations were derived from.
```kotlin
val studentRelation = relation {
    name("Student")
    attributes("sID", "sName", "GPA", "HS")
    tuples {
        tuple(1, "Sarah Anderson", 3.8, 2100)
        tuple(2, "Michael Chen", 3.9, 1000)
        // ...
    }
}
```

## Relational Algebra Operators

### Projection (π)
This operator simply creates a new relation from the original retaining only specified attributes.
Note that duplicate tuples are not allowed in relational algebra, so the new relation will contain only unique tuples.

```kotlin
val newRelation = studentRelation.project {
    attributes("sID", "sName", "GPA")
}
println(newRelation.name) // π_{sID,sName,GPA}(Student)
```

### Selection (σ)
This operator only retains rows from the original relation that satisfy a certain predicate.
Suppose we want to find all students with GPA >= 4.0 and whose high school size is less than 2000.
We need to construct a compound predicate using a DSL.
Parentheses are mandatory to precisely specify the order of predicate evaluation.

```kotlin
val newRelation = studentRelation.select {
    attribute("GPA") gte 4.0 and
            (attribute("HS") lt 2000)
}

println(newRelation.name) // σ_{((GPA>4.0)∨(GPA=4.0))∧(HS<2000)}(Student)
```
