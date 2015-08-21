package com.graphomatic.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 7/5/2015.
 */
@Document
class Relationship {
    @Id
    String id
    String name;
    String reversedName;// mainly for when  type=2-way
    String type;
    List<Category> categories;
    int priority = 1; //0 -> 10  where lowest number has greater priority and is displayed before higher priorities
    Map constraintTo // { itemType:[],  }
    Map constraintFrom // { itemType:[],  }
}

//new Relationship([
//    name: "Son",
//    reversedName: "Parent",
//    type: "simple",
//    priority: 1
//])
