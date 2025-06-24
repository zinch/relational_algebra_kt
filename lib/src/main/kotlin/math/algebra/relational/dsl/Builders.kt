package math.algebra.relational.dsl

import math.algebra.relational.Relation
import math.algebra.relational.Tuple
import kotlin.collections.map
import kotlin.collections.toList

fun relation(block: RelationBuilder.() -> Unit): Relation {
    val builder = RelationBuilder()
    builder.block()
    return builder.build()
}

class RelationBuilder {
    private lateinit var name: String
    private val attributes = mutableListOf<String>()
    private val tuples = mutableListOf<List<Any>>()

    fun name(relationName: String) {
        name = relationName
    }

    fun attributes(block: AttributesBuilder.() -> Unit) {
        val attributesBuilder = AttributesBuilder()
        attributesBuilder.block()
        attributes.addAll(attributesBuilder.build())
    }

    fun tuples(block: TuplesBuilder.() -> Unit) {
        val tuplesBuilder = TuplesBuilder()
        tuplesBuilder.block()
        tuples.addAll(tuplesBuilder.build())
    }

    fun build(): Relation {
        return Relation(name,attributes.toList(), tuples.map { Tuple(it) }.toList())
    }
}

class TuplesBuilder {
    private val tuplesList = mutableListOf<List<Any>>()

    fun tuple(vararg values: Any) {
        tuplesList.add(values.toList())
    }

    fun build(): List<List<Any>> {
        return tuplesList
    }
}

class ProjectionBuilder {
    private val attributes = mutableListOf<String>()

    fun build(): List<String> {
        return attributes
    }

    fun attributes(block: AttributesBuilder.() -> Unit) {
        val attributesBuilder = AttributesBuilder()
        attributesBuilder.block()
        attributes.addAll(attributesBuilder.build())
    }
}

class AttributesBuilder {
    private val attributesList = mutableListOf<String>()
    private var attributeIndex = 0

    fun name(attribute: String) {
        attributesList.add(attribute)
        attributeIndex++
    }

    fun build(): List<String> {
        return attributesList
    }
}
