package math.algebra.relational

import math.algebra.relational.dsl.relation

val studentRelation = relation {
    name("Student")
    attributes {
        name("sID")
        name("sName")
        name("GPA")
        name("HS")
    }
    tuples {
        tuple(1, "Sarah Anderson", 3.8, 2100)
        tuple(2, "Michael Chen", 3.9, 1000)
        tuple(3, "Emily Rodriguez", 3.7, 2300)
        tuple(5, "David Kim", 3.6, 1750)
        tuple(8, "Rachel Thompson", 3.4, 2000)
        tuple(13, "James Wilson", 4.0, 1900)
    }
}

val collegeRelation = relation {
    name("College")
    attributes {
        name("cName")
        name("state")
        name("enr")
    }
    tuples {
        tuple("Stanford University", "CA", 17249)
        tuple("University of California Berkeley", "CA", 45057)
        tuple("Harvard University", "MA", 23731)
        tuple("University of Texas at Austin", "TX", 51832)
        tuple("Florida International University", "FL", 58786)
        tuple("Arizona State University", "AZ", 83946)
        tuple("University of Washington", "WA", 47400)
    }
}

val applyRelation = relation {
    name("Apply")
    attributes {
        name("sID")
        name("cName")
        name("major")
        name("dec")
    }
    tuples {
        tuple(1, "Stanford University", "CS", 'Y')
        tuple(2, "Stanford University", "CS", 'R')
        tuple(3, "University of California Berkeley", "Statistics", 'Y')
        tuple(5, "University of California Berkeley", "Fluid Mechanics", 'Y')
        tuple(8, "University of California Berkeley", "English", 'R')
        tuple(13, "University of Texas at Austin", "CS", 'R')
    }
}
