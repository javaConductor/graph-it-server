package com.graphomatic.service

import com.graphomatic.Utils
import com.graphomatic.domain.Category
import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.ImageData
import com.graphomatic.domain.ItemRelationship
import com.graphomatic.domain.Position
import com.graphomatic.domain.Relationship
import com.graphomatic.oauth.OAuthResource
import com.graphomatic.persistence.DbAccess
import com.graphomatic.security.User
import com.graphomatic.security.UserResource
import com.graphomatic.typesystem.TypeSystem
import com.graphomatic.typesystem.domain.ItemType
import groovy.util.logging.Slf4j
import io.github.javaconductor.gserv.GServ
import io.github.javaconductor.gserv.converters.FormData
import io.github.javaconductor.gserv.requesthandler.RequestContext
import net.sf.json.groovy.JsonSlurper
import org.apache.http.auth.BasicUserPrincipal

import java.security.Principal

/**
 * Created by lcollins on 6/28/2015.
 */
@Slf4j
class RestService {
    def stopFn
    GraphItService graphItService
    TypeSystem typeSystem
	UserResource securityResource
    DbAccess dbAccess
    def RestService(DbAccess dbAccess, GraphItService graphItService,
                    TypeSystem typeSystem,
                    UserResource securityResource) {
        this.graphItService = graphItService
        this.typeSystem = typeSystem
		this.securityResource = securityResource
        this.dbAccess = dbAccess
    }

    def createService() {

        GServ gServ = new GServ();
        def oauthRes = new OAuthResource()
        def graphCategoryRes = gServ.resource("category") {
            get("") {
                List<Category> categories = graphItService.getCategories();
                writeJson categories.collect { category ->
                    [_links: links(category)] + Utils.persistentFields(category.properties)
                }
            }

            get(":id") {
                Category category = graphItService.getCategory(id);
                writeJson(Utils.persistentFields(category.properties) + [_links: links(category)])
            }

            links { category ->
                [
                        [rel : "self",
                         href: "/category/${category.id}",
                         method: "GET"]
                ]
            }
        }

        def graphRelationshipRes = gServ.resource("relationship") {
            get("") {
                List<Relationship> relationships = graphItService.getRelationshipDefs();
                writeJson relationships.collect { relationship ->
                    [_links: links(relationship)] + Utils.persistentFields(relationship.properties)
                }
            }
            links { relationship ->
                [
                        [rel : "self",
                         href: "/relationship/${relationship.id}",
                         method: "GET"]
                ]
            }
        }
        def graphItemRelationshipRes = gServ.resource("item-relationship") {

            get('') { ->
                List<ItemRelationship> itemRelationships = graphItService.getAllItemRelationships();
                writeJson itemRelationships.collect { itemRelationship ->
                    [_links: links(itemRelationship)] + Utils.persistentFields(itemRelationship.properties)
                }
            }

            get(':id') { id ->
                ItemRelationship itemRelationship = graphItService.getItemRelationship(id);
                writeJson itemRelationship
            }

            delete(':id') { id ->
                User u = getCurrentUser(requestContext)
                boolean ok = graphItService.removeItemRelationship(u, id)
                writeJson([success: ok])
            }

            /// receives item ids and returns all their relationships
            post("for-items") { List<String> itemIds ->
                List<ItemRelationship> items = graphItService.getRelationshipsForItems(itemIds)
                writeJson items.collect { itemRelationship ->
                    [_links: links(itemRelationship)] + Utils.persistentFields(itemRelationship.properties)
                }
            }

            /// creates item relationship
            post("") { ItemRelationship rel ->
                User u = getCurrentUser(requestContext)
                ItemRelationship itemRelationship = graphItService.createItemRelationship(u, rel);
                writeJson Utils.persistentFields(itemRelationship.properties) + [_links: links(itemRelationship)]
            }

            links { itemRelationship ->
                [
                        [rel : "self",
                         href: "/item-relationship/${itemRelationship.id}",
                         method: "GET"],

                        [rel   : "delete",
                         href  : "/item-relationship/${itemRelationship.id}",
                         method: "DELETE"]
                ]
            }
        }
        def graphItemImageRes = gServ.resource("graph-item-images") {

            /// get graph-items main image
            get("/:graphItemId/:imageId") { graphItemId, imageId ->
                ImageData imageData = graphItService.getItemData("/graph-item-images/$graphItemId/$imageId" )
                if (!imageData){
                    error  404, "No such image."
                }else {
                    contentType "${imageData.contentType}"
                    writeFrom imageData.inputStream
                }
            }

            /// post
            post(":graphItemId/order/main/:imageId") {is, graphItemId, imageId ->
                User u = getCurrentUser(requestContext)
                GraphItem graphItem = graphItService.setAsMainImage(u, graphItemId, imageId)
                if(graphItem) {
                    writeJson asMap(graphItem)
                }else{
                    error 404, "No such item [$graphItemId] or image [$imageId]"
                }
            }

            /// create a new image for this graph-item
            post(":graphItemId") {FormData formData, String graphItemId ->
                User u = getCurrentUser(requestContext)
                if (!formData.hasFiles()) {
                    error 400, "No file uploaded!"
                }else {
                    ByteArrayInputStream is = new ByteArrayInputStream(formData.files[0].content)
                    ImageData imageData = graphItService.createItemImage(u, graphItemId, is, formData.files[0].contentType, index)
                    contentType "${imageData.contentType}"
                    location "/graph-item-images/$graphItemId/${imageData.id}"
                    writeFrom imageData.inputStream
                }
            }
        }

    def prepareGraphItem = {GraphItem  graphItem ->
        Map m = Utils.persistentFields(graphItem.properties)
        m
    }

    def prepareItemType = {ItemType  itemType ->

        Map m = Utils.persistentFields(graphItem.properties)
        m
    }

    def typeSystemRes = gServ.resource("types") {

        /// get all types
        get("") { ->
            writeJson typeSystem.getAllTypes().collect { itemType ->
                [links: links(itemType)] + (asMap(itemType))
            }
        }

        /// get a type
        get(':id') { id ->
            ItemType itemType = typeSystem.getType(id)
            if(!itemType)
                error(404, "No such type: $id")
            else
                writeJson asMap(itemType) + [links: links(itemType)]
        }

        /// get a type
        get('byName/:typeName') { typeName ->
            ItemType itemType = typeSystem.getTypeByName(typeName)
            if(!itemType)
                error(404, "No such type: $typeName")
            else
            writeJson asMap(itemType) + [links: links(itemType)]
        }
        links { ItemType itemType ->
            [
                    [rel: "self", href: "/types/${itemType.id}", method: "GET"]
            ]

        }
    }

    def graphItemRes = gServ.resource("graph-item") {

        /// get all graph-items
        get("") { ->
            writeJson graphItService.getGraphItemsForUser( 0, 0, u ).collect { graphItem ->
                [links: links(graphItem)] + prepareGraphItem(graphItem)
            }
        }

        /// get all graph-items visible to a user
        get(":") { ->
            User u = getCurrentUser(requestContext)
            writeJson graphItService.getGraphItemsForUser( 0, 0, u ).collect { graphItem ->
                [links: links(graphItem)] + prepareGraphItem(graphItem)
            }
        }

        /// get a graphitem
        get(':id') { id ->
            GraphItem g = graphItService.getGraphItem(id)
            writeJson prepareGraphItem(g) + [links: links(g)]
        }

        /// remove a graphitem
        delete(':id') { id ->
            User u = getCurrentUser(requestContext)
            writeJson ([ ok : graphItService.removeGraphItem(u, id ) ])
        }

        /// Update
        put('') { GraphItem graphItem ->
            User u = getCurrentUser(requestContext)
            GraphItem g = graphItService.updateGraphItem(u,graphItem)
            writeJson prepareGraphItem(g) + [links: links(g)]
        }

        /// Update item position
        put('/:id/position/:x:Number/:y:Number') {dummy, graphItemId, x, y ->
            User u = getCurrentUser(requestContext)
            GraphItem g = graphItService.updateGraphItemPosition(u, graphItemId, x as int, y as int)
            log.debug("graphItem moved to: $x,  $y");
            writeJson prepareGraphItem(g) + [links: links(g)]
        }

        /// Add item note
        put('/:id/notes') {String notes, graphItemId ->
            User currentUser = getCurrentUser(requestContext)
            GraphItem gOld = graphItService.getGraphItem(graphItemId);
            GraphItem g = graphItService.updateGraphItemNotes(currentUser, graphItemId, notes ?: '')
            log.debug("graphItem [$graphItemId] set notes: from [${gOld.notes}] to [${g.notes}].");
            writeJson prepareGraphItem(g) + [links: links(g)]
        }

        /// Create from form
        post('/form') { FormData formData ->
            User currentUser = getCurrentUser(requestContext)
            String title = formData.fieldValue["title"]
            String cat = formData.fieldValue["category"]
            String jsonDataString = formData.fieldValue["data"]
            Map jsonData = new groovy.json.JsonSlurper().parseText(jsonDataString)
            String typeName
            def typeId = formData.fieldValue["type"]
            def notes = formData.fieldValue["notes"] ?: ""
            def type = typeSystem.getType(typeId)
            typeName = type?.name ?: TypeSystem.BASE_TYPE_NAME
            Position pos =  (
                    formData.fieldValue["position.x"] && formData.fieldValue["position.y"]
                        ) ?  new Position(
                                x: formData.getValue("position.x") as long,
                                y: formData.getValue("position.y") as long)
                          : new Position(x:100L, y:200L);

            Category category = graphItService.getCategory(cat)
            GraphItem g = graphItService.createGraphItem(currentUser, new GraphItem(title: title,
                    typeName: typeName,
                    images: [],
                    data: jsonData,
                    notes: notes,
                    position : pos,
                    categories: category ? [category] : []))
            log.debug("$this : $g")
            if(formData.files) {
                ByteArrayInputStream is = new ByteArrayInputStream(formData.files[0].content)
                ImageData imageData = graphItService.createItemImage(g.id, is, formData.files[0].contentType)
                g= graphItService.getGraphItem(g.id)
            }
            writeJson prepareGraphItem(g) + [_links: links(g)]
        }

        /// Create
            post('') { GraphItem graphItem ->
                User currentUser = getCurrentUser(requestContext)
                GraphItem g = graphItService.createGraphItem(currentUser, graphItem)
            writeJson prepareGraphItem(g) + [_links: links(g)]

        }

        links { GraphItem graphItem ->
            [
                    [rel: "self", href: "/graph-item/${graphItem.id}", method: "GET"],
                    [rel: "update", href: "/graph-item/", method: "PUT"],
                    [rel   : "updatePosition",
                     href  : "/graph-item/" +
                             "${graphItem.id}/position/" +
                             ":x/" +
                             ":y",
                     method: "PUT"],
                    [rel   : "delete",
                     href  : "/graph-item/${graphItem.id}",
                     method: "DELETE"]
            ]

        }
    }

        gServ.plugins{
            plugin("cors",[:])
        }.http {
            cors( "/", allowAll(3600) )

            basicAuthentication(['DELETE', "POST", "PUT"], '/*', "graph-it-users") { user, pswd, RequestContext requestContext ->
                def ok = (user.equals("system") && pswd.equals("system"))
                println "u=$user p=$pswd : ${ok ? "OK" : "FAILED"}";
                if(ok){
                    requestContext.setPrincipal(new BasicUserPrincipal(user));
                }
                ok
            }
            conversion(GraphItem) { istream ->
                def json = new JsonSlurper().parse(istream);
                Position p = new Position(x: json.position.x, y: json.position.y);

                List<Category> categories = json.categories.collect{ category ->
                    new Category(id: category.id, name: category.name);
                }
                new GraphItem(id: json.id,
                        position: p,
                        title: json.title,
                        typeName: json.typeName,
                        notes: json.notes ?: "",
                        categories: categories ?: [],
                        data: json.data ?: [:]);
            }
            conversion(ItemRelationship) { istream ->
                def json = new JsonSlurper().parse(istream);
                new ItemRelationship(
                    sourceItemId: json.sourceItemId,
                    relatedItemId: json.relatedItemId,
                    relationship: graphItService.getRelationshipDef(json.relationshipId))
            }
            conversion(List.class) { InputStream istream ->
                istream.getText().split(',') as List<String>
            }
            resource graphItemRes
            resource graphItemRelationshipRes
            resource graphRelationshipRes
            resource graphCategoryRes
            resource graphItemImageRes
            resource typeSystemRes
            resource securityResource
        }
    }

    User getCurrentUser(RequestContext requestContext){
        dbAccess.getUserByName(requestContext.principal.name)
    }
    def start() {
        stopFn = createService().start(8888)
    }

    def stop() {
        stopFn?.call()
    }

}
