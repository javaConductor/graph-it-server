package com.graphomatic.service

/**
 * Created by lee on 9/28/16.
 */
enum EventType {
    ItemUpdated, ItemCreated,
    ItemImageCreated, ItemImageRemoved,

    ViewItemMoved,ViewItemAdded,
    RelationshipCreated,RelationshipRemoved,

    UserLoggedIn
}
