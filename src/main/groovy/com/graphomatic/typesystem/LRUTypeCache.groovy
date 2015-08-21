package com.graphomatic.typesystem

import com.graphomatic.typesystem.domain.ItemType

/**
 * Created by lcollins on 8/20/2015.
 */
class LRUTypeCache extends LRUCache<String, ItemType> {
    LRUTypeCache(int cacheSize) {
        super(cacheSize)
    }
    LRUTypeCache() {
        this(1024)
    }
}
