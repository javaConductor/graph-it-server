package com.graphomatic.typesystem.domain

import com.graphomatic.domain.Category
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 8/11/2015.
 */

/*
{
	"name" : "GroupMember" ,
	"parent":"Person",
	"properties":[
		{"name": "contribution", "type": "text"}
	]
},{
	"name":"TrackCredits",
	"category": "Audio",
	"properties":[
		{"name": "trackId", "type": "id"},
		{"name": "contributors", "typeName":"Audio",
			"relationshipType":"audioClipOf", // defaults to name
			"collectionType":"list",
			"list":{
				"memberConstraint" : {
					itemType: ["Audio"]
			},
		},
		{"name": "producer", "relationshipType":"produced", itemType: ["Person","Artist","Producer"]},
		{"name": "audioClip", "relationshipType":"audioClip", itemType: "Audio" },
		{"name": "audioFile", "relationshipType":"audioFile", itemType: "Audio" },
		{"name": "credits", "relationshipType": "credits", "itemType":"TrackCredits"}
	]
},

 */
@Document
class ItemType {
	@Id
	String id
    String name
    List<Category> categories
    Map<String,PropertyDef> propertyDefs
    transient Set<String> hierarchy
    List<Map> defaults
    String parentName
}
