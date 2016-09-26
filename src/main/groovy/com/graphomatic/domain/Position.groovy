package com.graphomatic.domain

import groovy.transform.ToString
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 6/28/2015.
 */
@ToString
@Document
class Position {
    long x;
    long y;
}
