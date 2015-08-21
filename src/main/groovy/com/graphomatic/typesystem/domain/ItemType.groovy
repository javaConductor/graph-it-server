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
},

 */
@Document
class ItemType {
	@Id
	String id
    String name
    List<Category> categories
    List<PropertyDef> propertyDefs
    List<Map> defaults
    String parentName
}
