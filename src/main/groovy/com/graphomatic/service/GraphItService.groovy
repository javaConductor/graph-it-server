package com.graphomatic.service

import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.Position
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

    GraphItem removeGraphItem(String id) {
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
                        images: ['/images/manAndWomanBlackLight.jpg'])
        ]

        testData.each { gitem ->
            createGraphItem(gitem)
        }
    }
}
