package com.graphomatic.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 6/29/2015.
 */
@Document
class Property {
    @Id
    String id
    String name
    /**
     * when: either a normal string representation of a value
     * or a reference.  Syntax for a reference is as such: $ref:[itemid]
     * When an item in the list has this syntax is is assumed to be a reference
     *
     */
    Object value // Values and References
    boolean collectionType
}
