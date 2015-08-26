package com.graphomatic.typesystem.domain

import com.graphomatic.domain.Relationship
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 8/13/2015.
 */
@Document
class PropertyDef {
    @Id
    String id
    String name
    boolean required
    /**
     * If defined, these are the only relationships that can be
     * referenced my this dataElement.
     * If not defined, there are constraints on relationship
     */
    Relationship relationship
    String collectionType // "list" | "map" | null - should be null for single value
    String typeName
}
