package com.graphomatic.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 6/30/2015.
 */
@Document
class Category {
    @Id
    String id
    String name
    Category parent
}
