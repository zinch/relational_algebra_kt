package math.algebra.relational

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.containExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import math.algebra.relational.dsl.relation

class RelationalOperatorsTest : DescribeSpec({
    describe("Selection operator") {
        describe("with a 'College' relation") {
            it("selects all colleges") {
                // given
                val relation = collegeRelation.select()

                // when
                relation shouldBe collegeRelation

                // then
                relation.tuples.size shouldBe 7
            }

            it("selects only colleges in California") {
                // when
                val relation = collegeRelation.select {
                    attribute("state") equal "CA"
                }

                // then
                relation.name shouldBe "σ_{state=CA}(College)"
                relation.tuples should containExactly(
                    listOf(
                        Tuple("Stanford University", "CA", 17249),
                        Tuple("University of California Berkeley", "CA", 45057)
                    )
                )
            }

            it("selects nothing from an empty relations") {
                // given
                val emptyRelation = relation {
                    name("Empty")
                }

                // when
                val relation = emptyRelation.select {
                    attribute("state") equal "TX"
                }

                // then
                relation.name shouldBe "σ_{state=TX}(Empty)"
                relation.tuples.size shouldBe 0
            }

            it("selects colleges located in Arizona or having enrollment <= 30K") {
                // when
                val relation = collegeRelation.select {
                    attribute("state") equal "AZ" or
                            (attribute("enr") lte 30_000)
                }

                // then
                relation.name shouldBe "σ_{(state=AZ)∨((enr<30000)∨(enr=30000))}(College)"
                relation.tuples should containExactly(
                    listOf(
                        Tuple("Stanford University", "CA", 17249),
                        Tuple("Harvard University", "MA", 23731),
                        Tuple("Arizona State University", "AZ", 83946),
                    )
                )
            }

            it("selects colleges in California with more than 40K enrollment") {
                // when
                val relation = collegeRelation.select {
                    attribute("state") equal "CA" and
                            (attribute("enr") gte 40_000)
                }

                // then
                relation.name shouldBe "σ_{(state=CA)∧((enr>40000)∨(enr=40000))}(College)"
                relation.tuples should containExactly(
                    listOf(
                        Tuple("University of California Berkeley", "CA", 45057)
                    )
                )
            }
        }
    }

    describe("Projection operator") {
        describe("with an 'Apply' relation") {
            it("returns all student IDs") {
                // when
                val relationWithStudentIds = applyRelation.project {
                    attributes {
                        name("sID")
                    }
                }

                // then
                relationWithStudentIds.name shouldBe "π_{sID}(Apply)"
                relationWithStudentIds.attributes should containExactly("sID")
                relationWithStudentIds.tuples should containExactly(
                    listOf(
                        Tuple(1),
                        Tuple(2),
                        Tuple(3),
                        Tuple(5),
                        Tuple(8),
                        Tuple(13),
                    )
                )
            }

            it("returns unique college names") {
                // when
                val relationWithUniqueCollegeNames = applyRelation.project {
                    attributes {
                        name("cName")
                    }
                }

                // then
                relationWithUniqueCollegeNames.name shouldBe "π_{cName}(Apply)"
                relationWithUniqueCollegeNames.attributes should containExactly("cName")
                relationWithUniqueCollegeNames.tuples should containExactly(
                    listOf(
                        Tuple("Stanford University"),
                        Tuple("University of California Berkeley"),
                        Tuple("University of Texas at Austin"),
                    )
                )
            }

            it("returns unique tuples of major and decision") {
                // when
                val relationWithMajorAndDecision = applyRelation.project {
                    attributes {
                        name("major")
                        name("dec")
                    }
                }

                // then
                relationWithMajorAndDecision.name shouldBe "π_{major,dec}(Apply)"
                relationWithMajorAndDecision.attributes should containExactly("major", "dec")
                relationWithMajorAndDecision.tuples should containExactly(
                    listOf(
                        Tuple("CS", 'Y'),
                        Tuple("CS", 'R'),
                        Tuple("Statistics", 'Y'),
                        Tuple("Fluid Mechanics", 'Y'),
                        Tuple("English", 'R'),
                    )
                )
            }
        }
    }

    describe("Cross-product operator") {
        val crossProduct = studentRelation.cross(applyRelation)

        it("prefixes same attributes name with relation name") {
            crossProduct.name shouldBe "Student × Apply"
            crossProduct.attributes should containExactly(
                "Student.sID", "sName", "GPA", "HS", "Apply.sID", "cName", "major", "dec"
            )
        }

        it("combines all tuples together") {
            crossProduct.tuples should containExactly(
                listOf(
                    Tuple(1, "Sarah Anderson", 3.8, 2100, 1, "Stanford University", "CS", 'Y'),
                    Tuple(1, "Sarah Anderson", 3.8, 2100, 2, "Stanford University", "CS", 'R'),
                    Tuple(1, "Sarah Anderson", 3.8, 2100, 3, "University of California Berkeley", "Statistics", 'Y'),
                    Tuple(1, "Sarah Anderson", 3.8, 2100, 5, "University of California Berkeley", "Fluid Mechanics", 'Y'),
                    Tuple(1, "Sarah Anderson", 3.8, 2100, 8, "University of California Berkeley", "English", 'R'),
                    Tuple(1, "Sarah Anderson", 3.8, 2100, 13, "University of Texas at Austin", "CS", 'R'),
                    Tuple(2, "Michael Chen", 3.9, 1000, 1, "Stanford University", "CS", 'Y'),
                    Tuple(2, "Michael Chen", 3.9, 1000, 2, "Stanford University", "CS", 'R'),
                    Tuple(2, "Michael Chen", 3.9, 1000, 3, "University of California Berkeley", "Statistics", 'Y'),
                    Tuple(2, "Michael Chen", 3.9, 1000, 5, "University of California Berkeley", "Fluid Mechanics", 'Y'),
                    Tuple(2, "Michael Chen", 3.9, 1000, 8, "University of California Berkeley", "English", 'R'),
                    Tuple(2, "Michael Chen", 3.9, 1000, 13, "University of Texas at Austin", "CS", 'R'),
                    Tuple(3, "Emily Rodriguez", 3.7, 2300, 1, "Stanford University", "CS", 'Y'),
                    Tuple(3, "Emily Rodriguez", 3.7, 2300, 2, "Stanford University", "CS", 'R'),
                    Tuple(3, "Emily Rodriguez", 3.7, 2300, 3, "University of California Berkeley", "Statistics", 'Y'),
                    Tuple(3, "Emily Rodriguez", 3.7, 2300, 5, "University of California Berkeley", "Fluid Mechanics", 'Y'),
                    Tuple(3, "Emily Rodriguez", 3.7, 2300, 8, "University of California Berkeley", "English", 'R'),
                    Tuple(3, "Emily Rodriguez", 3.7, 2300, 13, "University of Texas at Austin", "CS", 'R'),
                    Tuple(5, "David Kim", 3.6, 1750, 1, "Stanford University", "CS", 'Y'),
                    Tuple(5, "David Kim", 3.6, 1750, 2, "Stanford University", "CS", 'R'),
                    Tuple(5, "David Kim", 3.6, 1750, 3, "University of California Berkeley", "Statistics", 'Y'),
                    Tuple(5, "David Kim", 3.6, 1750, 5, "University of California Berkeley", "Fluid Mechanics", 'Y'),
                    Tuple(5, "David Kim", 3.6, 1750, 8, "University of California Berkeley", "English", 'R'),
                    Tuple(5, "David Kim", 3.6, 1750, 13, "University of Texas at Austin", "CS", 'R'),
                    Tuple(8, "Rachel Thompson", 3.4, 2000, 1, "Stanford University", "CS", 'Y'),
                    Tuple(8, "Rachel Thompson", 3.4, 2000, 2, "Stanford University", "CS", 'R'),
                    Tuple(8, "Rachel Thompson", 3.4, 2000, 3, "University of California Berkeley", "Statistics", 'Y'),
                    Tuple(8, "Rachel Thompson", 3.4, 2000, 5, "University of California Berkeley", "Fluid Mechanics", 'Y'),
                    Tuple(8, "Rachel Thompson", 3.4, 2000, 8, "University of California Berkeley", "English", 'R'),
                    Tuple(8, "Rachel Thompson", 3.4, 2000, 13, "University of Texas at Austin", "CS", 'R'),
                    Tuple(13, "James Wilson", 4.0, 1900, 1, "Stanford University", "CS", 'Y'),
                    Tuple(13, "James Wilson", 4.0, 1900, 2, "Stanford University", "CS", 'R'),
                    Tuple(13, "James Wilson", 4.0, 1900, 3, "University of California Berkeley", "Statistics", 'Y'),
                    Tuple(13, "James Wilson", 4.0, 1900, 5, "University of California Berkeley", "Fluid Mechanics", 'Y'),
                    Tuple(13, "James Wilson", 4.0, 1900, 8, "University of California Berkeley", "English", 'R'),
                    Tuple(13, "James Wilson", 4.0, 1900, 13, "University of Texas at Austin", "CS", 'R'),
                )
            )
        }
    }
})
