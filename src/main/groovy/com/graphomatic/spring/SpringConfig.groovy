package com.graphomatic.spring

import com.graphomatic.persistence.GraphDbAccess
import com.mongodb.Mongo
import groovy.util.logging.Commons
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.gridfs.GridFsTemplate

/**
 * Created by lee on 1/6/18.
 */
@Commons
@Configuration
class SpringConfig {

    Properties properties = new Properties()

    SpringConfig(){
        File propFile = new File("/opt/graphOmatic/graph-it-server/graph-it-server.properties")
        properties.load(new FileInputStream(propFile))

    }
    @Autowired
    ApplicationContext context;

    @Bean(name="graphDb")
    GraphDatabaseService neo4jGraphDatabaseService(){
        GraphDatabaseFactory graphDbFactory = new GraphDatabaseFactory();
        GraphDatabaseService graphDb = graphDbFactory.newEmbeddedDatabase(
                new File("/opt/neo4j-store"))
    graphDb
    }

    @Bean(name="neo4jDriver")
    Driver neo4jDriver(){
        log.debug("uri:${properties["neo4j.uri"]}, user:${properties["neo4j.user"]}, password: ${properties["neo4j.password"]}")
        GraphDatabase.driver(
                properties["neo4j.uri"],
                AuthTokens.basic(
                        properties["neo4j.user"],
                        properties["neo4j.password"]
                ) )
    }

    @Autowired
    MongoDbFactory factory

    @Autowired
    MongoConverter converter

    @Bean(name="mongoConverter")
    MongoConverter converter(){
        MongoMappingContext context = new MongoMappingContext();
        MongoConverter converter = new MappingMongoConverter(factory, context)
        converter
    }

    @Bean(name="mongo")
    Mongo mongo(){
        Mongo mongo = new Mongo( properties["mongo.host"], properties["mongo.port"] as int)
        mongo
    }

    @Bean(name="mongoDbFactory")
    MongoDbFactory factory(){
        Mongo mongo = context.getBean("mongo")
        MongoDbFactory factory = new SimpleMongoDbFactory(mongo, properties["mongo.databaseName"])
        factory
    }

    @Bean("template")
    MongoTemplate template(){
        Mongo mongo = new Mongo( properties["mongo.host"], properties["mongo.port"] as int)
        MongoDbFactory factory = context.getBean("mongoDbFactory")
        new MongoTemplate( context.getBean("mongoDbFactory") )
    }

    @Bean
    GridFsTemplate gridFsTemplate(){
        def factory = context.getBean("mongoDbFactory")
        new GridFsTemplate(factory, converter)
    }

    @Bean
    GraphDbAccess getGraphDbAccess(){
        new GraphDbAccess(context.getBean("template"),
                context.getBean("gridFsTemplate"),
                context.getBean("neo4jDriver"))
    }

}
