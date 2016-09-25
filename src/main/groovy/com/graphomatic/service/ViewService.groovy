package com.graphomatic.service

import com.graphomatic.domain.View
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

/**
 * Created by lcollins on 11/7/2015.
 */
@Slf4j
class ViewService {

	GraphItService graphItService
	DbAccess dbAccess
	def ViewService(DbAccess dbAccess, GraphItService graphItService){
		this.graphItService = graphItService
		this.dbAccess = dbAccess
	}

	View getView( String viewId){
		dbAccess.getView(viewId)
	}


}
