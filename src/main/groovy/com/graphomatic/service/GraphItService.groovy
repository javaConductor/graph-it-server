package com.graphomatic.service

import com.graphomatic.domain.Category
import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.ImageData
import com.graphomatic.domain.ItemImage
import com.graphomatic.domain.ItemRelationship
import com.graphomatic.domain.Position
import com.graphomatic.domain.Relationship
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

/**
 * Created by lcollins on 6/28/2015.
 */
@Slf4j
@Service
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

    List<Category> getCategories() {
        return dbAccess.getCategories();
    }

    Category getCategory(id) {
        return dbAccess.getCategory(id);
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


        //todo FIX THIS   -- Family->parent == People
        def categories = [
                new Category(name: "People",parent: null),
                new Category(name: "Family",parent: null),
                new Category(name: "Music",parent: null)
        ];

        List<Category> nuCategories = createCategories(categories);

        def testData = [
                new GraphItem(title: "Lee Collins",
                        position: new Position(x: 100L, y:100L),
                        data: [],
                        categories: [nuCategories[0]],
                        images: [] ),
                new GraphItem(title: "David Collins",
                        position: new Position(x: 200L, y:100L),
                        data: [],
                        categories: [nuCategories[0]],
                        images: [] )
        ]

        def nuItems = testData.collect { gitem ->
            createGraphItem(gitem)
        }

        String folder="C:/Users/lcollins/git/graph-it/"
        createItemImage(nuItems[0].id,
                new FileInputStream(
                        new File(folder,
                                '/images/manAndWomanBlackLight.jpg')
                ), "image/jpeg"
        )

        createItemImage(nuItems[1].id,
                new FileInputStream(
                        new File(folder,
                                '/images/metal textures 1920x1200 wallpaper_wallpaperswa.com_73.jpg')
                ), "image/jpeg"
        )


        /// create Relationship
        def nuRels = [
         new Relationship(name: "Child", type: "simple", categories: [nuCategories[1]]),
         new Relationship(name: "Parent", type: "simple", categories: [nuCategories[1]]),
        ]

        nuRels.each { r ->
            createRelationship(r);
        }

        dbAccess.createItemRelationship(nuItems[0].id,nuItems[1].id,nuRels[0])

    }

    List<Category> createCategories(List<Category> categories) {
        dbAccess.createCategories(categories);
    }

    Relationship createRelationship(Relationship relationship) {
        dbAccess.createRelationship(relationship);
    }

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

    List<Relationship> getRelationshipDefs() {
        dbAccess.getRelationshipDefs();
    }

    ItemRelationship createItemRelationship(ItemRelationship itemRelationship) {
        dbAccess.createItemRelationship(itemRelationship.sourceItemId,
                itemRelationship.relatedItemId,
                itemRelationship.relationship);
    }

    Relationship getRelationshipDef(String id) {
        dbAccess.getRelationshipDef(id)
    }

    ImageData createItemImage(String  graphItemId,
                              InputStream inputStream,
                              String contentType) {
        dbAccess.createItemImage(graphItemId,contentType,inputStream)
    }

    /**
     *
     * @param graphItemId
     * @param imageId
     * @return
     */
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

    ImageData getItemData(String path) {
        dbAccess.getImageData(path)
    }
//
//    GraphItem addGraphItemNote(String graphItemId, String note) {
//        dbAccess.addItemNote(graphItemId, note);
//    }

    GraphItem updateGraphItemNotes(String graphItemId , String notes) {
        dbAccess.updateGraphItemNotes(graphItemId, notes)
    }
}
