package com.graphomatic.domain

import com.graphomatic.security.AccessType
import com.graphomatic.security.PermissionType
import com.graphomatic.security.User
import com.graphomatic.typesystem.domain.ItemType
import groovy.transform.ToString
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 6/28/2015.
 */
@ToString
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
    String groupName
    Map<String,String> accessMap = [
            (PermissionType.Owner.name()):"W",
            (PermissionType.Group.name()):"W",
            (PermissionType.Public.name()):"R",
    ]
    AccessType access //: O/G/P (owner,group,public) x RW - upto 3 chars
    String visibility //: O/G/P (owner,group,public) -

}
