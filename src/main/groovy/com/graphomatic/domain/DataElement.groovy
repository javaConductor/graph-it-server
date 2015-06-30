package com.graphomatic.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 6/29/2015.
 */
@Document
class DataElement {
    @Id
    String id
    String name;
    Object value;// Value
    String objectId;//Reference
    String type;// V,R
}
