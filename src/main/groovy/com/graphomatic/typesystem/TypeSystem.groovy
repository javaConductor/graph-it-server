package com.graphomatic.typesystem

import com.graphomatic.domain.GraphItem
import com.graphomatic.domain.Property
import com.graphomatic.service.DbAccess
import com.graphomatic.typesystem.domain.PropertyDef
import com.graphomatic.typesystem.domain.ItemType
import com.graphomatic.typesystem.validation.ValidationException
import org.codehaus.groovy.runtime.memoize.LRUCache

/**
 * Created by lcollins on 8/20/2015.
 */
class TypeSystem {

    DbAccess dbAccess
    LRUTypeCache cache
    def TypeSystem(DbAccess dbAccess){
        this.dbAccess = dbAccess
        cache = new LRUTypeCache()
    }

    ItemType resolveType(String typeName) {
        if( !typeName)
            return null;
        if(cache[typeName]){
            return cache[typeName]
        }

        ItemType itemType = dbAccess.getItemTypeByName(typeName)
        if( !itemType)
            return null;
        Map propertiesAndDefaults = this.getTypePropertiesAndDefaults(itemType)
        itemType.defaults = propertiesAndDefaults.defaults
        itemType.propertyDefs = propertiesAndDefaults.propertyDefs
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
        [dataDefs :appliedDatadefs + newDefs,
         defaults: propertiesAndDefaults.defaults << newDefaults]
    }

    Map getTypePropertiesAndDefaults(String  typeName){
        def ret = [defaults: [], propertyDefs: [:]]
        if(!typeName)
            return  ret

        ItemType itemType = cache[typeName]
        //if its in the cache that's all we need
        if(itemType) {
            return  [defaults: itemType.defaults, propertyDefs: itemType.propertyDefs]
        }else{// not in the cache so get it fron db
            itemType = dbAccess.getItemTypeByName(typeName)
            if(!itemType){// not in db so return empty map
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



    boolean createDefaultItem(ItemType itemType, Map initProperties){

        List<Property> data = createDefaultInitData(itemType, initProperties)

        new GraphItem(categories: itemType.categories,
                typeName: itemType.name,
                data: data)
    }

    List<Property> createDefaultInitData(ItemType itemType, Map initProperties) {
        // get the required properties
        List req = itemType.propertyDefs.findAll {PropertyDef propertyDef ->
           propertyDef.required
        }

        List reqProps = req.collect { PropertyDef propertyDef ->
            def defaultValue = initProperties[propertyDef.name]
            if(!defaultValue){
                throw  new ValidationException("Could not create item of type ${itemType.name}: No value for required property: ${propertyDef.name}")
            }
            // defaultValue can be either: value or item reference
            createProperty(propertyDef, defaultValue)
        }
    }

    Property createProperty(PropertyDef propertyDef, Map defaultValue, boolean validate = false) {
        // if we have a "value" - a literal value of a primitive type


        switch (propertyDef.containerType){
            case "list":
                List values = defaultValue.value

                if ( validate ){
                    validateValues(propertyDef.validDataTypes,propertyDef.validItemTypes, values)
                }
                new Property(name: propertyDef.name, value: values, collection: true)
                break;
            case null:
                Object value = defaultValue.value

                if ( validate ){
                    validateValues(propertyDef.validDataTypes,propertyDef.validItemTypes, [value])
                }
                new Property(name: propertyDef.name, value: value, collection:false)

                break;
            default:
                break
        }



        if ( defaultValue.value){
            // if this type is constrained on its types then
            if ( propertyDef.validDataTypes) {
                if (PrimitiveTypes.validate(propertyDef.validDataTypes, defaultValue.value)){
                    new Property(value: [defaultValue.value], name: propertyDef.name, type: "V")
                }else{
                    throw new ValidationException("Value: ${defaultValue.value} not valid for types: ${propertyDef.validDataTypes}")
                }
            }
        }else if (defaultValue.reference){
            // if we have a reference then we create a property
            // reference to an existing object

            List l = propertyDef.containerType ?  (defaultValue.reference ? : )
            new Property(name: propertyDef.name, items: [], type: "R" )

        }
    }

    boolean validateValues(List<String> primitiveTypes, List<String> itemTypes, List values) {

        values.every{ value ->
            validateValue(primitiveTypes, itemTypes, value)
        }


    }

    boolean validateValue(List<String> primitiveTypes, List<String> itemTypes, Object value) {
        if (PrimitiveTypes.validate(primitiveTypes, value)){

        }
    }
}
