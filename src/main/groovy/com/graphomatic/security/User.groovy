package com.graphomatic.security

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 11/5/2015.
 */
@Document
//@CompoundIndexes({
//	@CompoundIndex(name = "usernameIdx", def = "{'username': 1 }", unique= true, dropDups = true)
//})
class User {
	@Id
	String id
	String username
	String firstName
	String lastName
	String emailAddress
	String phoneNumber
//TODO what to do about circular-references ? 	Set<UserGroup> groups

	@Override
	boolean equals(Object obj) {
		return super.equals(obj)
	}

	int hashCode() {
		int result
		result = id.hashCode()
		result = 31 * result + username.hashCode()
		result = 31 * result + (firstName != null ? firstName.hashCode() : 0)
		result = 31 * result + (lastName != null ? lastName.hashCode() : 0)
		result = 31 * result + emailAddress.hashCode()
		result = 31 * result + (phoneNumber != null ? phoneNumber.hashCode() : 0)
		return result
	}
}
