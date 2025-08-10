package math.algebra.relational

import math.algebra.relational.dsl.RenameOperatorBuilder
import math.algebra.relational.dsl.ProjectionBuilder

class Relation(
    val name: String,
    val attributes: List<String>,
    val tuples: List<Tuple>
) {
    fun select(block: () -> Predicate? = { null }): Relation {
        val predicate = block() ?: return this

        val newTuples = tuples.filter {
            val attributeAccessor = TupleAttributeAccessorDecorator(this, it)
            predicate.isTrue(attributeAccessor)
        }

        val newName = "σ_{$predicate}($name)"
        return Relation(newName, attributes, newTuples)
    }

    fun project(block: ProjectionBuilder.() -> Unit): Relation {
        val projectionAttributeNames = ProjectionBuilder().apply(block).build()

        projectionAttributeNames.forEach {
            if (it !in attributes) throwInvalidAttributesException(it)
        }

        val projectionIndexes = attributes.withIndex()
            .mapNotNull { (index, existing) -> if (existing in projectionAttributeNames) index else null }

        val newAttributes = attributes.filter { it in projectionAttributeNames }
        val newTuples = tuples
            .map { it.getProjection(projectionIndexes) }
            .distinct()
        val newName = "π_{${newAttributes.joinToString(separator = ",")}}($name)"
        return Relation(newName, newAttributes, newTuples)
    }

    fun cross(relation: Relation): Relation {
        val firstRelAttributes = attributes.map { if (it in relation.attributes) "$name.$it" else it }
        val secondRelAttributes = relation.attributes.map { if (it in attributes) "${relation.name}.$it" else it }

        val newName = "$name × ${relation.name}"
        val newAttributes = firstRelAttributes + secondRelAttributes
        val newTuples = tuples.flatMap { firstTuple ->
            relation.tuples.map { secondTuple -> Tuple(firstTuple.values + secondTuple.values) }
        }
        return Relation(newName, newAttributes, newTuples)
    }

    fun naturalJoin(relation: Relation): Relation {
        val newName = "$name ⋈ ${relation.name}"
        val firstRelAttrs = attributes
        val secondRelAttrs = relation.attributes

        val commonAttributes = firstRelAttrs.toSet().intersect(secondRelAttrs.toSet())
        val secondRelUniqueIndices = secondRelAttrs
            .filter { it !in commonAttributes }
            .map { relation.getAttributeIndex(it) }

        val newTuples = mutableListOf<Tuple>()
        for (firstTuple in tuples) {
            for (secondTuple in relation.tuples) {
                val isMatching = commonAttributes.all {
                    val firstIdx = getAttributeIndex(it)
                    val secondIdx = relation.getAttributeIndex(it)
                    firstTuple[firstIdx] == secondTuple[secondIdx]
                }
                if (isMatching) {
                    val secondTupleValues = secondRelUniqueIndices.map { idx -> secondTuple[idx] }
                    newTuples.add(Tuple(firstTuple.values + secondTupleValues))
                }
            }
        }

        val newAttributes = firstRelAttrs + secondRelAttrs.filter { it !in commonAttributes }
        return Relation(newName, newAttributes, newTuples)
    }

    fun union(relation: Relation): Relation {
        ensureRelationSchemaIsSame(relation)

        val newName = "$name ∪ ${relation.name}"
        return Relation(newName, attributes, tuples + relation.tuples)
    }

    fun difference(relation: Relation): Relation {
        ensureRelationSchemaIsSame(relation)

        val newName = "$name - ${relation.name}"
        val tupleSet = relation.tuples.toSet()
        val newTuples = tuples.filterNot { it in tupleSet }
        return Relation(newName, attributes, newTuples)
    }

    fun intersection(relation: Relation): Relation {
        ensureRelationSchemaIsSame(relation)
        val newName = "$name ∩ ${relation.name}"
        return Relation(newName, attributes, tuples.intersect(relation.tuples).toList())
    }

    fun rename(block: RenameOperatorBuilder.() -> Unit): Relation {
        val builder = RenameOperatorBuilder().apply(block)
        val attributeMappings = builder.attributeMappings

        val (newAttributes, attributesMapping) = if (attributeMappings.isEmpty()) {
            attributes to ""
        } else {
            val mapped = attributes.map { attributeMappings[it] ?: it }
            val mappingStr = attributes
                .filter { it in attributeMappings }
                .joinToString(separator = ",", prefix = "(", postfix = ")") { "${attributeMappings[it]}←$it" }
            mapped to mappingStr
        }
        val fullRelationName = "ρ_{${builder.relationName}$attributesMapping}($name)"
        return Relation(fullRelationName, newAttributes, tuples)
    }

    internal fun getAttributeIndex(attrName: String): Int {
        val attrIdx = attributes.indexOf(attrName)
        if (attrIdx == -1) {
            throwInvalidAttributesException(attrName)
        }
        return attrIdx
    }

    private fun throwInvalidAttributesException(attrName: String): Nothing {
        val message = "Invalid attribute: $attrName. Existing attributes: ${attributes.joinToString()}"
        throw InvalidAttributeException(message)
    }

    override fun toString(): String = "Relations with attributes (${attributes.joinToString()}) and ${tuples.size} tuples"

    private fun ensureRelationSchemaIsSame(relation: Relation) {
        ensureAttributeCountIsEqual(relation)
        ensureAttributeNamesAreEqual(relation)
    }

    private fun ensureAttributeCountIsEqual(relation: Relation) {
        if (attributes.size != relation.attributes.size) {
            throw InvalidAttributeException(
                "Attribute count is different. " +
                        """"$name" - ${attributes.size}, "${relation.name}" - ${relation.attributes.size}."""
            )
        }
    }

    private fun ensureAttributeNamesAreEqual(relation: Relation) {
        if (attributes != relation.attributes) {
            throw InvalidAttributeException(
                "Attribute names are different. " +
                        """"$name" - ${attributes}, "${relation.name}" - ${relation.attributes}."""
            )
        }
    }

    private class TupleAttributeAccessorDecorator(
        private val rel: Relation,
        val tuple: Tuple
    ) : TupleAttributeAccessor {
        override fun getAttributeValue(attrName: String): Any {
            val attrIdx = rel.getAttributeIndex(attrName)
            return tuple[attrIdx]
        }
    }
}

data class Tuple(val values: List<Any>) {
    constructor(vararg values: Any) : this(values.toList())

    operator fun get(index: Int): Any = values[index]

    fun getProjection(projectionIndexes: List<Int>): Tuple {
        return Tuple(this.values.slice(projectionIndexes))
    }
}

interface TupleAttributeAccessor {
    fun getAttributeValue(attrName: String): Any
}
