package com.graphomatic.security

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 11/9/2015.
 */
@Document
class UserLogin {
	@Id
	String id
	User user;
	Date when;
	String ipAddr;
}
