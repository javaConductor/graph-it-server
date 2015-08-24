import com.graphomatic.typesystem.PrimitiveTypes
import spock.lang.Specification

/**
 * Created by lcollins on 8/24/2015.
 */
class PrimitiveTypeSpec extends  Specification {

    def "should parse 1"(){
        when:
        def number = PrimitiveTypes.fromString("text", "1")

        then:
        number == '1'
    }

 def "should parse 1 as num"(){
        when:
        def number = PrimitiveTypes.fromString("number", "1")

        then:
        number == 1
    }

 def "should parse 1.5 as num"(){
        when:
        def number = PrimitiveTypes.fromString("number", "1.5")

        then:
        number == 1.5
    }

 def "should parse 'true'  as true"(){
        when:
        def b = PrimitiveTypes.fromString("boolean", "true")

        then:
        b
    }

 def "should parse 'false'  as false"(){
        when:
        def b = PrimitiveTypes.fromString("boolean", "false")

        then:
        !b
    }

    def "should parse null as null"(){
        when:
        def number = PrimitiveTypes.fromString("text", null)

        then:
        number == null
    }

    def "should parse email"(){
        when:
        def number = PrimitiveTypes.fromString("emailAddress", "lee@me.us")

        then:
        number == "lee@me.us"
    }

    def "should parse bad email to null"(){
        when:
        def number = PrimitiveTypes.fromString("emailAddress", "lee/me.us")

        then:
        number == null
    }

}
