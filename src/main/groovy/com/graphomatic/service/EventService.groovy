package com.graphomatic.service

import com.graphomatic.domain.Event
import com.graphomatic.domain.View
import groovy.util.logging.Slf4j
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service

/**
 * Created by lcollins on 11/9/2015.
 */
@Slf4j
@Service
class EventService {
	MongoTemplate mongo;

	EventService(MongoTemplate mongo){
		this.mongo = mongo
	}

	def addEvent(Event event){
		mongo.save( event );
		event
	}

	View  findView() {

	}

}
