package com.graphomatic.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 10/25/2015.
 */
@Document
class ViewItem {
    @Id
    String id;
    String itemId;
    transient GraphItem item;
    Position position;
    int height
    int width
    String uiDisplayMode; //( fullscreen|normal|compact )
}
