package com.graphomatic.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 7/23/2015.
 */
@Document
class ItemImage {
    @Id
    String id
    String graphItemId
    String mimeType
    String imagePath // ->  "/image/$gridFsFileId"
}
