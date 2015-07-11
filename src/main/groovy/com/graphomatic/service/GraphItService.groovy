package com.graphomatic.service

import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.ItemRelationship
import com.graphomatic.domain.Position
import com.graphomatic.domain.Relationship
import groovy.util.logging.Slf4j

/**
 * Created by lcollins on 6/28/2015.
 */
@Slf4j
class GraphItService {
    DbAccess dbAccess

    def GraphItService(DbAccess dbAccess) {
        this.dbAccess = dbAccess

        if (! getAllGraphItems().findAll { it }.size()){
            try {
                createTestData();
            } catch (Exception e) {
                log.error("Error creating test data.", e)
            }
        }
    }

    GraphItem getGraphItem(String id) {
        return dbAccess.getGraphItem(id);
    }

    boolean removeGraphItem(String id) {
        return dbAccess.removeGraphItem(id);
    }

    List<GraphItem> getAllGraphItems() {
        return dbAccess.getAllGraphItems();
    }

    GraphItem updateGraphItemPosition(String graphItemId, long x, long y) {
        return dbAccess.updatePosition(graphItemId, x, y);
    }


    GraphItem updateGraphItem(GraphItem graphItem) {
        return dbAccess.update(graphItem);
    }

    GraphItem createGraphItem(String title, Position position) {
        return dbAccess.createGraphItem(title,
                position.x,
                position.y);
    }

    GraphItem createGraphItem(GraphItem graphItem) {
        return dbAccess.createGraphItem(graphItem);

    }

    def createTestData() {
        def testData = [
                new GraphItem(title: "Lee Collins",
                        position: new Position(x: 100L, y:100L),
                        data: [:],
                        images: ['/images/manAndWomanBlackLight.jpg']),
                new GraphItem(title: "David Collins",
                        position: new Position(x: 200L, y:100L),
                        data: [:],
                        images: ['/images/metal textures 1920x1200 wallpaper_wallpaperswa.com_73.jpg'])
        ]

        def nuItems = testData.collect { gitem ->
            createGraphItem(gitem)
        }

        /// create Relationship
        def nuRels = [
         new Relationship(name: "Child", type: "simple"),
         new Relationship(name: "Parent", type: "simple"),
        ]

        nuRels.each { r ->
            createRelationship(r);
        }

        dbAccess.createItemRelationship(nuItems[0].id,nuItems[1].id,nuRels[0])

    }

    Relationship createRelationship(Relationship relationship) {
        dbAccess.createRelationship(relationship);
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
}
