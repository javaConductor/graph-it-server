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
    String displayName
    boolean required
    boolean readOnly
    /**
     *  if this property refers to an item then assigning this property implies
     *  this relationship between 'this'  item and the reference
     */
    Relationship relationship
    String collectionType // "list" | "map" | null - should be null for single value
    String typeName
}
