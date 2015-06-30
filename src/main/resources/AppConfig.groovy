import com.gmongo.GMongo
import com.gmongo.GMongoClient
import com.graphomatic.Utils
import com.graphomatic.service.DbAccess
import com.graphomatic.service.GraphItService
import com.graphomatic.service.RestService
import com.mongodb.Mongo
import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoDbFactory

/**
 * Created by javaConductor on Jun 11.
 */

Properties properties = new Properties();
File propFile = new File("/opt/graphOmatic/graph-it-server/graph-it-server.properties")
properties.load(new FileInputStream(propFile))
//Map mongoOpts = Utils.subMap(properties, 'mongo.options')

beans {
    xmlns([ctx: 'http://www.springframework.org/schema/context',
           mongo: 'http://www.springframework.org/schema/data/mongo'])
    ctx.'component-scan'('base-package': "com.graphomatic")
    mongo.repositories('base-package': "com.graphomatic.domnain")
    ////////////////////////////////////////////////////////////////
    ///  Connector Objects for DB
    ////////////////////////////////////////////////////////////////

    mongo.mongo(
            'host':properties["mongo.host"],
            'port':properties["mongo.port"] as int,
            'id':"mongoDb");
    mongo.'db-factory'(
            'dbname':properties["mongo.databaseName"],
            'mongo-ref':"mongoDb",
            'id':"mongoDbFactory");


    mongoTemplate(MongoTemplate){ beanDefinition ->
        beanDefinition.constructorArgs = [
            ref('mongoDbFactory')
        ]
    }

    dbAccess(DbAccess){beanDefinition ->
        beanDefinition.constructorArgs = [
            ref('mongoTemplate')
        ]
    }

    graphItService(GraphItService){beanDefinition ->
        beanDefinition.constructorArgs = [
            ref('dbAccess')
        ]
    }

    ////////////////////////////////////////////////////////////////
    //// Web Components
    ////////////////////////////////////////////////////////////////
    restService(RestService) { beanDefinition ->
        beanDefinition.constructorArgs = [
            ref('graphItService')
        ]
    }
}
