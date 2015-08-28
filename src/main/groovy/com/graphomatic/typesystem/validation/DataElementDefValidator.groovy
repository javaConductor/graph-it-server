package com.graphomatic.typesystem.validation

import com.graphomatic.typesystem.domain.PropertyDef

/**
 * Created by lcollins on 8/13/2015.
 */
class DataElementDefValidator {

    PropertyDef validate(PropertyDef dataElementDef){
        if (!dataElementDef.name)
            throw new ValidationException("Data element definition must have 'name'.")

        dataElementDef
    }
}
