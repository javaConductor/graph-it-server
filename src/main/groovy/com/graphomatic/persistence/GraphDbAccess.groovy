package com.graphomatic.persistence

import com.graphomatic.domain.Category
import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.ImageData
import com.graphomatic.domain.ItemImage
import com.graphomatic.domain.ItemRelationship
import com.graphomatic.domain.Relationship
import com.graphomatic.domain.View
import com.graphomatic.typesystem.domain.Group
import com.graphomatic.typesystem.domain.ItemType
import com.mongodb.gridfs.GridFSFile
import groovy.util.logging.Commons
import groovy.util.logging.Log
import org.neo4j.graphdb.GraphDatabaseService
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork
import org.springframework.stereotype.Component;

import static org.neo4j.driver.v1.Values.parameters;

/**
 * Created by lee on 1/4/18.
 */
@Commons
@Component
class GraphDbAccess {
    Driver driver;
    MongoTemplate mongo
    GridFsTemplate gridFsTemplate
    GraphDbAccess(MongoTemplate mongo, GridFsTemplate gridFsTemplate, Driver neo4jDriver) {
        this.mongo=mongo
        this.gridFsTemplate=gridFsTemplate
    driver = neo4jDriver
    }

    Category mapToCategory(Map cat){

        if(cat["parent"] != null) {
            new Category(id: cat["id"],
                    name: cat["name"],
                    description: cat["description"],
                    parent: cat["parent"])
        }
        else {
            new Category(id: cat["id"],
                    name: cat["name"],
                    description: cat["description"])
        }
    }


    ItemRelationship mapToItemRelationship(Map item, Map relationship, Map related, Relationship relationshipType){
/*
    String id
    String sourceItemId;
    String relatedItemId;
    Relationship relationship;
*/
            new ItemRelationship( id: relationship["id"],
                    sourceItemId: item["id"],
                    relatedItemId: related["id"],
                    relationship: relationshipType)
    }

    /**
     *
     * @return List of ALL Categories
     */
    List<Category> getCategories(){
        GraphDatabaseService service;
        try{
            Session s = driver.session()
            s.readTransaction(new TransactionWork() {
                @Override
                Object execute(Transaction tx) {
                    StatementResult res =  tx.run("MATCH (c:_Category_) " +
                            "RETURN c")
                    res.list().collect { c -> mapToCategory(c.asMap())}
                }
            })
        } catch (Exception e){

        }
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
    Category createCategory(Category category ) {
            Session s = driver.session()
            s.writeTransaction(new TransactionWork() {
                @Override
                Object execute(Transaction tx) {
                     // create uuid for this category
                    String id = UUID.randomUUID().toString()
                    String statement = "CREATE (category:_Category_ {" +
                            "id:'${id}', " +
                            "name:'${category.name}', " +
                            (category.parent ? "parent:'${category.parent}'," : "") +
                            "description:'${category.description}'" +
                            "  } ) " +
                            //"WITH cat as c "+
                            //"MATCH (category:_Category_ {id:c.id} ) " +
                            "RETURN category"
                    log.debug("Creating category using: [$statement]")
                    StatementResult res =  tx.run(statement)
                    if (res.hasNext()) {
                        def c = res.next().get("category")
                        System.out.println(c.asMap())
                        def cat = mapToCategory( c.asMap() )
                        log.debug("Created category: $cat ")
                        cat
                    }
                    else
                        null
                }
            })
    }


    /**
     *
     * @param category
     * @return
     */
    boolean removeCategory(String categoryId ) {
            Session s = driver.session()
            s.writeTransaction(new TransactionWork() {
                @Override
                Object execute(Transaction tx) {
                     // create uuid for this category
                    String statement = "MATCH (category:_Category_ {" +
                            "id:'${categoryId}'} )  " +
                            "DELETE category " +
                            "RETURN true"
                    log.debug("Deleting category using: [$statement]")
                    tx.run(statement)
                    true
                }
            })
    }

    Category updateCategory(Category category) {
        Session s = driver.session()
        s.writeTransaction(new TransactionWork() {
            @Override
            Object execute(Transaction tx) {
                StatementResult res =  tx.run("MATCH (c:_Category_ {" +
                        "id:'${category.id}'} ) " +
                        "SET " +
                        "name = '${category.name}', " +
                        "parent = '${category.parent}'," +
                        "description = '${category.description}'" +
                        "RETURN c")
                def cat = mapToCategory(res.single().asMap())
                log.debug("Updated category: $cat ")
                cat
            }
        })
    }

    Category getCategory(String id) {
        Session s = driver.session()
        s.writeTransaction(new TransactionWork() {
            @Override
            Object execute(Transaction tx) {
                StatementResult res =  tx.run("MATCH (c:_Category_ {" +
                        "id:'${id}'} ) " +
                        "RETURN c")
                if(!res.hasNext())
                    null
                else {
                   def cat = mapToCategory(res.next().get("c").asMap())
                    log.debug("Updated category: $cat ")
                    cat
                }
            }
        })
    }


    /**
     *
     * @param sourceItemId
     * @param relatedItemId
     * @param relationship
     * @return
     */
    ItemRelationship createItemRelationship(String sourceItemId, String relatedItemId, Relationship relationship) {
        Session s = driver.session()
        Map mRelationshipTypes = [:]
        s.writeTransaction(new TransactionWork() {
            @Override
            Object execute(Transaction tx) {
                // create uuid for this itemRelationship
                String id = UUID.randomUUID().toString()
                String statement =
                        "MATCH (s {id:${sourceItemId} ) " +
                        "MATCH (t {id:${relatedItemId} ) " +
                                "WITH s as source, t as related " +
                        "CREATE itemRelationship (source)-[r:${relationship.name} {}]-(related)" +
                                "RETURN source,related,r"
                log.debug("Creating itemRelationship using: [$statement]")
                StatementResult res =  tx.run(statement)
                if (res.hasNext()) {
                    def result = res.next()
                    def item = result.get("source")
                    def related =  result.get("related")
                    def relationshipRel =  result.get("r")
                    Relationship relationshipType = mRelationshipTypes[relationship['id']]
                    relationshipType = relationshipType ?: getRelationship(relationship['id'])
                    mRelationshipTypes[relationship['id']] = relationshipType
                    ItemRelationship itemRelationship = mapToItemRelationship(item, rela)
                    log.debug("Created itemRelationship: $itemRelationship ")
                    itemRelationship
                }
                else
                    throw new Exception("Could not create itemRelationship.")
            }
        })
    }

    /**
     *
     * @param itemIds
     * @return
     */
    List<ItemRelationship> getRelationshipForItems(List<String> itemIds) {
        Session s = driver.session()
        s.writeTransaction(new TransactionWork() {
            @Override
            Object execute(Transaction tx) {
                String statement =
                        "MATCH (item:Item) - [r] - (t) " +
                                'WHERE item.id IN $itemIds ' +
                                "RETURN item, r, t "
                log.debug("Creating itemRelationship using: [$statement]")
                def params = [itemIds:itemIds]
                StatementResult res =  tx.run( statement, params)
                def ret = []
                while (res.hasNext()) {
                    def result = res.next()
                    def item = result.get("item")
                    def relationship =  result.get("r")
                    def related =  result.get("t")
                    Relationship relationshipType = getRelationship(relationship['id'])
                    def itemRelationship = mapToItemRelationship(item.asMap(),
                            relationship.asMap(),
                            related.asMap(),
                            relationshipType
                    )
                    log.debug("Found itemRelationship: $itemRelationship ")
                    ret += itemRelationship
                }
                ret
            }

        })

    }

    ItemRelationship getItemRelationship(String id) {
        mongo.findById(id,ItemRelationship)
    }

    boolean removeItemRelationship(String id) {
        Session s = driver.session()
        s.writeTransaction(new TransactionWork() {
            @Override
            Object execute(Transaction tx) {
                String statement =
                        "MATCH () - [r {id:$id}] - () " +
                                "DELETE r "
                                "RETURN true "
                log.debug("Deleting itemRelationship using: [$statement]")
                def params = [:]
                StatementResult res =  tx.run( statement, params )
                (res.hasNext())
            }
        })
    }


    /**
     *
     * @param graphItem
     * @return
     */
    GraphItem createGraphItem(GraphItem graphItem) {
        String typeName = "${graphItem.typeName}" ?: "Node"
        graphItem.images.head()
        Session s = driver.session()
        s.writeTransaction(new TransactionWork() {
            @Override
            Object execute(Transaction tx) {
                // create uuid for this item
                String id = UUID.randomUUID().toString()
                Map mProps = [
                    id:id,
                    name: typeName,
                        ownerName: graphItem.ownerName,
                        groupName: graphItem.groupName,
                        groupName: graphItem.accessMap,

                ]
                String statement = "CREATE (item:typeName {" +
                        "id:'$id', " +
                        "name:'$typeName', " +
                        "description:'${graphItem.description}'" +
                        "description:'${category.description}'" +
                        "  } ) " +
                        //"WITH cat as c "+
                        //"MATCH (category:_Category_ {id:c.id} ) " +
                        "RETURN category"
                log.debug("Creating category using: [$statement]")
                StatementResult res =  tx.run(statement)
                if (res.hasNext()) {
                    def c = res.next().get("category")
                    System.out.println(c.asMap())
                    def cat = mapToCategory( c.asMap() )
                    log.debug("Created category: $cat ")
                    cat
                }
                else
                    null
            }
        })



        this.mongo.insert(graphItem)
        graphItem
    }


//
//    List<Relationship> getRelationshipDefs() {
//        mongo.findAll(Relationship);
//    }
//
//    Relationship getRelationshipDef(String id) {
//        mongo.findById(id, Relationship)
//    }
//
//    /**
//     * Create an image in GridFs for a graphItem
//     * @param graphItemId
//     * @param contentType
//     * @param inputStream
//     * @return
//     */
//    ImageData createItemImage(String graphItemId, String contentType, InputStream inputStream ) {
//        UUID uuid = UUID.randomUUID()
//        String fname = uuid.toString()
//        GridFSFile gfile = gridFsTemplate.store( inputStream, "$GRAPH_ITEM_IMAGE_FOLDER/$graphItemId/$fname", contentType )
//
//        // create it
//        def newImage = new ItemImage(id: fname, graphItemId: graphItemId, mimeType: contentType, imagePath: gfile.filename)
//        // add it to the item
//        addItemImage(newImage);
//        // return the imageData
//        getImageData(gfile.filename)
//    }
//
//    /**
//     *
//     * @param itemImage
//     * @return GraphItem containing the new ItemImage
//     */
//    GraphItem addItemImage(ItemImage itemImage){
//
//        if (! itemImage.graphItemId){
//            throw new IllegalArgumentException("Graph item id must be present in ItemImage.")
//        }
//        mongo.findAndModify(
//                new Query(Criteria.where('id').is(itemImage.graphItemId)),
//                new Update().addToSet("images", itemImage ),
//                GraphItem)
//    }
//
//    /**
//     *
//     * @param imagePath
//     * @return Returns file at $imagePath
//     */
//    ImageData getImageData( String imagePath ) {
//        GridFsResource resource =  gridFsTemplate.getResource("$imagePath")
//        String id = imagePath.split('/').last()
//        new ImageData(id: id,
//                inputStream: resource.inputStream,
//                contentType: resource.contentType,
//                size: resource.contentLength())
//    }
//
//
//    GraphItem updateGraphItemNotes(String graphItemId, String notes) {
//        GraphItem g = mongo.findAndModify(
//                new Query(Criteria.where('id').is(graphItemId)),
//                new Update().set("notes", notes ),
//                GraphItem)
//        g= getGraphItem(graphItemId)
//        log.debug("DbAccess: item $graphItemId changed notes to [${notes} result: [${g.notes}]]")
//        g
//    }
//
//    Category getCategoryByName(String name) {
//        mongo.findOne(
//                new Query(Criteria.where("name").is(name)),Category
//        )
//    }
//
//    ItemType getItemTypeByName(String name) {
//        mongo.findOne(
//                new Query(Criteria.where("name").is(name)),ItemType
//        )
//    }
//
//    Relationship getRelationshipDefByName(String name) {
//        mongo.find(
//                new Query(Criteria.where('name').is(name)),Relationship
//        )
//    }
//
//    ItemType createItemType(ItemType itemType) {
//        mongo.insert(itemType)
//        itemType
//    }
//
//    Group createGroup(Group group) {
//        mongo.insert(group)
//        group
//    }
//
//    List<ItemType> getAllItemTypes() {
//        mongo.findAll(ItemType)
//    }
//
//    String getTypeOfItem(String s) {
//        def item = getGraphItem(s)
//        item?.typeName
//    }
//
//    ItemType getType(String typeId) {
//        mongo.findById(typeId, ItemType)
//    }
//
//    ItemType getTypeByName(String name) {
//        mongo.findOne(
//                new Query(Criteria.where('name').is(name)),ItemType
//        )
//    }
//
//
//    View getView(String id) {
//        mongo.findById(id, View)
//    }

}
