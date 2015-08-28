package com.graphomatic.typesystem

import com.graphomatic.domain.Property
import com.graphomatic.service.DbAccess
import com.graphomatic.typesystem.domain.ItemType
import com.graphomatic.typesystem.domain.PropertyDef
import com.graphomatic.typesystem.validation.ValidationException

/**
 * Created by lcollins on 8/20/2015.
 */
class TypeSystem {
    static final String BASE_TYPE_NAME='$thing'
    def baseItemType = new ItemType(
            name: BASE_TYPE_NAME,
            propertyDefs: [
                createDateTime: [name : "createDateTime", collectionType: '', typeName: 'dateTime', required: true] as PropertyDef
                ],
            hierarchy: [],
            categories: ["ALL"]
            )

    DbAccess dbAccess
    LRUTypeCache cache

    def TypeSystem(DbAccess dbAccess) {
        this.dbAccess = dbAccess
        cache = new LRUTypeCache()
        cache[baseItemType.name]=baseItemType
    }

    ItemType resolveType(String typeName) {
        if (!typeName)
            return null;

        if (cache[typeName]) {
            return cache[typeName]
        }

        ItemType itemType = dbAccess.getItemTypeByName(typeName)
        if (!itemType)
            return null;

        Map propertiesAndDefaults = this.getTypePropertiesAndDefaults(typeName)
        itemType.defaults = propertiesAndDefaults.defaults
        itemType.propertyDefs = propertiesAndDefaults.propertyDefs
        itemType.hierarchy = getTypeHierarchy(itemType)

        cache[typeName] = itemType
        itemType
    }

    Map getTypePropertiesAndDefaults(String typeName) {
        boolean foundInCache;

        String t = typeName
        List<ItemType> itemTypeList = []

        while( t && !foundInCache ) {
            ItemType itemType = cache[t]

            if(!itemType)
                itemType = dbAccess.getItemTypeByName(t)
            else
                foundInCache = true;

            if(!itemType)
                throw  new ValidationException("No such type: $t");
            itemTypeList.add itemType
            t = itemType.parentName
        }

        itemTypeList.reverse().inject( [ propertyDefs: [:], defaults:[:] ] ){Map accum, ItemType  type ->
            [propertyDefs : ((accum.propertyDefs ) << (type.propertyDefs)),
            defaults :   type.defaults ?  (accum.defaults  <<  type.defaults) : accum.defaults ]
        }
    }

    List getTypeHierarchy(ItemType itemType) {
        return [itemType.name] << (itemType.parentName
                ? getTypeHierarchy(resolveType(itemType.parentName))
                : [])
    }

    Map<String, Property> createDefaultInitData(ItemType itemType, Map initProperties) {
        itemType.propertyDefs.collectEntries {String propertyName, PropertyDef propertyDef ->
            if (propertyDef.required &&  !initProperties[propertyName] && !itemType?.defaults[propertyName]) {
                throw new ValidationException("Could not create item of type ${itemType.name}: No value for required property: ${propertyName}")
            }
            [(propertyName) : createProperty(propertyDef, initProperties[propertyName] ?: itemType.defaults[propertyName])]
        }
    }

    Property createProperty(PropertyDef propertyDef, Object value, boolean validate = false) {
        // if we have a "value" - a literal value of a primitive type
        switch (propertyDef.collectionType) {

            case "list":
                List values = value instanceof List ? value : [value]
                if (validate) {
                    validateValues(propertyDef.typeName, values)
                }
                values = fixTypes(propertyDef, values)
                new Property(name: propertyDef.name, value: values, collectionType: "list")
                break;

            case "":
            case null:
                if (validate) {
                    validateValues(propertyDef.typeName, [value])
                }
                new Property(name: propertyDef.name, value: fixType(propertyDef, value), collectionType: "")
                break;

            default:
                throw new ValidationException("Bad collectionType: ${propertyDef.collectionType}")
        }
    }

    List fixTypes(PropertyDef propertyDef, List values){
        values.collect {
            fixType(propertyDef,it)
        }
    }

    Object fixType(String  typeName, Object value){
        PrimitiveTypes.fromString(typeName, value)
    }

    boolean isReference( Object o){
        String s = o?.toString()?.trim()
        (s.startsWith('$ref:[') && s.endsWith(']') )
    }

    boolean validateValues(String typeName, List values) {
        values.every { value ->
            boolean ok = validateValue(typeName, value)
            if (!ok)
                throw new ValidationException("Value: $value not valid for type: ${typeName}")
        }
    }

    boolean validateValue(String typeName, Object value) {
        ((dataValidForPrimitiveTypes(typeName, value)) ||
                (dataValidForTypes(typeName, value)))
    }

    boolean dataValidForTypes(String typeName, Object value) {
        ItemType valueItemType = getReferenceType(value)
        // is the type of the valueItem compatible
        // with any of the valid types
       canAssign( valueItemType?.name, typeName )
    }

    boolean dataValidForPrimitiveTypes(String typeName, Object value) {
        PrimitiveTypes.validate(typeName, value)
    }

    ItemType getReferenceType(String itemReference) {
        //$ref:[bcbdbcbd5-cnndn443-cdnnddncmdmd]
        String itemId = itemReference.substring(6, itemReference.length() - 1)
        def typeName = dbAccess.getTypeOfItem(itemId)
        resolveType(typeName)
    }

    boolean canAssign(String sourceTypeName, String targetTypeName) {
        ItemType sourceType = resolveType(sourceTypeName)
        // search for  target type up the hierarchy of source type
        sourceType?.hierarchy?.contains(targetTypeName)
    }

    boolean isKnownType(String typeName) {
        PrimitiveTypes.isPrimitiveType(typeName) || resolveType(typeName)
    }

    boolean validateProperties(ItemType itemType, Map<String, Property> dataList) {
        // make sure the primitive types are ok and the others are references
        dataList.keySet().every { String name ->
            PropertyDef pdef = itemType.propertyDefs[name]
            PrimitiveTypes.isPrimitiveType ( pdef?.typeName ) ? (
                PrimitiveTypes.validate(pdef?.typeName,  dataList[name]?.value )
            ) : (
                isReference(dataList[ name ].value ) &&
                        canAssign(pdef.name, getReferenceType(dataList[ name ]?.value)?.name)
            )
        }
    }
}
