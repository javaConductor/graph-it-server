package com.graphomatic.persistence

import com.graphomatic.domain.GraphItem
import com.graphomatic.security.AccessType
import com.graphomatic.security.User
import com.graphomatic.security.UserGroup
import com.graphomatic.security.UserLogin
import com.mongodb.DBCursor
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsTemplate

/**
 * Created by lee on 9/25/16.
 */
trait UserDbAccess {
    MongoTemplate mongo;
    GridFsTemplate gridFsTemplate

    UserLogin saveUserLogin(User user, Date date, String ipAddr) {
        UserLogin ul = new UserLogin(ipAddr: ipAddr, user: user, when: date );
        mongo.insert( ul )
        ul
    }

    List<UserGroup> getUserGroups(){
        mongo.findAll(UserGroup)
    }

    UserGroup getUserGroupByName(String groupName){
        mongo.findOne(Query.query(Criteria.where("name").is(groupName)), UserGroup)
    }

    UserGroup addUserToGroup(String  username, String groupName){
        UserGroup userGroup = getUserGroupByName(groupName)
        if(!userGroup)
            throw new IllegalArgumentException("No such user group: $groupName")
        User user = getUserByName(username)
        if(!user)
            throw new IllegalArgumentException("No such user: $username")
        userGroup.users.add(user)
        mongo.save(userGroup);
        userGroup
    }

    User getUserByName(String username) {
        mongo.findOne(Query.query(Criteria.where("username").is(username)), User)
    }

    List<UserGroup> getGroupsForUser(User u){
        List<UserGroup> groups = []
        DBCursor c=mongo.getCollection("UserGroup").find(['users': ["$elemMatch": [_id: u._id]]])
        while (c.hasNext()){
            groups << (c.curr() as UserGroup)
        }
        groups
    }

    User createUser(User user) {
        mongo.save(user)
        user
    }
}
