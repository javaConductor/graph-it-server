package com.graphomatic.domain

import org.springframework.data.annotation.Id

/**
 * Created by lcollins on 7/5/2015.
 */
class Relationship {
    @Id
    String id
    String name;
    String reversedName;// mainly for when  type=2-way
    String type;
    String[] categories;
    int priority = 1; //0 -> 10  where lowest number has greater priority and is displayed before higher priorities
}

//new Relationship([
//    name: "Son",
//    reversedName: "Parent",
//    type: "simple",
//    priority: 1
//])
