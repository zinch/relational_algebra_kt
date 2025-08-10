package math.algebra.relational.dsl

import math.algebra.relational.Relation
import math.algebra.relational.Tuple

fun relation(block: RelationBuilder.() -> Unit): Relation = RelationBuilder().apply(block).build()

class RelationBuilder {
    private lateinit var name: String
    private val attributes = mutableListOf<String>()
    private val tuples = mutableListOf<List<Any>>()

    fun name(relationName: String) {
        name = relationName
    }

    fun attributes(vararg attributeNames: String) {
        attributes.addAll(attributeNames)
    }

    fun tuples(block: TuplesBuilder.() -> Unit) {
        val tuplesBuilder = TuplesBuilder()
        tuplesBuilder.block()
        tuples.addAll(tuplesBuilder.build())
    }

    fun build(): Relation = Relation(name, attributes.toList(), tuples.map { Tuple(it) }.toList())
}

class TuplesBuilder {
    private val tuplesList = mutableListOf<List<Any>>()

    fun tuple(vararg values: Any) {
        tuplesList.add(values.toList())
    }

    fun build(): List<List<Any>> = tuplesList
}

class ProjectionBuilder {
    private val attributes = mutableListOf<String>()

    fun build(): List<String> = attributes

    fun attributes(vararg attributeNames: String) {
        attributes.addAll(attributeNames)
    }
}

class RenameOperatorBuilder {
    private var newRelationName: String = ""
    private var renamedAttributes: Map<String, String> = emptyMap()

    val relationName: String get() = newRelationName
    val attributeMappings: Map<String, String> get() = renamedAttributes.toMap()

    fun attributes(vararg attributeMapping: Pair<String, String>) {
        renamedAttributes = attributeMapping.toMap()
    }

    fun relation(newName: String) { newRelationName = newName }
}
