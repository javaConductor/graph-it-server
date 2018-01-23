import com.graphomatic.security.UserResource
import com.graphomatic.security.SecurityService
import com.graphomatic.persistence.DbAccess
import com.graphomatic.service.EventService
import com.graphomatic.service.GraphItService
import com.graphomatic.service.RestService
import com.graphomatic.typesystem.TypeSystem
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.gridfs.GridFsTemplate

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
/**
 * Created by javaConductor on Jun 11.
 */

Properties properties = new Properties();
File propFile = new File("/opt/graphOmatic/graph-it-server/graph-it-server.properties")
properties.load(new FileInputStream(propFile))
//Map mongoOpts = Utils.subMap(properties, 'mongo.options')

beans {
    xmlns([ctx  : 'http://www.springframework.org/schema/context',
           mongo: 'http://www.springframework.org/schema/data/mongo'])
    ctx.'component-scan'('base-package': "com.graphomatic")
    mongo.repositories('base-package': "com.graphomatic.domain")
    ////////////////////////////////////////////////////////////////
    ///  Connector Objects for DB
    ////////////////////////////////////////////////////////////////

    mongo.mongo(
            'host': properties["mongo.host"],
            'port': properties["mongo.port"] as int,
            'id': "mongoDb");


    mongo.'db-factory'(
            'dbname': properties["mongo.databaseName"],
            'mongo-ref': "mongoDb",
            'id': "mongoDbFactory");

    mongo.'mapping-converter'(
            'id': "converter",
            'db-factory-ref': ('mongoDbFactory'));

    mongoTemplate(MongoTemplate) { beanDefinition ->
        beanDefinition.constructorArgs = [
                ref('mongoDbFactory'),
                ref('converter')
        ]
    }

    gridFsTemplate(GridFsTemplate) { beanDefinition ->
        beanDefinition.constructorArgs = [
                ref(mongoDbFactory),
                ref(converter)
        ]
    }

    dbAccess(DbAccess) { beanDefinition ->
        beanDefinition.constructorArgs = [
                ref(mongoTemplate),
                ref('gridFsTemplate')
        ]
    }

    typeSystem(TypeSystem) { beanDefinition ->
        beanDefinition.constructorArgs = [
                ref('dbAccess')
        ]
    }


    graphDbFactory(GraphDatabaseFactory) {

    }

    graphDbService(GraphDatabaseService){
        new GraphDatabaseFactory().newEmbeddedDatabase("/opt/neo4j-store")
    }


    graphDbAccess(GraphDbAccess) { beanDefinition ->
        beanDefinition.constructorArgs = [
            ref('graphDbService')
        ]

    }
    ////////////////////////////////////////////////////////////////
    //// Security Components
    ////////////////////////////////////////////////////////////////

    securityService(SecurityService) { beanDefinition ->
        beanDefinition.constructorArgs = [
                ref('dbAccess')
        ]
    }

    securityResource(UserResource) { beanDefinition ->
        beanDefinition.constructorArgs = [
                ref('securityService')
        ]
    }

    ////////////////////////////////////////////////////////////////
    //// Services
    ////////////////////////////////////////////////////////////////
    eventService(EventService) {beanDefinition ->
        beanDefinition.constructorArgs = [
                ref('mongoTemplate')
        ]
    }
    graphItService(GraphItService) { beanDefinition ->
        beanDefinition.constructorArgs = [
                ref('dbAccess'),
                ref('typeSystem'),
                ref('securityService'),
                ref('eventService')
        ]
    }

    ////////////////////////////////////////////////////////////////
    //// Web Components
    ////////////////////////////////////////////////////////////////
    restService(RestService) { beanDefinition ->
        beanDefinition.constructorArgs = [
                ref('dbAccess'),
                ref('graphItService'),
                ref('typeSystem'),
                ref('securityResource')
        ]
    }
}
