package com.graphomatic.persistence

import com.graphomatic.domain.Category
import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.GraphItemStatus
import com.graphomatic.domain.ImageData
import com.graphomatic.domain.ItemImage
import com.graphomatic.domain.ItemRelationship
import com.graphomatic.domain.View
import com.graphomatic.security.User
import com.graphomatic.typesystem.domain.Group
import com.graphomatic.typesystem.domain.ItemType
import com.graphomatic.domain.Position
import com.graphomatic.domain.Relationship
import com.mongodb.gridfs.GridFSFile
import groovy.util.logging.Slf4j
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.gridfs.GridFsResource
import org.springframework.stereotype.Repository
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

/**
 * Created by lcollins on 6/28/2015.
 */
@Repository
@Slf4j
class DbAccess implements UserDbAccess, ItemDbAccess {

    final static String GRAPH_ITEM_IMAGE_FOLDER = "/graph-item-images"
    MongoTemplate mongo;
    GridFsTemplate gridFsTemplate
    DbAccess(MongoTemplate mongo, GridFsTemplate gridFsTemplate){
        this.mongo = mongo
        this.gridFsTemplate = gridFsTemplate
    }

    /**
     *
     * @return List of ALL Categories
     */
    List<Category> getCategories(){
        mongo.findAll(Category.class )
    }


    /**
     *
     * @param graphItem
     * @return
     */
    List<Category> createCategories(List<Category> categories ){
        categories.collect { category ->
            createCategory (category)
        }
    }

    /**
     *
     * @param category
     * @return
     */
    Category createCategory(Category category ){
        mongo.insert( category )
        category
    }

    /**
     *
     * @param relationship
     * @return
     */
    Relationship createRelationship(Relationship relationship) {
        mongo.insert(relationship)
        relationship
    }

    /**
     *
     * @param sourceItemId
     * @param relatedItemId
     * @param relationship
     * @return
     */
    ItemRelationship createItemRelationship(String sourceItemId, String relatedItemId, Relationship relationship) {
        def itemRelationship = new ItemRelationship(sourceItemId: sourceItemId,
                relatedItemId: relatedItemId,
                relationship: relationship)
        mongo.insert(itemRelationship)
        itemRelationship
    }

    /**
     *
     * @param itemIds
     * @return
     */
    List<ItemRelationship> getRelationshipForItems(List<String> itemIds) {
        /// call mongo to get ItemRelationship where ( (itemRelationship.sourceItemId in relatedIds) OR (itemRelationship.sourceItemId in relatedIds) )
        mongo.find(new Query(
                Criteria.where("sourceItemId").in(itemIds).orOperator(
                        Criteria.where("relatedItemId").in(itemIds))), ItemRelationship.class)
    }

    List<ItemRelationship> getAllItemRelationships() {
        mongo.findAll(ItemRelationship);
    }

    ItemRelationship getItemRelationship(String id) {
        mongo.findById(id,ItemRelationship)
    }

    boolean removeItemRelationship(String id) {
        try {
            mongo.remove(new Query(
                    Criteria.where("id").is(id)
            ))
            return true
        } catch (Exception e) {
            log.debug("Error removing item-relationship:$id", e);
            return false
        }
    }

    List<Relationship> getRelationshipDefs() {
        mongo.findAll(Relationship);
    }

    Relationship getRelationshipDef(String id) {
        mongo.findById(id, Relationship)
    }

    Category getCategory(String id) {
        mongo.findById(id, Category);
    }

    /**
     * Create an image in GridFs for a graphItem
     * @param graphItemId
     * @param contentType
     * @param inputStream
     * @return
     */
    ImageData createItemImage(String graphItemId, String contentType, InputStream inputStream ) {
        UUID uuid = UUID.randomUUID()
        String fname = uuid.toString()
        GridFSFile gfile = gridFsTemplate.store( inputStream, "$GRAPH_ITEM_IMAGE_FOLDER/$graphItemId/$fname", contentType )

        // create it
        def newImage = new ItemImage(id: fname, graphItemId: graphItemId, mimeType: contentType, imagePath: gfile.filename)
        // add it to the item
        addItemImage(newImage);
        // return the imageData
        getImageData(gfile.filename)
    }

    /**
     *
     * @param itemImage
     * @return GraphItem containing the new ItemImage
     */
    GraphItem addItemImage(ItemImage itemImage){

        if (! itemImage.graphItemId){
            throw new IllegalArgumentException("Graph item id must be present in ItemImage.")
        }
        mongo.findAndModify(
                new Query(Criteria.where('id').is(itemImage.graphItemId)),
                new Update().addToSet("images", itemImage ),
                GraphItem)
    }

    /**
     *
     * @param imagePath
     * @return Returns file at $imagePath
     */
    ImageData getImageData( String imagePath ) {
        GridFsResource resource =  gridFsTemplate.getResource("$imagePath")
        String id = imagePath.split('/').last()
        new ImageData(id: id,
                inputStream: resource.inputStream,
                contentType: resource.contentType,
                size: resource.contentLength())
    }


    GraphItem updateGraphItemNotes(String graphItemId, String notes) {
        GraphItem g = mongo.findAndModify(
                new Query(Criteria.where('id').is(graphItemId)),
                new Update().set("notes", notes ),
                GraphItem)
        g= getGraphItem(graphItemId)
        log.debug("DbAccess: item $graphItemId changed notes to [${notes} result: [${g.notes}]]")
        g
    }

    Category getCategoryByName(String name) {
        mongo.findOne(
                new Query(Criteria.where("name").is(name)),Category
        )
    }

    ItemType getItemTypeByName(String name) {
        mongo.findOne(
                new Query(Criteria.where("name").is(name)),ItemType
        )
    }

    Relationship getRelationshipDefByName(String name) {
        mongo.find(
                new Query(Criteria.where('name').is(name)),Relationship
        )
    }

    ItemType createItemType(ItemType itemType) {
        mongo.insert(itemType)
        itemType
    }

    Group createGroup(Group group) {
        mongo.insert(group)
        group
    }

    List<ItemType> getAllItemTypes() {
        mongo.findAll(ItemType)
    }

    String getTypeOfItem(String s) {
        def item = getGraphItem(s)
        item?.typeName
    }

    ItemType getType(String typeId) {
        mongo.findById(typeId, ItemType)
    }

    ItemType getTypeByName(String name) {
        mongo.findOne(
                new Query(Criteria.where('name').is(name)),ItemType
        )
    }

    Category updateCategory(Category category) {
        mongo.save(category)
        category
    }

    View getView(String id) {
        mongo.findById(id, View)
    }

}
