package com.graphomatic.service

import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.Position
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

/**
 * Created by lcollins on 6/28/2015.
 */
@Repository
class DbAccess {

    MongoTemplate mongo;
    DbAccess(MongoTemplate mongo){
        this.mongo = mongo
    }

    List<GraphItem> getAllGraphItems(){
        [mongo.findOne(Query.query(Criteria.where("_id").exists(true)), GraphItem.class )]
//        mongo.findAll( GraphItem.class )
    }

    GraphItem createGraphItem(String title, long x, long y ){
        GraphItem g = new GraphItem(title: title,
                position: new Position(x:x,y:y))
        mongo.insert( g )
        g
    }

    GraphItem createGraphItem(GraphItem graphItem ){
        mongo.insert( graphItem )
        graphItem
    }

    GraphItem getGraphItem(String id){
        mongo.findById(id, GraphItem)
    }

    boolean removeGraphItem(String id){
        mongo.remove(new Query(Criteria.where('id').is(id)))
        return true
    }

    GraphItem updatePosition(String graphItemId, long x, long y){
        mongo.findAndModify(new Query(Criteria.where('id').is(graphItemId)),
                Update.update('position', new Position(x:x,y:y)),GraphItem);
    }

    GraphItem update(GraphItem graphItem){
        mongo.save(graphItem)
        graphItem
    }

}
