package com.graphomatic.service

import com.graphomatic.Utils
import com.graphomatic.domain.GraphItem
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
                writeJson Utils.persistentFields(g) + [links: links(g)]
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
                new JsonSlurper().parse(istream).properties as GraphItem
            }
            resource graphItemRes
        }
    }

    def start() {
        stopFn = createService().start(8888)
    }

    def stop() {
        stopFn?.call()
    }

}
