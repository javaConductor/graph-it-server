package com.graphomatic.domain

import groovy.transform.ToString
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 7/5/2015.
 * Represents a Relationship type
 */
@ToString
@Document
class Relationship {
    @Id
    String id
    String name
    String reversedName// mainly for when  type=2-way
    String type // normal, 2-way
    String parentName
    List<Category> categories
    Map<String, String> properties

    int priority = 1 //0 -> 10  where lowest number has greater priority and is displayed before higher priorities
    Map constraintTo // { itemType:[],  }
    Map constraintFrom // { itemType:[],  }
}
