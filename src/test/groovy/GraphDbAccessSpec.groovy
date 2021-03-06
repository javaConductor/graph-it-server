import com.graphomatic.domain.Category
import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.GraphItemStatus
import com.graphomatic.persistence.GraphDbAccess
import com.graphomatic.spring.SpringConfig
import com.graphomatic.typesystem.PrimitiveTypes
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import org.springframework.test.context.ContextConfiguration

/**
 * Created by lcollins on 8/24/2015.
 */
@ContextConfiguration(classes = SpringConfig)
class GraphDbAccessSpec extends  Specification {

    @Autowired
    GraphDbAccess dbAccess

    def "should be injected for test"(){
        when:
        def a=dbAccess

        then:
        a != null
    }

    def "should create category"(){
        when:
        Category cat = new Category(name: "Liquid", description: "liquid stuff", id: "numberOne")
        def a=dbAccess.createCategory(cat)

        then:
        a != null
        def saved = dbAccess.getCategory(a.id)
        saved != null
        def wasRemoved = dbAccess.removeCategory(a.id)
        wasRemoved == true

    }
    def "should create graphItem"(){
        when:
        GraphItem item = new GraphItem(
                id: "numberOne", images: [], title: "The New One",
                status: GraphItemStatus.New.toString(),
                ownerName: "Lee", groupName: "Lee",
                notes: "Notes ...", accessMap: [:],
                data: [name: "lee collins", age: 50], typeName: "Person")
        def a=dbAccess.createGraphItem(item)

        then:
        a != null

    }

}
