package com.graphomatic.security

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lee on 9/25/16.
 */
@Document
@CompoundIndex(name = "usernameIdx", def = "{'username': 1 }", unique= true, dropDups = true)
class Secret {
    @Id
    String id
    String username
    String passwordHash
}
