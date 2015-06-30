package com.graphomatic.domain

import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 6/28/2015.
 */
@Document
class Position {
    long x;
    long y;
}
