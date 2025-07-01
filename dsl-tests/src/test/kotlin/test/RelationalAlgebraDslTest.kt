package test

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import math.algebra.relational.Tuple
import math.algebra.relational.attribute
import math.algebra.relational.dsl.relation

class RelationalAlgebraDslTest : DescribeSpec({
    val studentRelation = relation {
        name("Student")
        attributes("sID", "sName", "GPA", "HS")
        tuples {
            tuple(1, "Sarah Anderson", 3.8, 2100)
            tuple(2, "Michael Chen", 3.9, 1000)
            tuple(3, "Emily Rodriguez", 3.7, 2300)
            tuple(5, "David Kim", 3.6, 1750)
            tuple(8, "Rachel Thompson", 3.4, 2000)
            tuple(13, "James Wilson", 4.0, 1900)
        }
    }

    val applyRelation = relation {
        name("Apply")
        attributes("sID", "cName", "major", "dec")
        tuples {
            tuple(1, "Stanford University", "CS", 'Y')
            tuple(2, "Stanford University", "CS", 'R')
            tuple(3, "University of California, Berkeley", "Statistics", 'Y')
            tuple(5, "University of California, Berkeley", "Fluid Mechanics", 'Y')
            tuple(8, "University of California, Berkeley", "English", 'R')
            tuple(13, "University of Texas at Austin", "CS", 'R')
        }
    }

    describe("Combination of operators") {
        it("finds names and GPA of students with HS>1000 who applied to CS and were rejected using cross product") {
            val rel = studentRelation.cross(applyRelation).select {
                attribute("Student.sID") equal attribute("Apply.sID") and
                        (attribute("HS") gt 1000) and
                        (attribute("major") equal "CS") and
                        (attribute("dec") equal 'R')
            }.project {
                attributes("sName", "GPA")
            }

            rel.name shouldBe "π_{sName,GPA}(σ_{(((Student.sID=Apply.sID)∧(HS>1000))∧(major=CS))∧(dec=R)}(Student × Apply))"
            rel.tuples should containExactly(
                listOf(
                    Tuple("James Wilson", 4.0)
                )
            )
        }

        it("finds names and GPA of students with HS>1000 who applied to CS and were rejected using natural join") {
            val rel = studentRelation.naturalJoin(applyRelation).select {
                (attribute("HS") gt 1000) and
                        (attribute("major") equal "CS") and
                        (attribute("dec") equal 'R')
            }.project {
                attributes("sName", "GPA")
            }

            rel.name shouldBe "π_{sName,GPA}(σ_{((HS>1000)∧(major=CS))∧(dec=R)}(Student ⋈ Apply))"
            rel.tuples should containExactly(
                listOf(
                    Tuple("James Wilson", 4.0)
                )
            )
        }

    }
})
