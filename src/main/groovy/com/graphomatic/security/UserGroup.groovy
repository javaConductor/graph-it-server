package com.graphomatic.security

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 11/5/2015.
 */
@Document
@CompoundIndex(name = "userGroupNameIdx", def = "{'name': 1 }", unique= true, dropDups = true)

class UserGroup {
	String name
	@Id
	String id
	Set<User> users
}
