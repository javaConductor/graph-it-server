import com.gmongo.GMongo
import com.gmongo.GMongoClient
import com.graphomatic.Utils
import com.graphomatic.service.DbAccess
import com.graphomatic.service.GraphItService
import com.graphomatic.service.RestService
import com.graphomatic.typesystem.TypeSystem
import com.mongodb.Mongo
import com.mongodb.MongoClient
import com.mongodb.ServerAddress
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.gridfs.GridFsTemplate

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
    mongo.repositories('base-package': "com.graphomatic.domain")
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

    mongo.'mapping-converter'(
            'id': "converter",
            'db-factory-ref': ('mongoDbFactory') );

    mongoTemplate(MongoTemplate){ beanDefinition ->
        beanDefinition.constructorArgs = [
            ref('mongoDbFactory'),
            ref('converter')
        ]
    }

    gridFsTemplate(GridFsTemplate){ beanDefinition ->
        beanDefinition.constructorArgs = [
            ref(mongoDbFactory),
            ref(converter)
        ]
    }

    dbAccess(DbAccess){beanDefinition ->
        beanDefinition.constructorArgs = [
            ref(mongoTemplate),
            ref('gridFsTemplate')
        ]
    }

    typeSystem(TypeSystem){beanDefinition ->
            beanDefinition.constructorArgs = [
                    ref('dbAccess')
            ]
    }

    graphItService(GraphItService){beanDefinition ->
        beanDefinition.constructorArgs = [
            ref('dbAccess'),
            ref('typeSystem')
        ]
    }

    ////////////////////////////////////////////////////////////////
    //// Web Components
    ////////////////////////////////////////////////////////////////
    restService(RestService) { beanDefinition ->
        beanDefinition.constructorArgs = [
            ref('graphItService'), ref('typeSystem')
        ]
    }
}
