package math.algebra.relational

fun attribute(attrName: String): Attribute = Attribute(attrName)

data class Attribute(val name: String) {
    infix fun equal(value: Any): Predicate = Equal(name, value)
    infix fun lt(value: Any): Predicate = LessThan(name, value)
    infix fun lte(value: Any): Predicate = LessThan(name, value) or Equal(name, value)
    infix fun gt(value: Any): Predicate = GreaterThan(name, value)
    infix fun gte(value: Any): Predicate = GreaterThan(name, value) or Equal(name, value)
    override fun toString() = name
}

sealed interface Predicate {
    infix fun and(other: Predicate): Predicate {
        return And(this, other)
    }

    infix fun or(other: Predicate): Predicate {
        return Or(this, other)
    }

    fun isTrue(attributeAccessor: TupleAttributeAccessor): Boolean
}

internal abstract class AttributePredicate(
    val attributeName: String
) : Predicate {
    protected fun ensureAttributeIsComparableWithValue(attribute: TupleAttributeAccessor, value: Any): Comparable<Any> {
        val actualValue = attribute.getAttributeValue(attributeName)

        if (actualValue !is Comparable<*> || value !is Comparable<*>) {
            throw OperationIsNotApplicableToType()
        }

        if (actualValue::class != value::class) {
            throw OperationIsNotApplicableToType()
        }

        // Safe cast since we've verified the classes match
        @Suppress("UNCHECKED_CAST")
        return actualValue as Comparable<Any>
    }
}

internal class Equal(val attributeName: String, val value: Any) : Predicate {
    override fun isTrue(attributeAccessor: TupleAttributeAccessor): Boolean {
        val actualValue = attributeAccessor.getAttributeValue(attributeName)
        val valueToCompareWith = if (value is Attribute) attributeAccessor.getAttributeValue(value.name) else value
        return actualValue == valueToCompareWith
    }

    override fun toString() = "$attributeName=$value"
}

internal class LessThan(attributeName: String, val value: Any) : AttributePredicate(attributeName) {
    override fun isTrue(attributeAccessor: TupleAttributeAccessor): Boolean {
        val valueToCompareWith = if (value is Attribute) attributeAccessor.getAttributeValue(value.name) else value
        val comparableAttribute = ensureAttributeIsComparableWithValue(attributeAccessor, valueToCompareWith)
        return comparableAttribute < valueToCompareWith
    }

    override fun toString() = "$attributeName<$value"
}

internal class GreaterThan(attributeName: String, val value: Any) : AttributePredicate(attributeName) {
    override fun isTrue(attributeAccessor: TupleAttributeAccessor): Boolean {
        val valueToCompareWith = if (value is Attribute) attributeAccessor.getAttributeValue(value.name) else value
        val comparableAttribute = ensureAttributeIsComparableWithValue(attributeAccessor, valueToCompareWith)
        return comparableAttribute > valueToCompareWith
    }

    override fun toString() = "$attributeName>$value"
}

internal data class And(val first: Predicate, val second: Predicate) : Predicate {
    override fun isTrue(attributeAccessor: TupleAttributeAccessor): Boolean {
        return first.isTrue(attributeAccessor) && second.isTrue(attributeAccessor)
    }

    override fun toString() = "($first)∧($second)"
}

internal data class Or(val first: Predicate, val second: Predicate) : Predicate {
    override fun isTrue(attributeAccessor: TupleAttributeAccessor): Boolean {
        return first.isTrue(attributeAccessor) || second.isTrue(attributeAccessor)
    }

    override fun toString() = "($first)∨($second)"
}

fun not(predicate: Predicate): Predicate {
    return Not(predicate)
}

internal data class Not(val predicate: Predicate) : Predicate {
    override fun isTrue(attributeAccessor: TupleAttributeAccessor): Boolean {
        return !predicate.isTrue(attributeAccessor)
    }

    override fun toString() = "¬($predicate)"
}
