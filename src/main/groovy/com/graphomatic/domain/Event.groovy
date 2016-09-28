package com.graphomatic.domain

import com.graphomatic.security.User
import com.graphomatic.service.EventType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 6/30/2015.
 */
//@ToString
@Document(collection = "eventLog")
class Event {
    @Id
    String id
    EventType eventType
    String username
    List<String> itemIds
    Date timestamp
    String description

    @Override
    String toString() {
        return "Event( $eventType ) @$timestamp.format('YYYY-MM-DD HH:MM:SS')"
    }

    Event( EventType eventType, User u, String description){
        this.eventType = eventType
        username = u.username
        this.description = description
        timestamp = new Date()
        itemIds = []
    }

    Event(EventType eventType,  List<String> items, String description){
        this.eventType = eventType
        username = "system"
        this.description = description
        timestamp = new Date()
        itemIds = items
    }
}
