package com.graphomatic.domain

import org.springframework.data.annotation.Id

/**
 * Created by lcollins on 7/5/2015.
 */
class ItemRelationship {
    @Id
    String id
    String sourceItemId;
    String relatedItemId;
    Relationship relationship;
}
