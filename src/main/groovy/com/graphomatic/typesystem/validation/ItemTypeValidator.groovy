package com.graphomatic.typesystem.validation

import com.graphomatic.typesystem.domain.ItemType

/**
 * Created by lcollins on 8/13/2015.
 */
class ItemTypeValidator {

    def validate(ItemType itemType){
        if (!itemType.name)
            throw new ValidationException("Item type must have 'name'.")
        itemType
    }
}
