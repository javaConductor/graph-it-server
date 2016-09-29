package com.graphomatic.service

import com.graphomatic.domain.Category
import com.graphomatic.domain.Event
import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.GraphItemStatus
import com.graphomatic.domain.ImageData
import com.graphomatic.domain.ItemRelationship
import com.graphomatic.persistence.DbAccess
import com.graphomatic.security.SecurityService
import com.graphomatic.security.User
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
class GraphItService implements ItemService {
    DbAccess dbAccess
    TypeSystem typeSystem
    SecurityService securityService
    EventService eventService

    def GraphItService(DbAccess dbAccess, TypeSystem typeSystem, SecurityService securityService, EventService eventService) {
        this.dbAccess = dbAccess
        this.typeSystem = typeSystem
        this.securityService = securityService
        this.eventService = eventService

        if (!getAllGraphItems(  ).findAll { it }.size()) {
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
    // Group
    //////////////////////////////////////////////////////////////////////////////////////////////////
    Group createGroup(Group group) {
        dbAccess.createGroup(group)
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

    boolean removeItemRelationship(User u, String id) {
        ItemRelationship itemRelationship = getItemRelationship(id);
        def ret = dbAccess.removeItemRelationship(id)
        if ( ret )
            eventService.addEvent(new Event(
                EventType.RelationshipRemoved, u, [id],
                    "User: ${u.username} removed relationship:${id} (${itemRelationship.sourceItemId} -> ${itemRelationship.relationship.id} -> ${itemRelationship.relatedItemId})")
            )
        ret
    }

    ItemRelationship createItemRelationship(User u, ItemRelationship itemRelationship) {
        ItemRelationship ret = dbAccess.createItemRelationship(itemRelationship.sourceItemId,
                itemRelationship.relatedItemId,
                itemRelationship.relationship);
        eventService.addEvent(new Event(
                EventType.RelationshipCreated, u, [ret.id, itemRelationship.sourceItemId,
                                                   itemRelationship.relatedItemId],
                "User: ${u.username} created relationship:${ret.id} (${itemRelationship.sourceItemId} -> ${itemRelationship.relationship.id} -> ${itemRelationship.relatedItemId})"))
        ret
    }


}
