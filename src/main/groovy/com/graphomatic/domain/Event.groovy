package com.graphomatic.domain

import com.graphomatic.security.User
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
    String name
    String username
    List<String> itemIds
    Date timestamp
    String description

    @Override
    String toString() {
        return "Event( $name ) @$timestamp.format('YYYY-MM-DD HH:MM:SS')"
    }

    Event(String eventName, User u, String description){
        name = eventName
        username = u.username
        description = description
        timestamp = new Date()
        itemIds = []
    }

    Event(String eventName, List<String> items, String description){
        name = eventName
        username = "system"
        description = description
        timestamp = new Date()
        itemIds = items
    }
}
