package com.graphomatic

/**
 * Created by lcollins on 6/28/2015.
 */
class Utils {
    static Map subMap( Map m, String prefix ){
        Map ret = [:]
        m.each { String k, v ->
            if (k.startsWith(prefix) ){
                ret[k.substring(prefix.length())] = v;
            }
        }
        ret
    }

    static Map persistentFields(Map m){
        m.findAll { !['class', 'metaClass'].contains(it.key) }

    }
}
