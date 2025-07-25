package math.algebra.relational

import math.algebra.relational.dsl.ProjectionBuilder

class Relation(
    val name: String,
    val attributes: List<String>,
    val tuples: List<Tuple>
) {
    fun select(block: () -> Predicate? = { null }): Relation {
        val predicate = block()

        if (predicate == null) {
            return this
        }

        val newTuples = tuples.filter {
            val attributeAccessor = TupleAttributeAccessorDecorator(this, it)
            predicate.isTrue(attributeAccessor)
        }

        val newName = "σ_{$predicate}($name)"
        return Relation(newName, attributes, newTuples)
    }

    fun project(block: ProjectionBuilder.() -> Unit): Relation {
        val projectionBuilder = ProjectionBuilder()
        projectionBuilder.block()
        val projectionAttributeNames = projectionBuilder.build()

        projectionAttributeNames.forEach {
            if (!attributes.contains(it)) {
                throwInvalidAttributesException(it)
            }
        }

        val projectionIndexes = mutableListOf<Int>()
        for ((index, existingAttributeName) in attributes.withIndex()) {
            if (projectionAttributeNames.contains(existingAttributeName)) {
                projectionIndexes.add(index)
            }
        }

        val newAttributes = attributes.filter { projectionAttributeNames.contains(it) }
        val newTuples = tuples
            .map { it.getProjection(projectionIndexes) }
            .groupBy { it }
            .map { it.key }
        val newName = "π_{${newAttributes.joinToString(separator = ",")}}($name)"
        return Relation(newName, newAttributes, newTuples)
    }

    fun cross(relation: Relation): Relation {
        val firstRelAttributes = attributes.map {
            if (relation.attributes.contains(it)) {
                "$name.$it"
            } else {
                it
            }
        }
        val secondRelAttributes = relation.attributes.map {
            if (attributes.contains(it)) {
                "${relation.name}.$it"
            } else {
                it
            }
        }

        val newName = "$name × ${relation.name}"
        val newAttributes = firstRelAttributes + secondRelAttributes
        val newTuples = tuples.flatMap { firstTuple ->
            relation.tuples.map { secondTuple ->
                Tuple(firstTuple.values + secondTuple.values)
            }
        }
        return Relation(newName, newAttributes, newTuples)
    }

    fun naturalJoin(relation: Relation): Relation {
        val newName = "$name ⋈ ${relation.name}"
        val firstRelationAttributes = attributes.toSet()
        val secondRelationAttributes = relation.attributes.toSet()

        val commonAttributes = firstRelationAttributes.intersect(secondRelationAttributes)
        val secondRelationAttributeIndices = secondRelationAttributes
            .subtract(commonAttributes)
            .map { relation.getAttributeIndex(it) }

        val newTuples = mutableListOf<Tuple>()
        for (firstTuple in tuples) {
            for (secondTuple in relation.tuples) {
                val isMatching: Boolean = commonAttributes.all {
                    val firstIdx = getAttributeIndex(it)
                    val secondIdx = relation.getAttributeIndex(it)
                    firstTuple[firstIdx] == secondTuple[secondIdx]
                }
                if (isMatching) {
                    val secondTupleValues = secondRelationAttributeIndices.map { i -> secondTuple[i] }
                    newTuples.add(Tuple(firstTuple.values + secondTupleValues))
                }
            }
        }

//        val newTuples = tuples.flatMap { firstTuple ->
//            relation.tuples
//                .filter { secondTuple ->
//                    commonAttributes.all {
//                        val firstIdx = getAttributeIndex(it)
//                        val secondIdx = relation.getAttributeIndex(it)
//                        return firstTuple[firstIdx] == secondTuple[secondIdx]
//                    }
//                }
//                .map { secondTuple ->
//                    Tuple(firstTuple.values + secondTuple.values)
//                }
//        }
        return Relation(newName, (firstRelationAttributes + secondRelationAttributes).toList(), newTuples)
    }

    fun getAttributeIndex(attrName: String): Int {
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

    override fun toString(): String {
        return "Relations with attributes (${attributes.joinToString()}) and ${tuples.size} tuples"
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
