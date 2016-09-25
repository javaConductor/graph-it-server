package com.graphomatic.security

import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.View
import com.graphomatic.service.DbAccess
import com.graphomatic.typesystem.PrimitiveTypes

/**
 * Created by lcollins on 11/9/2015.
 */
class SecurityService {
	DbAccess dbAccess
	def SecurityService( DbAccess dbAccess ){
		this.dbAccess = dbAccess
	}

	AuthResult authenticate( String username, String password ){

		//TODO implement OAuth 2.0 authentication
		return  testAuthenticate( username, password );
	}

	AuthResult testAuthenticate( String username, String password ){

			if  ( username == "patrick" && password == "adams")
				return new AuthResult(mustChangePasword: false, authToken: "tok.${new Date().time}");

			if  ( username == "change" && password == "password")
				return new AuthResult(mustChangePasword: true, authToken: "tok.${new Date().time}");

		return new AuthResult(mustChangePasword: false);

	}

	UserLogin saveUserLogin(User u, Date when, String ipAddr){
		dbAccess.saveUserLogin(u, when, ipAddr )
	}

	AccessType  getAccess( GraphItem item, User user ){

		/// get the access map of item
		Map accessMap = item.accessMap;

		// check if user is owner
		if ( item.ownerName == user.username ) {
			return accessMap[PermissionType.Owner]
		}

		// check if user in group with access

		// check if item has public access

	}

	AccessType getAccess( View view, User user ){

	}

}
