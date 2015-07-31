package com.graphomatic.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 7/23/2015.
 */
class ImageData {
    String id
    String contentType
    InputStream inputStream
    long size
}
