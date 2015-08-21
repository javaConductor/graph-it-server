package com.graphomatic.typesystem.domain

import com.graphomatic.typesystem.GroupType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 8/11/2015.
 */
@Document
class GroupDef {
    @Id
    String id
    String name
    GroupType type
    List<ItemType> validMemberTypes
    String defaultTemplate
    List<PropertyDef> groupPropertyDefs
}
