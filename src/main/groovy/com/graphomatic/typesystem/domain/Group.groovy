package com.graphomatic.typesystem.domain


import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 8/17/2015.
 */
@Document
class Group {
    @Id
    String id
    String name
    GroupDef groupDef
    Map<String, Object> groupProperties
}
