package com.graphomatic.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 10/25/2015.
 */
@Document
class View {
    @Id
    String id;
    String name;
    String defaultViewType = "default";
    List<ViewItem> viewItems;
    // protects from editing and/or moving items
    // W | R |
    // change: view items, view attributes ( current viewtype, )
    Map accessMap = [
            owner:"W",
            group:"W",
            public:"R",
    ]

}
