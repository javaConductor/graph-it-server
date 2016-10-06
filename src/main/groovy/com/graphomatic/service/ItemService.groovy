package com.graphomatic.service

import com.graphomatic.domain.*
import com.graphomatic.persistence.DbAccess
import com.graphomatic.security.SecurityService
import com.graphomatic.security.User
import com.graphomatic.typesystem.TypeSystem
import com.graphomatic.typesystem.domain.Group
import com.graphomatic.typesystem.domain.ItemType
import com.graphomatic.util.DataLoader
import com.mongodb.DBAddress
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

/**
 * Created by lcollins on 6/28/2015.
 */
@Slf4j
@Service
trait  ItemService {

    GraphItem getGraphItem(String id) {
        return readTransform(this.dbAccess.getGraphItem(id))
    }

    boolean removeGraphItem(User u, String id) {
        GraphItem item = getGraphItem(id);
        if (item) {
            item.status = GraphItemStatus.Deleted.name();
            def removed = this.dbAccess.update(item);
            def evt = new Event(EventType.ViewItemMoved,
                    u,
                    [id],
                    "${u.username} removed item ${item.title}{$id)")
            eventService.addEvent(evt)
            removed
        } else
            throw new IllegalArgumentException("No such item: id=$id")
    }

    List<GraphItem> getAllGraphItems() {
        return this.dbAccess.getAllGraphItems()
                .collect(this.&readTransform)
    }

    List<GraphItem> getGraphItemsForUser(int pageSize, int pageNumber, User user) {
        return this.dbAccess.getAllGraphItemsForUser(pageSize, pageNumber, user)
                .collect(this.&readTransform)
                .findAll { item ->
            securityService.userCanViewItem(user, item)
        }
    }

    List<GraphItem> getPublicGraphItems(int pageSize, int pageNumber) {
        return this.dbAccess.getPublicGraphItems(pageSize, pageNumber)
                .collect(this.&readTransform)
    }

    GraphItem setAsMainImage(User u, String graphItemId, String imageId) {
        GraphItem graphItem = this.dbAccess.getGraphItem(graphItemId)
        if (!graphItemId)
            return null

        def both = graphItem.images.split { img ->
            img.id == imageId
        }
        if (both[0].empty)
            return null
        graphItem.images = (both[0]) + (both[1])
        updateGraphItem(u, graphItem)
    }

    GraphItem updateGraphItemNotes(User u, String graphItemId, String notes) {
        if (securityService.userCanUpdateItem(u, getGraphItem(graphItemId))) {
            this.dbAccess.updateGraphItemNotes(graphItemId, notes)
        }
    }

    GraphItem readTransform(GraphItem graphItem) {
        if (graphItem.typeName)
            graphItem.type = typeSystem.resolveType(graphItem.typeName)
        graphItem
    }

    GraphItem writeTransform(GraphItem graphItem) {
        if (!graphItem.typeName)
            graphItem.typeName = TypeSystem.BASE_TYPE_NAME
        graphItem
    }

    GraphItem updateGraphItemPosition(User u, String graphItemId, long x, long y) {
        def item = readTransform(this.dbAccess.updatePosition(u, graphItemId, x, y));
        def evt = new Event(EventType.ViewItemMoved,
                [graphItemId],
                "${u.username} moved item $graphItemId to $x, $y")
        eventService.addEvent(evt)
        writeTransform(item)
    }

    GraphItem updateGraphItem(User u, GraphItem graphItem) {
        def updated = writeTransform(this.dbAccess.update(graphItem));
        eventService.addEvent(new Event(
                EventType.ItemUpdated, u, [graphItem.id], "User: ${u.username} updated item: ${graphItem.title}(${graphItem.id})"))
        return updated
    }

    GraphItem createGraphItem(User u, GraphItem graphItem) {
        // before we write it, lets check it
        if (graphItem.data) {//TODO  check the value and return the error message
            def type = graphItem.type ?: typeSystem.resolveType(graphItem.typeName);
            if (!typeSystem.validateProperties(type, graphItem.data)) {
                throw new ValidationException("Invalid item properties.")
            }
        }
        ///TODO should we store the data raw or in Property objects?????
        /// maybe we should create the Property objects here
        def savedItem = readTransform(this.dbAccess.createGraphItem( writeTransform(graphItem)))
        eventService.addEvent(new Event(
                EventType.ItemUpdated, u, [savedItem.id], "User: ${u.username} updated item: ${savedItem.title}(${savedItem.id})"))
        savedItem
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // TestData
    //////////////////////////////////////////////////////////////////////////////////////////////////
    List testDataFiles = [
            "sword.data.json"
    ]

    Map getTestData(){
        [
            "Categories"   : [
                    "People"     : [
                            children: [
                                    Family : [:],
                                    Peers  : [:],
                                    Friends: [:]
                            ]
                    ],
                    "Electronics": [:],
                    "Music"      : [:],
                    "Location"   : [:],
                    "Business"   : [:],
                    "Software"   : [
                            "children": [
                                    "Software Design"     : [:],
                                    "Software Deployment" : [:],
                                    "Software Development": [
                                            "children": [
                                                    "Embedded App"    : [:],
                                                    "Desktop App"     : [:],
                                                    "Web App"         : [:],
                                                    "Web Service"     : [:],
                                                    "Phone/Tablet App": [:]
                                            ]
                                    ]
                            ]
                    ]
            ],
            "Types"        : [
                    Person                   : [
                            categories: ["People"],
                            properties: [
                                    "First Name": [type: "text", required: true],
                                    "Last Name" : [type: "text", required: true],
                                    "email"     : [type: "emailAddress", required: false]
                            ]
                    ],
                    "Electronics"            : [
                            categories: ["ALL"],
                            properties: [
                                    "Serial Number": [type: 'text', required: true],
                                    "Manufacturer" : [type: 'text', required: false],
                                    "Vendor"       : [type: 'text', required: false]
                            ]
                    ],
                    "Computer"               : [
                            parent    : "Electronics",
                            properties: [
                                    "Host Name"       : [type: 'text', required: true],
                                    "Operating System": [type: 'text', required: false],
                                    "Memory (mb)"     : [type: 'number', required: false],
                                    "IP Address"      : [type: 'text', required: false],
                                    "Description"     : [type: 'text', required: false]
                            ]
                    ],

                    "Smart Phone"            : [
                            parent    : "Electronics",
                            properties: [
                                    "Phone Number": [type: 'text', required: true],
                            ]
                    ],
                    "Tablet"                 : [
                            parent    : "Electronics",
                            properties: [:]
                    ],
                    "ServerComputer"         : [
                            parent    : "Computer",
                            properties: [:]
                    ],
                    "ClientComputer"         : [
                            parent    : "Computer",
                            properties: [:]
                    ],
                    "Load Balancer"          : [
                            parent    : "Computer",
                            properties: [:]
                    ],
                    "Software"               : [
                            parent    : '$thing',
                            properties: [
                                    "Name"                    : [type: 'text', required: true],
                                    "Primary Language"        : [type: 'text', required: false],
                                    "Memory Requirements (mb)": [type: 'number', required: false],
                                    "IP Address"              : [type: 'text', required: false],
                                    "Description"             : [type: 'text', required: false]
                            ]
                    ],
                    "Application Environment": [
                            parent                : '$thing',
                            properties            : [
                                    "Name" : [type: 'text', required: true],
                                    "Nodes": [type: 'Server Computer', required: true, collectionType: "list"],
                            ],
                            "Software Application": [
                                    parent: "Software"

                            ],
                            "Software Module"     : [
                                    parent: "Software"
                            ],
                            "Function"            : [
                                    categories: ["Software Development"],
                                    properties: [
                                            "Name"       : [type: 'text', required: true],
                                            "Description": [type: 'text', required: false]
                                    ]
                            ]
                    ],

                    Verse                    : [
                            properties: [
                                    verseId: [name: "verseId", type: 'text', required: true],
                                    book   : [name: "book", type: 'number', required: true],
                                    chapter: [name: "chapter", type: 'number', required: true],
                                    verse  : [name: "verse", type: 'number', required: true],
                            ]
                    ]
            ],
            "Relationships": [
                    "Family"        : [
                            "type"      : "simple",
                            "categories": ["Family"],
                            "notes"     : "Family"
                    ],
                    "Spouse"        : [
                            "type"      : "simple",
                            "parent"    : "Family",
                            "categories": [
                                    "Family"
                            ]
                    ],
                    "Wife"          : [
                            "type"        : "simple",
                            "parent"      : "Spouse",
                            "reversedName": "Husband",
                            "categories"  : [
                                    "Family"
                            ]
                    ],
                    "Husband"       : [
                            "type"        : "simple",
                            "parent"      : "Spouse",
                            "reversedName": "Wife",
                            "categories"  : [
                                    "Family"
                            ]
                    ],
                    "Child"         : [
                            "type"        : "simple",
                            "reversedName": "Parent",
                            "categories"  : [
                                    "Family"
                            ],
                            "parent"      : "Family",
                            "notes"       : ""
                    ],
                    "Son"           : [
                            "type"        : "simple",
                            "reversedName": "Parent",
                            "categories"  : [
                                    "Family"
                            ],
                            "parent"      : "Child",
                            "notes"       : ""
                    ],
                    "Daughter"      : [
                            "type"        : "simple",
                            "reversedName": "Parent",
                            "categories"  : [
                                    "Family"
                            ],
                            "parent"      : "Child",
                            "notes"       : ""
                    ],
                    "Parent"        : [
                            "type"        : "simple",
                            "reversedName": "Child",
                            "categories"  : [
                                    "Family"
                            ],
                            "parent"      : "Family",
                            "notes"       : ""
                    ],
                    "Father"        : [
                            "type"        : "simple",
                            "reversedName": "Child",
                            "parent"      : "Parent",
                            "categories"  : [
                                    "Family"
                            ],
                            "notes"       : ""
                    ],
                    "Mother"        : [
                            "type"        : "simple",
                            "reversedName": "Child",
                            "parent"      : "Parent",
                            "categories"  : [
                                    "Family"
                            ],
                            "notes"       : ""
                    ],

                    "Sibling"       : [
                            "type"        : "simple",
                            "reversedName": "Sibling",
                            "parent"      : "Family",
                            "categories"  : [
                                    "Family"
                            ],
                            "notes"       : ""
                    ],

                    "Brother"       : [
                            "type"        : "simple",
                            "reversedName": "Sibling",
                            "parent"      : "Sibling",
                            "categories"  : [
                                    "Family"
                            ],
                            "notes"       : ""
                    ],
                    "Sister"        : [
                            "type"        : "simple",
                            "reversedName": "Sibling",
                            "parent"      : "Sibling",
                            "categories"  : [
                                    "Family"
                            ],
                            "notes"       : ""
                    ],

                    "Aunt"          : [
                            "type"      : "simple",
                            "parent"    : "Family",
                            "categories": [
                                    "Family"
                            ],
                            "notes"     : ""
                    ],
                    "Uncle"         : [
                            "type"      : "simple",
                            "parent"    : "Family",
                            "categories": [
                                    "Family"
                            ],
                            "notes"     : ""
                    ],
                    "In Law"        : [
                            "type"      : "simple",
                            "parent"    : "Family",
                            "categories": [
                                    "Family"
                            ],
                            "notes"     : ""
                    ],


                    "Brother In Law": [
                            "type"      : "simple",
                            "parent"    : "In Law",
                            "categories": [
                                    "Family"
                            ],
                            "notes"     : ""
                    ],


                    "Sister In Law" : [
                            "type"      : "simple",
                            "parent"    : "In Law",
                            "categories": [
                                    "Family"
                            ],
                            "notes"     : ""
                    ],

                    "Mother In Law" : [
                            "type"      : "simple",
                            "parent"    : "In Law",
                            "categories": [
                                    "Family"
                            ],
                            "notes"     : ""
                    ],

                    "Father In Law" : [
                            "type"      : "simple",
                            "parent"    : "In Law",
                            "categories": [
                                    "Family"
                            ],
                            "notes"     : ""
                    ],


                    "Composed Of"   : [
                            "type"        : "simple",
                            "reversedName": "Part Of",
                            "categories"  : [],
                            "notes"       : ""
                    ],
                    "Part Of"       : [
                            "type"        : "simple",
                            "reversedName": "Composed Of",
                            "categories"  : [],
                            "parent"      : "Family",
                            "notes"       : ""
                    ],
                    "Aggregates"    : [
                            "type"        : "simple",
                            "reversedName": "Child",
                            "parent"      : "Uses",
                            "notes"       : ""
                    ],
                    "Uses"          : [
                            "type"      : "simple",
                            "categories": [],
                            "notes"     : ""
                    ],
                    "Resides At"    : [
                            "type"      : "simple",
                            "categories": [
                                    "Location"
                            ],
                            "parent"    : "Family",
                            "notes"     : ""
                    ],
                    "Located At"    : [
                            "type"      : "simple",
                            "categories": [
                                    "Location"
                            ],
                            "notes"     : ""
                    ],
                    "Calls"         : [
                            "type"      : "simple",
                            "categories": [
                                    "Software Design"
                            ],
                            "parent"    : "Uses",
                            "notes"     : ""
                    ],
                    "Called By"     : [
                            "type"      : "simple",
                            "categories": [
                                    "Software"
                            ],
                            "parent"    : "Uses",
                            "notes"     : ""
                    ]
            ],
            "GroupDef"     : [:]
    ]
}
    def createTestData() {

        String testDataString = new JsonBuilder(getTestData()).toPrettyString()
        /// Read this from config files
        DataLoader loader = new DataLoader(this, typeSystem)
        loader.loadData new ByteArrayInputStream(testDataString.bytes)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // ImageData
    //////////////////////////////////////////////////////////////////////////////////////////////////

    ImageData getItemData(String path) {
        this.dbAccess.getImageData(path)
    }

    ImageData createItemImage(User u, String graphItemId,
                              InputStream inputStream,
                              String contentType) {

        ImageData imageData = this.dbAccess.createItemImage(graphItemId, contentType, inputStream)
        GraphItem graphItem = getGraphItem(graphItemId);
        eventService.addEvent(new Event(
                EventType.ItemImageCreated, u, [graphItemId], "User: ${u.username} created image ${imageData.id} for item: ${graphItem.title}(${graphItem.id})"))
        imageData
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Item Type
    //////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * @param graphItemId
     * @param imageId
     * @return
     */
    ItemType getItemTypeByName(String name) {
        ItemType itemType = this.dbAccess.getItemTypeByName(name)
        postProcess(itemType)
    }

    ItemType getAllItemTypes() {
        this.dbAccess.getAllItemTypes().collect(postProcess)
    }

    ItemType createItemType(ItemType itemType, boolean doPostProcess ) {
        if (!itemType.parentName)
            itemType.parentName = TypeSystem.BASE_TYPE_NAME
        ItemType saved = (this.dbAccess.createItemType(itemType))
        if (doPostProcess)
            saved = postProcess(saved)
        saved
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Common
    //////////////////////////////////////////////////////////////////////////////////////////////////
    ItemType postProcess(ItemType itemType) {
        if (itemType) {
            def inherited = typeSystem.getTypePropertiesAndDefaults(itemType.name)
            itemType.defaults = inherited.defaults
            itemType.propertyDefs = inherited.propertyDefs
        }
        itemType
    }

}
