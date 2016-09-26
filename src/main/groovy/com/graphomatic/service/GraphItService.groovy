package com.graphomatic.service

import com.graphomatic.domain.Category
import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.GraphItemStatus
import com.graphomatic.domain.ImageData
import com.graphomatic.domain.ItemRelationship
import com.graphomatic.persistence.DbAccess
import com.graphomatic.typesystem.TypeSystem
import com.graphomatic.typesystem.domain.Group
import com.graphomatic.typesystem.domain.ItemType
import com.graphomatic.domain.Relationship
import com.graphomatic.util.DataLoader
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

/**
 * Created by lcollins on 6/28/2015.
 */
@Slf4j
@Service
class GraphItService {
    DbAccess dbAccess
    TypeSystem typeSystem

    def GraphItService(DbAccess dbAccess, TypeSystem typeSystem) {
        this.dbAccess = dbAccess
        this.typeSystem = typeSystem

        if (!getAllGraphItems().findAll { it }.size()) {
            try {
                createTestData();
            } catch (Exception e) {
                log.error("Error creating test data.", e)
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Category
    //////////////////////////////////////////////////////////////////////////////////////////////////
    List<Category> getCategories() {
        return dbAccess.getCategories();
    }

    Category getCategory(id) {
        return dbAccess.getCategory(id);
    }

    Category updateCategory(Category cat) {
        return dbAccess.updateCategory(cat);
    }

    List<Category> createCategories(List<Category> categories) {
        dbAccess.createCategories(categories);
    }

    Category getCategoryByName(String name) {
        dbAccess.getCategoryByName(name)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Graph Item
    //////////////////////////////////////////////////////////////////////////////////////////////////
    GraphItem getGraphItem(String id) {
        return readTransform(dbAccess.getGraphItem(id))
    }

    boolean removeGraphItem(String id) {
        //TODO only mark it as deleted
        GraphItem item = getGraphItem(id);
        if( item ){
            item.status = GraphItemStatus.Deleted.name();
            return dbAccess.update( item );
        }
        throw new IllegalArgumentException("No such item: id="+id)
    }

    List<GraphItem> getAllGraphItems() {
        return dbAccess.getAllGraphItems().collect(this.&readTransform)
    }

    GraphItem setAsMainImage(String graphItemId, String imageId) {
        GraphItem graphItem = dbAccess.getGraphItem(graphItemId)
        if (!graphItemId)
            return null

        def both = graphItem.images.split { img ->
            img.id == imageId
        }
        if (both[0].empty)
            return null
        graphItem.images = (both[0]) + (both[1])

        dbAccess.update(graphItem)
    }

    GraphItem updateGraphItemNotes(String graphItemId, String notes) {
        dbAccess.updateGraphItemNotes(graphItemId, notes)
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

    GraphItem updateGraphItemPosition(String graphItemId, long x, long y) {
        return readTransform(dbAccess.updatePosition(graphItemId, x, y));
    }

    GraphItem updateGraphItem(GraphItem graphItem) {
        return writeTransform(dbAccess.update(graphItem));
    }

    GraphItem createGraphItem(GraphItem graphItem) {
        // before we write it, lets check it
        if (graphItem.data) {//TODO  check the value and return the error message
            typeSystem.validateProperties(graphItem.type ?: typeSystem.resolveType(graphItem.typeName), graphItem.data)
        }
        ///TODO should we store the data raw or in Property objects?????
        /// maybe we should create the Property objects here
        return readTransform(dbAccess.createGraphItem(writeTransform(graphItem)))
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Group
    //////////////////////////////////////////////////////////////////////////////////////////////////
    Group createGroup(Group group) {
        dbAccess.createGroup(group)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // TestData
    //////////////////////////////////////////////////////////////////////////////////////////////////
    List testDataFiles = [
            "sword.data.json"
    ]

    Map testData = [
            "Categories"   : [
                    "People"  : [
                            children: [
                                    Family : [:],
                                    Peers  : [:],
                                    Friends: [:]
                            ]
                    ],
                    "Electronics"   : [:            ],
                    "Music"   : [:],
                    "Location": [:],
                    "Business": [:],
                    "Software": [
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
            "Types" : [
                    Person: [
                            categories: ["People"],
                            properties: [
                                    "First Name": [type: "text", required: true],
                                    "Last Name" : [type: "text", required: true],
                                    "email"     : [type: "emailAddress", required: false]
                            ]
                    ],
                    "Electronics": [
                            categories: ["ALL"],
                            properties: [
                                    "Serial Number":  [type: 'text', required: true],
                                    "Manufacturer":  [type: 'text', required: false],
                                    "Vendor":  [type: 'text', required: false]
                            ]
                    ],
                    "Computer": [
                            parent: "Electronics",
                            properties: [
                                        "Host Name":  [type: 'text', required: true],
                                        "Operating System":  [type: 'text', required: false],
                                        "Memory (mb)":  [type: 'number', required: false],
                                        "IP Address":  [type: 'text', required: false],
                                        "Description":  [type: 'text', required: false]
                            ]
                    ],

                    "Smart Phone": [
                            parent: "Electronics",
                            properties: [
                                    "Phone Number":  [type: 'text', required: true],
                            ]
                    ],
                    "Tablet": [
                            parent: "Electronics",
                            properties: [:]
                    ],
                    "ServerComputer" : [
                            parent: "Computer",
                            properties: [:]
                    ],
                    "ClientComputer" : [
                            parent: "Computer",
                            properties: [:]
                    ],
                    "Load Balancer" : [
                            parent: "Computer",
                            properties: [:]
                    ],
                    "Software": [
                            parent: '$thing',
                            properties: [
                                    "Name":  [type: 'text', required: true],
                                    "Primary Language":  [type: 'text', required: false],
                                    "Memory Requirements (mb)":  [type: 'number', required: false],
                                    "IP Address":  [type: 'text', required: false],
                                    "Description":  [type: 'text', required: false]
                            ]
                    ],
                    "Application Environment" : [
                            parent: '$thing',
                            properties: [
                                    "Name":  [type: 'text', required: true],
                                    "Nodes": [type: 'Server Computer', required: true, collectionType: "list"],
                            ],
                    "Software Application" : [
                        parent: "Software"

                        ],
                    "Software Module" : [
                            parent: "Software"
                        ],
                    "Function" : [
                            categories: ["Software Development"],
                            properties: [
                                    "Name":  [type: 'text', required: true],
                                    "Description":[type: 'text', required: false]
                            ]
                        ]
                    ],

                    Verse : [
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
                            "notes"     :   "Family"
                    ],
                    "Spouse": [
                            "type"      : "simple",
                            "parent"      : "Family",
                            "categories"  : [
                                    "Family"
                            ]
                    ],
                    "Wife": [
                            "type"      : "simple",
                            "parent"      : "Spouse",
                            "reversedName": "Husband",
                            "categories"  : [
                                    "Family"
                            ]
                    ],
                    "Husband": [
                            "type"      : "simple",
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
                    "Son"         : [
                            "type"        : "simple",
                            "reversedName": "Parent",
                            "categories"  : [
                                    "Family"
                            ],
                            "parent"      : "Child",
                            "notes"       : ""
                    ],
                    "Daughter"         : [
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

    def createTestData() {

        String testDataString = new JsonBuilder(testData).toPrettyString()
        /// Read this from config files
        DataLoader loader = new DataLoader(this, typeSystem)
        loader.loadData new ByteArrayInputStream(testDataString.bytes)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Relationship
    //////////////////////////////////////////////////////////////////////////////////////////////////
    Relationship createRelationship(Relationship relationship) {
        dbAccess.createRelationship(relationship);
    }

    Relationship getRelationshipDef(String id) {
        dbAccess.getRelationshipDef(id)
    }

    List<Relationship> getRelationshipDefs() {
        dbAccess.getRelationshipDefs();
    }

    Relationship getRelationshipDefByName(String name) {
        if (!name)
            return null
        dbAccess.getRelationshipDefByName(name)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Item Relationship
    //////////////////////////////////////////////////////////////////////////////////////////////////
    List<ItemRelationship> getAllItemRelationships() {
        dbAccess.getAllItemRelationships();
    }

    List<ItemRelationship> getRelationshipsForItems(List<String> itemIds) {
        dbAccess.getRelationshipForItems(itemIds);
    }

    ItemRelationship getItemRelationship(String id) {
        dbAccess.getItemRelationship(id)
    }

    boolean removeItemRelationship(String id) {
        dbAccess.removeItemRelationship(id)
    }

    ItemRelationship createItemRelationship(ItemRelationship itemRelationship) {
        dbAccess.createItemRelationship(itemRelationship.sourceItemId,
                itemRelationship.relatedItemId,
                itemRelationship.relationship);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // ImageData
    //////////////////////////////////////////////////////////////////////////////////////////////////

    ImageData getItemData(String path) {
        dbAccess.getImageData(path)
    }

    ImageData createItemImage(String graphItemId,
                              InputStream inputStream,
                              String contentType) {
        dbAccess.createItemImage(graphItemId, contentType, inputStream)
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
        ItemType itemType = dbAccess.getItemTypeByName(name)
        postProcess(itemType)
    }

    ItemType getAllItemTypes() {
        dbAccess.getAllItemTypes().collect(postProcess)
    }

    ItemType createItemType(ItemType itemType, boolean doPostProcess = true) {
        if (!itemType.parentName)
            itemType.parentName = TypeSystem.BASE_TYPE_NAME
        ItemType saved = (dbAccess.createItemType(itemType))
        if(doPostProcess)
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
