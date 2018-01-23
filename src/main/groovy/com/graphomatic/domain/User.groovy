package com.graphomatic.domain

import groovy.transform.ToString
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 7/5/2015.
 */
@ToString
@Document
class User {
    @Id
    String id
    @Indexed(unique = true)
    String username
    @Indexed(unique = true)
    String email
    String pswd // md5 of pswd
}
