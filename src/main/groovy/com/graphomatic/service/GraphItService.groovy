package com.graphomatic.service

import com.graphomatic.domain.Category
import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.ImageData
import com.graphomatic.domain.ItemRelationship
import com.graphomatic.domain.Property
import com.graphomatic.typesystem.TypeSystem
import com.graphomatic.typesystem.domain.Group
import com.graphomatic.typesystem.domain.ItemType
import com.graphomatic.domain.Position
import com.graphomatic.domain.Relationship
import com.graphomatic.util.DataLoader
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

        if (! getAllGraphItems().findAll { it }.size()){
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
        return readTransform( dbAccess.getGraphItem(id) )
    }

    boolean removeGraphItem(String id) {
        //TODO only mark it as deleted
        return dbAccess.removeGraphItem(id);
    }

    List<GraphItem> getAllGraphItems() {
        return dbAccess.getAllGraphItems().collect( this.&readTransform)
    }

    GraphItem setAsMainImage(String graphItemId, String imageId) {
        GraphItem graphItem = dbAccess.getGraphItem(graphItemId)
        if (! graphItemId)
            return null

        def both = graphItem.images.split { img ->
            img.id == imageId
        }
        if(both[0].empty)
            return null
        graphItem.images = (both[0] ) + ( both[1] )

        dbAccess.update(graphItem)
    }

    GraphItem updateGraphItemNotes(String graphItemId , String notes) {
        dbAccess.updateGraphItemNotes(graphItemId, notes)
    }

    GraphItem readTransform( GraphItem graphItem ){
        if(graphItem.typeName)
            graphItem.type = typeSystem.resolveType(graphItem.typeName)
    }

    GraphItem writeTransform( GraphItem graphItem ){
        if(!graphItem.typeName)
            graphItem.typeName = "Empty"
    }

    GraphItem updateGraphItemPosition(String graphItemId, long x, long y) {
        return dbAccess.updatePosition(graphItemId, x, y);
    }

    GraphItem updateGraphItem(GraphItem graphItem) {
        return dbAccess.update(graphItem);
    }

    GraphItem createGraphItem(GraphItem graphItem) {
        // before we write it, lets check it
        if(graphItem.data){
            typeSystem.validateProperties(  graphItem.type ?:  typeSystem.resolveType(  graphItem.typeName ), graphItem.data )
        }
        return dbAccess.createGraphItem(writeTransform(graphItem ));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Group
    //////////////////////////////////////////////////////////////////////////////////////////////////
    Group createGroup( Group group){
        dbAccess.createGroup( group )
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // TestData
    //////////////////////////////////////////////////////////////////////////////////////////////////
    List testDataFiles = [
            "sword.data.json"
    ]

    Map testData = ["Categories":[],
                    "ItemTypes":[],
                    "GroupDef":[]
    ]
    def createTestData() {

        /// Read this from config files
        DataLoader loader = new DataLoader(this)

        testDataFiles.each { fname ->
            loader.loadData   new FileInputStream( new File("../$fname") )
        }

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
        dbAccess.getRelationshipDefByName(name)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Item Relationship
    //////////////////////////////////////////////////////////////////////////////////////////////////
    List<ItemRelationship> getAllItemRelationships( ) {
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

    ImageData createItemImage(String  graphItemId,
                              InputStream inputStream,
                              String contentType) {
        dbAccess.createItemImage(graphItemId,contentType,inputStream)
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

    ItemType createItemType(ItemType itemType) {
        postProcess(dbAccess.createItemType(itemType))
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    // Common
    //////////////////////////////////////////////////////////////////////////////////////////////////
    ItemType postProcess( ItemType itemType ){
        if(itemType){
            def inherited = typeSystem.getTypePropertiesAndDefaults( itemType.name )
            itemType.defaults = inherited.defaults
            itemType.propertyDefs = inherited.propertyDefs
        }
        itemType
    }

}
