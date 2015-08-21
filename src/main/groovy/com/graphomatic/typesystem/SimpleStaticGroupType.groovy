package com.graphomatic.typesystem

import com.graphomatic.domain.GraphItem
import com.graphomatic.service.GraphItService
import com.graphomatic.typesystem.GroupType

/**
 * Created by lcollins on 8/17/2015.
 */
class SimpleStaticGroupType implements GroupType {

    List<String> _memberIds = []
    def GroupType(){
        setName "simple_static"
        setGroupTemplate "/group/1/template"
        //groupProperties =
    }

    @Override
    List<GraphItem> members(GraphItService service) {
        _memberIds.collect { itemId ->
                service.getGraphItem(itemId)
        }
    }

    List<GraphItem> addMember(GraphItem item) {
        _memberIds.add(item.id)
        this
    }



}
