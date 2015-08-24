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

    DbAccess dbAccess
    LRUTypeCache cache

    def TypeSystem(DbAccess dbAccess) {
        this.dbAccess = dbAccess
        cache = new LRUTypeCache()
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

    /**
     *  Add dataDefs from newDataDefs to origDataDefs also replace
     *  Maybe this is tooo simple
     *
     * @param propertiesAndDefaults
     * @param newDataDefs
     * @param newDefaults
     * @return map{ defaults, propertyDefs}
     */
    Map applyPropertiesAndDefaults(Map propertiesAndDefaults, List newDataDefs, Map newDefaults) {
        /// first update the existing propertyDefs in the map
        List appliedDatadefs = propertiesAndDefaults.propertyDefs.collect { PropertyDef origDataDef ->

            def replacement = newDataDefs.find { PropertyDef nuDataDef ->
                nuDataDef.name == origDataDef.name
            }
            return (replacement) ?: origDataDef
        }

        //// now we have replaced the ones from original - let's get the new ones from newDataDefs
        List newDefs = newDataDefs.split { PropertyDef nuDataDef ->
            return appliedDatadefs.find { PropertyDef origDataDef ->
                nuDataDef.name == origDataDef.name
            }
        }[1]

        /// now we have both lists so concat
        [dataDefs: appliedDatadefs + newDefs,
         defaults: propertiesAndDefaults.defaults << newDefaults]
    }

    Map getTypePropertiesAndDefaults(String typeName) {
        def ret = [defaults: [], propertyDefs: [:]]
        if (!typeName)
            return ret

        ItemType itemType = cache[typeName]
        //if its in the cache that's all we need
        if (itemType) {
            return [defaults: itemType.defaults, propertyDefs: itemType.propertyDefs]
        } else {// not in the cache so get it fron db
            itemType = dbAccess.getItemTypeByName(typeName)
            if (!itemType) {// not in db so return empty map
                return ret
            }
        }
        // now we have an ItemType so lets use it
        if (itemType.parentName) {// preload list with parent stuff
            ret = getTypePropertiesAndDefaults(itemType.parentName)
        }

        ret = applyPropertiesAndDefaults(ret, itemType.propertyDefs, itemType.defaults)
        ret
    }

    List getTypeHierarchy(ItemType itemType) {
        return [itemType.name] << (itemType.parentName
                ? getTypeHierarchy(resolveType(itemType.parentName))
                : [])
    }


    List<Property> createDefaultInitData(ItemType itemType, Map initProperties) {
        // get the required properties
        List requiredAndOptional = itemType.propertyDefs.split { PropertyDef propertyDef ->
            propertyDef.required
        }

        List propNames = itemType.propertyDefs.collect {pdef ->
            pdef.name
        }

        List req = requiredAndOptional[0]
        List optional = requiredAndOptional[1]
        List reqProps = req.collect { PropertyDef propertyDef ->
            def instanceValue = initProperties[propertyDef.name]
            if (!instanceValue && !itemType?.defaults[propertyDef.name]) {
                throw new ValidationException("Could not create item of type ${itemType.name}: No value for required property: ${propertyDef.name}")
            }
            createProperty(propertyDef, instanceValue ?: itemType.defaults[propertyDef.name])
        }

        List optionalProps = optional.collect { PropertyDef propertyDef ->
            def instanceValue = initProperties[propertyDef.name]
            // defaultValue can be either: value or item reference
            if (instanceValue || itemType?.defaults[propertyDef.name]) {
                createProperty(propertyDef, instanceValue ?: itemType.defaults[propertyDef.name])
            }
        }

        List extraInitProps = initProperties.keySet().collect { String name ->
            Object value = initProperties[name]
            return (propNames.contains(name)) ? [] : [synthesizeProperty(name, value)]
        }.flatten()

        reqProps << (optionalProps <<  extraInitProps)
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

    Property synthesizeProperty(String propertyName, Object value) {
        // value may be a list or single value
        // if its a list it can have a reference in the list
        // if its s single value it may be a reference

        if (value instanceof  List)
            synthesizeListProperty(propertyName, value as List)
        else {
            if(isReference(value)){
                new Property(  name: propertyName, value: value, collectionType: "")
            }else{
                new Property(  name: propertyName, value: value, collectionType: "")
            }
        }
    }

    boolean isReference( Object o){
        String s = o?.toString()?.trim()
        (s.startsWith('$ref:[') && s.endsWith(']') )
    }

    Property synthesizeListProperty(String propertyName, List list) {
            new Property(  name: propertyName, value: list, collectionType: "list")
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
       canAssign( valueItemType.name, typeName )
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
        sourceType.hierarchy.contains(targetTypeName)
    }

    boolean isKnownType(String typeName) {
        PrimitiveTypes.isPrimitiveType(typeName) || resolveType(typeName)
    }
}
