package com.graphomatic.service

import com.graphomatic.Utils
import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.ItemRelationship
import com.graphomatic.domain.Relationship
import groovy.util.logging.Slf4j
import io.github.javaconductor.gserv.GServ
import net.sf.json.groovy.JsonSlurper

/**
 * Created by lcollins on 6/28/2015.
 */
@Slf4j
class RestService {
    def stopFn
    GraphItService graphItService

    def RestService(GraphItService graphItService) {
        this.graphItService = graphItService
    }

    def createService() {

        GServ gServ = new GServ();
        def graphRelationshipRes = gServ.resource("relationship") {
            get(""){
                List<Relationship> relationships = graphItService.getRelationshipDefs();
                writeJson relationships.collect { relationship ->
                    [_links: links(relationship)] + Utils.persistentFields(relationship.properties)
                }
            }
            links{ relationship ->
                [
                        [rel: "self",
                         href: "/relationship/${relationship.id}",
                         method: "GET"]
                ]
            }
        }
        def graphItemRelationshipRes = gServ.resource("item-relationship") {

            get(':id'){ id ->
                ItemRelationship itemRelationship = graphItService.getItemRelationship(id);
                writeJson itemRelationship
            }

            delete(':id'){ id ->
                boolean ok = graphItService.removeItemRelationship(id);
                writeJson (   [ success : ok] )
            }

            /// receives item ids and returns all their relationships
            post(""){ List<String> itemIds ->
                List<ItemRelationship> items = graphItService.getRelationshipsForItems(itemIds)

                writeJson items.collect { itemRelationship ->
                    [_links: links(itemRelationship)] + Utils.persistentFields(itemRelationship.properties)
                }
            }

            links{ itemRelationship ->
                [
                        [rel: "self",
                         href: "/item-relationship/${itemRelationship.id}",
                         method: "GET"],

                        [rel   : "delete",
                         href  : "/item-relationship/${itemRelationship.id}",
                         method: "DELETE"]
                ]
            }
        }
        def graphItemRes = gServ.resource("graph-item") {

            /// get all graph-items
            get("") { ->
                writeJson graphItService.allGraphItems.collect { graphItem ->
                    [links: links(graphItem)] + Utils.persistentFields(graphItem.properties)
                }
            }

            /// get a graphitem
            get(':id') { id ->
                GraphItem g = graphItService.getGraphItem(id)
                writeJson Utils.persistentFields(g.properties) + [links: links(g)]
            }

            /// remove a graphitem
            delete(':id') { id ->
                writeJson { ok : graphItService.removeGraphItem(id) }
            }

            /// Update
            put('') { GraphItem graphItem ->
                GraphItem g = graphItService.updateGraphItem(graphItem)
                writeJson Utils.persistentFields(g.properties) + [links: links(g)]
            }

            /// Update item position
            put('/:id/position/:x:Number/:y:Number') {dummy, graphItemId, x, y ->
                GraphItem g = graphItService.updateGraphItemPosition(graphItemId, x as int, y as int)
                log.debug("graphItem moved to: $x,  $y");
                writeJson Utils.persistentFields(g.properties) + [links: links(g)]
            }

            /// Create
            post('') { GraphItem graphItem ->
                GraphItem g = graphItService.createGraphItem(graphItem)
                writeJson Utils.persistentFields(g.properties) + [links: links(g)]
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
            conversion(GraphItem) { istream ->
                def json = new JsonSlurper().parse(istream);
                new GraphItem(position: json.position, title: json.title, categories: json.categories);
            }
            conversion(List.class) { InputStream istream ->
                istream.getText().split(',') as List<String>
            }
            resource graphItemRes
            resource graphItemRelationshipRes
            resource graphRelationshipRes
        }
    }

    def start() {
        stopFn = createService().start(8888)
    }

    def stop() {
        stopFn?.call()
    }

}
