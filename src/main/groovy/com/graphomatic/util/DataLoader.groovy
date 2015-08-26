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
import groovy.json.JsonSlurper

/**
 * Created by lcollins on 8/11/2015.
 */
/*
types: [{
	name:"Business",
	category: "Business",
	requiredData:[
		{"name": "name", "type": "text"}

	],
	optionalData:[
		{"name": "mainAddress", "relationshipType": "atAddress", "itemType":"StreetAddress"},
		{"name": "branches", "relationshipType": "atAddress", "itemType":"BusinessLocation"},
		{"name": "email", "relationshipType": "emailAddress", "itemType":"EmailAddress" },
		{"name": "postalAddress", "relationshipType": "mailingAddress", "itemType":"StreetAddress" }
	]
},
{
	"name":"StreetAddress",
	"category": "Locations",
	"requiredData":[
		{"name": "address", "type": "text"},
		{"name": "city", "type": "text"},
		{"name": "state", "type": "text"},
		{"name": "country", "type": "text", "defaultValue": "US" }
	],
	"optionalData":[
		{"name": "address2", "type": "text"},
		{"name": "postalCode", "type": "text"}
	]
},
{
	"name":"Track",
	"category": "Audio",
	"requiredData":[
		{"name": "title", "type": "text"},
		{"name": "artist", "relationshipType":"performed", itemType: ["Person","Artist"]},
	],
	"optionalData":[
		{"name": "producer", "relationshipType":"produced", itemType: ["Person","Artist","Producer"]},
		{"name": "audioClip", "relationshipType":"audioClip", itemType: "Audio" },
		{"name": "audioFile", "relationshipType":"audioFile", itemType: "Audio" },
		{"name": "credits", "relationshipType": "credits", "itemType":"TrackCredits"}
	]
},
{
	"name":"Artist",
	"category": "Audio",
	"requiredData":[
		{"name": "name", "type": "text"},
		{"name": "photos", "relationshipType":"photoOf", itemType: "Image"},
	],
	"optionalData":[
		{"name": "groupMembers", "relationshipType":"memberOf", itemType: ["Person","GroupMemeber"]}
	]
},
{
	"name" : "GroupMember" ,
	"parent":"Person",
	"requiredData":[
	],
	"optionalData":[
		{"name": "contribution", "type": "text"}
	]
},{
	"name":"TrackCredits",
	"category": "Audio",
	"requiredData":[
		{"name": "trackId", "type": "id"},
		{"name": "contributors",
			"type":"map",
			"map":{
				"key":{
					"type": "text"
				},
				"value":{
					"relationshipType":"audioClip", // defaults to name
					itemType: "Audio"
				}
			},
		},
	],
	"optionalData":[
		{"name": "producer", "relationshipType":"produced", itemType: ["Person","Artist","Producer"]},
		{"name": "audioClip", "relationshipType":"audioClip", itemType: "Audio" },
		{"name": "audioFile", "relationshipType":"audioFile", itemType: "Audio" },
		{"name": "credits", "relationshipType": "credits", "itemType":"TrackCredits"}
	]
}],
 */

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
        List<Category> categories
        List<Relationship> relationshipDefs
        List<ItemType> types

        if(data.Categories)
            categories = loadCategories(data.Categories);
        if(data.Relationships)
            relationshipDefs = loadRelationships(data.Relationships)
        if(data.Types)
            types = loadItemTypes(data.Types)
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

    /**
     * Load the categories from data set
     *
     * @param jsonCategoryList
     * @return
     */
    List<Category> loadCategories(Object jsonCategoryList) {
        return jsonCategoryList.collect {
            def cat = categoryFromJson(it)
            return graphItService.createCategories([it])
        }.flatten()
    }

    private Category categoryFromJson(jsonCategory) {
        Category parentCat
        if (!jsonCategory.name)
            throw new ValidationException("Categories must have names.");
        if (jsonCategory.parent) {
            parentCat = graphItService.getCategoryByName(jsonCategory.parent)
            if (!parentCat) {
                throw new ValidationException("No such parent: ${jsonCategory.parent}  for Category: ${jsonCategory.name}");
            }
        }
        new Category(name: jsonCategory.name, parent: parentCat)
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
        Relationship parentRel
        if (!jsonRelationship.name)
            throw new ValidationException("Relationship must have name.");
        if (jsonRelationship.parent) {
            parentRel = graphItService.getRelationshipDefByName(jsonRelationship.parent)
            if (!parentRel) {
                throw new ValidationException("No such parent: ${jsonRelationship.parent}  for Relationship: ${jsonRelationship.name}");
            }
        }
        new Relationship(name: jsonRelationship.name, parent: parentRel)
    }
    /**
     * Load the relationshipTypes from data set
     *
     * @param jsonRelationships
     * @return
     */
    List<Relationship> loadRelationships(jsonRelationships) {
        jsonRelationships.collect { it ->
            def newrel = relationshipFromJson(it)
            graphItService.createRelationship(newrel)
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

    private PropertyDef dataElementDefFromJson(jsonPropertyDef) {
        String typeName
        Relationship relationship
        String propertyName
        String defaultValue

        propertyName = jsonPropertyDef.name
        if (!propertyName)
            throw new IllegalArgumentException('Name is empty or missing.')

        defaultValue = jsonPropertyDef.defaultValue
        typeName = jsonPropertyDef.typeName
        if (!typeSystem.isKnownType(typeName)) {
            throw new ValidationException("No such type: $typeName for data element: $propertyName")

            // convert string to proper data type
            if( PrimitiveTypes.isPrimitiveType(typeName) ){
                typeSystem.fixType( typeName, defaultValue )
            }

            // when an item property refers to another item then
            // the relationship between those items will be 'jsonPropertyDef.relationship'
            if (jsonPropertyDef.relationship) {// element relationship constraint
                relationship  = graphItService.getRelationshipDefByName(jsonPropertyDef.relationship)
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
                            defaultValue: defaultValue
                    )
            )
        }
    }
    /**
     *
     * @param jsonItemType
     * @return
     */
    private ItemType itemTypeFromJson(jsonItemType) {

        if (!jsonItemType.name)
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
        Map<String, PropertyDef> dataDefs = jsonItemType.properties.collectEntries {String propertyName, jsonDataElementDef ->
            [ (propertyName) : dataElementDefFromJson(jsonDataElementDef)]
        }
        /// throw exception if not valid
        itemTypeValidator.validate(new ItemType(categories: categories,
                propertyDefs: dataDefs,
                parent: parentType,
                name: jsonItemType.name
        ))
    }

    ItemType loadItemTypes(jsonItemTypes) {
        jsonItemTypes.collect { it ->
            def newtype = itemTypeFromJson(it)
            graphItService.createItemType(newtype)
        }
    }
}
