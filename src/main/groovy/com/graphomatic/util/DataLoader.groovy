package com.graphomatic.util

import com.graphomatic.domain.Category
import com.graphomatic.typesystem.MapInfo
import com.graphomatic.typesystem.TypeSystem
import com.graphomatic.typesystem.domain.ItemType
import com.graphomatic.domain.Relationship
import com.graphomatic.typesystem.validation.DataElementDefValidator
import com.graphomatic.typesystem.validation.ItemTypeValidator
import com.graphomatic.typesystem.validation.ValidationException
import com.graphomatic.service.GraphItService
import com.graphomatic.typesystem.PrimitiveTypes
import com.graphomatic.typesystem.domain.PropertyDef
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j

/**
 * Created by lcollins on 8/11/2015.
 */
@Log4j
class DataLoader {
    GraphItService graphItService
    TypeSystem typeSystem
    ItemTypeValidator itemTypeValidator = new ItemTypeValidator()
    DataElementDefValidator dataElementDefValidator = new DataElementDefValidator()

    DataLoader(GraphItService graphItService, TypeSystem typeSystem) {
        this.graphItService = graphItService
        this.typeSystem = typeSystem
    }

    def loadData(InputStream is) {

        def data = new JsonSlurper().parse(is)
//        Map<String, Category> categories
        Map<String, Relationship> relationshipDefs
        Map<String, ItemType> types

        if (data.Categories)
            loadCategories(data.Categories);
        if (data.Relationships)
            relationshipDefs = loadRelationships(data.Relationships)
        if (data.Types)
            types = loadItemTypes(data.Types)
        println( new JsonBuilder(types).toPrettyString() )
    }

/*
    * categories: [{
    *   name: "ALL",
    *   description: "Catch-all category - uncategorized items/relationships go here"
    *   },{
    *   name: "Family",
    *   parent: "ALL",
    *   description: "We are family!!"
    * }]
    * */
    /*
    {
  "Categories": [
    {
      "name": "People",
      "children": {
        "Family": {},
        "Peers": {},
        "Friends": {}
      }
    },
    {
      "name": "Music"
    },
    {
      "name": "Location"
    },
    {
      "name": "Business"
    },
    {
      "name": "Software",
      "children": {
          "Software Design":{
          },
          "Software Deployment":{
          },
          "Software Application":{
            "children": {
                "Embedded App":{},
                "Desktop App":{},
                "Web App": {},
                "Web Service": {},
                "Phone/Tablet App":{}

            }
          }
      }
    }
  ],

     */

    /**
     * Load the categories from data set
     *
     * @param jsonCategoryList
     * @return
     */
    Map<String, Category> loadCategories(Map jsonCategoryMap) {
        return jsonCategoryMap.collectEntries {catName, catDef ->
            [(catName): categoryFromJson(catDef + [name : catName])]
        }
    }

    private Category categoryFromJson(jsonCategory, Category parentCat=null) {

        if (!jsonCategory.name)
            throw new ValidationException("Categories must have names.");
        Category cat = new Category(
                name: jsonCategory.name,
                description: jsonCategory.description,
                parent: parentCat
        )
        cat = graphItService.createCategories([cat])[0]
        //// now do the children

        List childCategories = jsonCategory.children ?
                jsonCategory.children.keySet().collect {childName ->
                    def child = jsonCategory.children[ childName ]
                    categoryFromJson(child + [name: childName], cat)
                } : []

        cat.children = childCategories
        //graphItService.updateCategory(cat)
        cat
    }

    private List<Category> categoryListFromJson(jsonCategoryList) {
        jsonCategoryList.collect { categoryName ->
            def nuCat = graphItService.getCategoryByName(categoryName)
            if (!nuCat) {
                throw new ValidationException("No such Category: ${categoryName}");
            }
            nuCat
        }
    }

/*
    "relationshipTypes": [{
	    "category":"Literature",
		"name":"writerOf",
		"constraintFrom": "[item.isInTypes([Person,Writer])]",
		"constraintTo": "item.isInTypes(['LiteraryWork'])"
    }]
 */
/**
 *
 * @param jsonRelationship
 * @return
 */
    private Relationship relationshipFromJson(jsonRelationship) {
        String parentName
        if (!jsonRelationship.name)
            throw new ValidationException("Relationship must have name.");

        new Relationship(name: jsonRelationship.name, parentName: jsonRelationship.parent )
    }
    /**
     * Load the relationshipTypes from data set
     *
     * @param jsonRelationships
     * @return
     */
    Map<String, Relationship> loadRelationships(Map jsonRelationships) {
        jsonRelationships.collectEntries { relationshipName, rel ->
            def newrel = relationshipFromJson(rel + [name : relationshipName])
            [relationshipName : graphItService.createRelationship(newrel)]
        }
    }

/*	"properties":[
		{"name": "name", "type": "text", "required": true},
		{"name": "photos", "relationshipType":"photoOf", itemType: "Image", "required": true},
		{"name": "producer", "relationshipType":"produced", itemType: ["Person","Artist","Producer"]},
		{"name": "audioClip", "relationshipType":"audioClip", itemType: "Audio" },
		{"name": "audioFile", "relationshipType":"audioFile", itemType: "Audio" },
		{"name": "credits", "relationshipType": "credits", "itemType":"TrackCredits"}
	]*/

    private PropertyDef dataElementDefFromJson(propertyName, jsonPropertyDef) {
        String typeName
        Relationship relationship

        if (!propertyName)
            throw new IllegalArgumentException('Name is empty or missing.')

        typeName = jsonPropertyDef.type
        if (!typeSystem.isKnownType(typeName)) {
            log.warn("No such type: $typeName for data element: $propertyName")
        }
            // convert string to proper data type
//            if (PrimitiveTypes.isPrimitiveType(typeName)) {
//                typeSystem.fixType(typeName, defaultValue)
//            }

            // when an item property refers to another item then
            // the relationship between those items will be 'jsonPropertyDef.relationship'
            if (jsonPropertyDef.relationship) {// element relationship constraint
                relationship = graphItService.getRelationshipDefByName(jsonPropertyDef.relationship)
            }

            if (typeName == (PrimitiveTypes.Map)) {
                throw new IllegalArgumentException('data type: "map" is not yet supported')
            }

            /// throw exception if not valid
            dataElementDefValidator.validate(
                    new PropertyDef(
                            name: propertyName,
                            typeName: typeName,
                            relationship: relationship,
                            required: jsonPropertyDef.required
                    )
            )
        }

    /**
     *
     * @param jsonItemType
     * @return
     */
    private ItemType itemTypeFromJson(typeName, jsonItemType) {

        if (!typeName)
            throw new IllegalArgumentException("Types must have names.")

        /// PARENT
        def parentType
        if (jsonItemType.parent) {
            // get the data defs from the hierarchy
            parentType = graphItService.getItemTypeByName(jsonItemType.parent)
        }

        /// CATEGORY
        List<Category> categories = []
        if (jsonItemType.category) {
            categories = categoryListFromJson(jsonItemType.category)
        }

        /// DATA
        Map<String, PropertyDef> dataDefs =
                jsonItemType.properties.collectEntries { String propertyName, jsonDataElementDef ->
            [(propertyName): dataElementDefFromJson(propertyName, jsonDataElementDef)]
        }
        /// throw exception if not valid
        itemTypeValidator.validate(
                new ItemType(
                        categories: categories,
                        propertyDefs: dataDefs,
                        parentName: jsonItemType.parent,
                        name: typeName
                )
        )
    }

    Map<String,ItemType> loadItemTypes(Map jsonItemTypes) {
        println "loadItemTypes: "+new JsonBuilder(jsonItemTypes).toPrettyString()

        jsonItemTypes.collectEntries { typeName, jsonItem ->
            println "loading Type $typeName"
            def newtype = itemTypeFromJson(typeName, jsonItem)
           // println "Before "+new JsonBuilder(newtype).toPrettyString()
            newtype = graphItService.createItemType( newtype, false)
            println "After "+new JsonBuilder(newtype).toPrettyString()
            [ (typeName) : ( newtype ) ]
        }
    }
}
