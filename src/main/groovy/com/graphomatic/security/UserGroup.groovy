package com.graphomatic.security

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 11/5/2015.
 */
@Document
class UserGroup {
	String name
	@Id
	String id
	Set<User> users
}
