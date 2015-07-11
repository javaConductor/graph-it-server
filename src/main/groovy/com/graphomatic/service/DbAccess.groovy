package com.graphomatic.service

import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.ItemRelationship
import com.graphomatic.domain.Position
import com.graphomatic.domain.Relationship
import groovy.util.logging.Slf4j
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

/**
 * Created by lcollins on 6/28/2015.
 */
@Repository
@Slf4j
class DbAccess {

    MongoTemplate mongo;
    DbAccess(MongoTemplate mongo){
        this.mongo = mongo
    }

    /**
     *
     * @return
     */
    List<GraphItem> getAllGraphItems(){
        //TODO fix this - findAll should work!
        mongo.find(Query.query(Criteria.where("_id").exists(true)), GraphItem.class )
//        mongo.findAll( GraphItem.class )
    }

    /**
     *
     * @param title
     * @param x
     * @param y
     * @return
     */
    GraphItem createGraphItem(String title, long x, long y ){
        GraphItem g = new GraphItem(title: title,
                position: new Position(x:x,y:y))
        mongo.insert( g )
        g
    }

    /**
     *
     * @param graphItem
     * @return
     */
    GraphItem createGraphItem(GraphItem graphItem ){
        mongo.insert( graphItem )
        graphItem
    }

    /**
     *
     * @param id
     * @return
     */
    GraphItem getGraphItem(String id){
        mongo.findById(id, GraphItem)
    }

    /**
     *
     * @param id
     * @return
     */
    boolean removeGraphItem(String id){
        mongo.remove(new Query(Criteria.where('id').is(id)))
        return true
    }

    /**
     *
     * @param graphItemId
     * @param x
     * @param y
     * @return
     */
    GraphItem updatePosition(String graphItemId, long x, long y){
        Position p = new Position(x:x,y:y);
        log.debug("GraphItem: $graphItemId position: $p");
        GraphItem updated = mongo.findAndModify(new Query(Criteria.where('id').is(graphItemId)),
                Update.update('position', p ),GraphItem);
        updated
    }

    /**
     *
     * @param graphItem
     * @return
     */
    GraphItem update(GraphItem graphItem){
        mongo.save(graphItem)
        graphItem
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
        def itemRelationship = new ItemRelationship(sourceItemId: sourceItemId, relatedItemId: relatedItemId, relationship: relationship)
        mongo.insert(itemRelationship)
        itemRelationship
    }

    /**
     *
     * @param itemIds
     * @return
     */
    List<ItemRelationship> getRelationshipForItems(List<String> itemIds) {
        /// call mongo to get ItemRelationship where ( (itemRelationship.sourceItemId in itemIds) OR (itemRelationship.sourceItemId in itemIds) )
        mongo.find(new Query(
                Criteria.where("sourceItemId").in(itemIds).orOperator(
                        Criteria.where("relatedItemId").in(itemIds))), ItemRelationship.class)
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
}
