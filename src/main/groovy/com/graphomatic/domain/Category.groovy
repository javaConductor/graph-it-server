package com.graphomatic.domain

import groovy.transform.ToString
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 6/30/2015.
 */
//@ToString
@Document
class Category {
    @Id
    String id
    String name
    String description
    Category parent
    List<Category> children

    @Override
    String toString() {
        return "Category( $name )"
    }
}
