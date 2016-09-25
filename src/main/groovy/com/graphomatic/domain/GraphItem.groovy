package com.graphomatic.domain

import com.graphomatic.security.User
import com.graphomatic.typesystem.domain.ItemType
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
    Position position = new Position(x:100, y: 200);

    List<Category> categories;
    String notes
//    Map<String, Property> data;
    Map<String, Object> data;
    String typeName
    transient ItemType type
    String ownerName
    Map accessMap = [
            owner:"W",
            group:"W",
            public:"R",
    ]
    String access //: O/G/P (owner,group,public) x RW - upto 3 chars
    String visibility //: O/G/P (owner,group,public) -

}
