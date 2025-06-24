package math.algebra.relational

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class RelationTest : DescribeSpec({

    describe("Relation") {
        it("returns relation name") {
            studentRelation.name shouldBe "Student"
        }

        it("returns an index of an attribute") {
            val studentNameAttribute = studentRelation.getAttributeIndex("sName")

            studentNameAttribute shouldBe 1
        }

        it("throws exception when asking for a wrong attribute index") {
            val exception = shouldThrow<InvalidAttributeException> {
                studentRelation.getAttributeIndex("unknown")
            }
            exception.message shouldBe "Invalid attribute: unknown. Existing attributes: sID, sName, GPA, HS"
        }
    }
})
