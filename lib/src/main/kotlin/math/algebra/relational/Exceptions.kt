package math.algebra.relational

import java.lang.RuntimeException

class InvalidAttributeException(message: String) : RuntimeException(message)
class OperationIsNotApplicableToType : RuntimeException()
