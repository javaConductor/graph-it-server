package com.graphomatic.security

import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.View
import com.graphomatic.persistence.DbAccess

/**
 * Created by lcollins on 11/9/2015.
 */
class SecurityService {
	DbAccess dbAccess
	def SecurityService( DbAccess dbAccess ){
		this.dbAccess = dbAccess
	}

	User createUser(String username, String userGroupName, String email){
		createUser(new User(username: username, userGroupName: userGroupName, emailAddress: email))
	}

	User createUser(User user){
		dbAccess.createUser( user )
	}

	AuthResult authenticate( String username, String password ){

		//TODO implement OAuth 2.0 authentication
		return  testAuthenticate( username, password );
	}

	AuthResult testAuthenticate( String username, String password ){

			if  ( username == "patrick" && password == "adams")
				return new AuthResult(mustChangePassword: false, authToken: "tok.${new Date().time}");

			if  ( username == "change" && password == "password")
				return new AuthResult(mustChangePassword: true, authToken: "tok.${new Date().time}");

		return new AuthResult(mustChangePassword: false);

	}

	UserLogin saveUserLogin(User u, Date when, String ipAddr){
		dbAccess.saveUserLogin(u, when, ipAddr )
	}

	AccessType  getAccess( GraphItem item, User user ){

		/// get the access map of item
		Map accessMap = item.accessMap;

		// check if user is owner
		if ( item.ownerName == user.username ) {
			return accessMap[PermissionType.Owner.name()]
		}

		// check if user in group with access
		dbAccess.getGroupsForUser(user).
		item.groupName

		// check if item has public access

	}

	AccessType getAccess( View view, User user ){

	}

	UserGroup getPrimaryUserGroup(User user) {
		List groups = getGroupsForUser(user)
		groups && groups[0] ? groups[0] : null;
	}

	List<UserGroup> getGroupsForUser(User u){
		dbAccess.getGroupsForUser(u)
	}

	def viewPermissions = [AccessType.View.name(), AccessType.Delete.name(), AccessType.Update.name()]
	def updatePermissions = [AccessType.Delete.name(), AccessType.Update.name()]
	boolean userCanViewItem(User user, GraphItem item){

		//if this is the owner
		if (user.username == item.ownerName)
			return true
		// if visible to all
		if (viewPermissions.contains(item.accessMap[PermissionType.Public.name()])){
			return true;
		}
		//if user is in a group to which this itm is visible
		if (viewPermissions.contains(item.accessMap[PermissionType.Group.name()])){
			if(dbAccess.userInGroup(user,item.groupName))
				return true;
		}
	false
	}

	boolean userCanUpdateItem(User user, GraphItem item){

		//if this is the owner
		if (user.username == item.ownerName)
			return true
		// if visible to all
		if (updatePermissions.contains(item.accessMap[PermissionType.Public.name()])){
			return true;
		}
		//if user is in a group to which this itm is visible
		if (updatePermissions.contains(item.accessMap[PermissionType.Group.name()])){
			if(dbAccess.userInGroup(user,item.groupName))
				return true;
		}
		false
	}
}
