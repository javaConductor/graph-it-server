package com.graphomatic.domain

import com.mongodb.DBObject
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 6/28/2015.
 */
@Document
class GraphItem {
    @Id
    String id;
    String title;
    List<ItemImage> images;
    Position position;

    List<Category> categories;
    List<String> notes;
    List<DataElement> data;

}
