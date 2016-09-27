package com.graphomatic.persistence

import com.graphomatic.domain.Event
import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.GraphItemStatus
import com.graphomatic.domain.Position
import com.graphomatic.security.PermissionType
import com.graphomatic.security.User
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.gridfs.GridFsTemplate

/**
 * Created by lee on 9/26/16.
 */
trait ItemDbAccess {
    GridFsTemplate gridFsTemplate
    Criteria activeCriteria = (Criteria.where("status").is(GraphItemStatus.Active.name()))
    Criteria activeOrNewCriteria = activeCriteria.orOperator(Criteria.where("status").is(GraphItemStatus.Active.name()))

    /**
     *
     * @param graphItem
     * @return
     */
    GraphItem createGraphItem(GraphItem graphItem) {
        mongo.insert(graphItem)
        graphItem
    }

    /**
     *
     * @param id
     * @return
     */
    GraphItem getGraphItem(String id) {
        mongo.findById(id, GraphItem)
    }

    /**
     *
     * @param id
     * @return
     */
    boolean removeGraphItem(String id) {
        //TODO mark as deleted until no relationships then we cam remove
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
    GraphItem updatePosition(User u , String graphItemId, long x, long y) {
        Position p = new Position(x: x, y: y);
        log.debug("GraphItem: $graphItemId position: $p");
        GraphItem updated = mongo.findAndModify(new Query(Criteria.where('id').is(graphItemId)),
                Update.update('position', p), GraphItem);

        updated
    }

    /**
     *
     * @param graphItem
     * @return
     */
    GraphItem update(GraphItem graphItem) {
        ///TODO - update is creating dups
        mongo.updateFirst(
                new Query().addCriteria(Criteria.where('_id').is(graphItem.id)),
                new Update().
                        set('title', graphItem.title ?: "").
                        set("notes", graphItem.notes ?: "").
                        set("categories", graphItem.categories ?: []).
                        set("data", graphItem.data ?: [:]), GraphItem)
        graphItem
    }

    /**
     * @param user
     * @return List of graphItems for a user
     */
    List<GraphItem> getAllGraphItemsForUser(User user) {
        Criteria activeCriteria = (Criteria.where("status").exists(false)
                .orOperator(Criteria.where("status").is(GraphItemStatus.Active.name())))
        Criteria userCriteria = (Criteria.where("ownerName").is(user.username))
        mongo.find(Query.query(activeCriteria.andOperator(userCriteria)), GraphItem.class)
    }

    /**
     *
     * @return List of Active graphItems
     */
    List<GraphItem> getAllGraphItems() {
        getAllGraphItems(-1, -1, null);
    }

    /**
     *
     * @param pageSize
     * @param pageNumber
     * @return List of Active graphItems starting at page 'pageNumber' with no more than 'pageSize' items
     */
    List<GraphItem> getAllGraphItems(int pageSize, int pageNumber, User user ) {
        Criteria criteria = activeCriteria;
        if (user)
            criteria = criteria.andOperator(userCriteria(user))
        if (pageNumber < 0)
            mongo.find(Query.query(criteria), GraphItem)
        else
            mongo.find(Query.query(criteria)
                    .skip(pageNumber > 0 ? ((pageNumber - 1) * pageSize) : 0)
                    .limit(pageSize), GraphItem.class);
    }

    Criteria userCriteria(User user){
        (Criteria.where("ownerName").is(user.username))
    }
    /**
     *
     * @param title
     * @param x
     * @param y
     * @return
     */
    GraphItem createGraphItem(String title, long x, long y) {
        GraphItem g = new GraphItem(title: title,
                position: new Position(x: x, y: y))
        g.status = GraphItemStatus.New.name();
        mongo.insert(g)
        g
    }


}
