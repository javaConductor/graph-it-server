package com.graphomatic.typesystem

import com.graphomatic.domain.GraphItem
import com.graphomatic.typesystem.domain.PropertyDef
import com.graphomatic.service.GraphItService

/**
 * Created by lcollins on 8/17/2015.
 */
interface GroupType {
    String name = ""
    String  defaultTemplate="";
    List<GraphItem> members(GraphItService service);
    List<PropertyDef> groupProperties;
}
