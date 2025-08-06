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
### Cross product (R × S)
Creates a new relation by combining each tuple from the first relation
with every tuple from the second relation. If the first relation has N tuples
and the second relation has M tuples, the resulting relation will have MxN tuples.

```kotlin
 var allStudentsWithAllColleges
        = studentRelation.cross(collegeRelation) // Student × College
```

### Natural join (R ⋈ S)
Joins tuples from two relations based on equality of values in their common attributes. 

```kotlin
 var studentsWithCollegesWhereTheyApplied
        = studentRelation.naturalJoin(applyRelation) // Student ⋈ Apply
```

### Rename operator (ρ)
Transforms a relation by renaming one or multiple attributes.
It is useful because in relational algebra attributes must have the same name if you need to perform
a natural join or a union.

```kotlin
val studentRelationWithStudentName = studentRelation.rename {
    attributes("sName" to "studentName")
}
```

### Union operator (R ∪ S)
This operator combines all tuples from two relations.
It is required that all attribute names must be the same in both relations.

```kotlin
 // ρ_{name←sName}(π_{sName}(Student)) ∪ ρ_{name←pName}(π_{pName}(Professor))
var allNames = studentNames.union(professorNames)
```

### Difference operator (R - S)
This operator retains all tuples from the first relation that do not match any tuples from the second relation.
It is required that all attribute names must be the same in both relations.

```kotlin
// ρ_{name←sName}(π_{sName}(Student)) - ρ_{name←pName}(π_{pName}(Professor))
var studentNamesThatDoNotMatchProfessorNames
        = studentNames.differenced(professorNames)
```

### Intersection operator (R ∩ S)
This operator retains only matching tuples from both relations, eliminating duplicates.
It is required that all attribute names must be the same in both relations.

```kotlin
// ρ_{name←sName}(π_{sName}(Student)) ∩ ρ_{name←pName}(π_{pName}(Professor))
var studentNamesThatMatchProfessorNames
        = studentNames.intersection(professorNames)
```
