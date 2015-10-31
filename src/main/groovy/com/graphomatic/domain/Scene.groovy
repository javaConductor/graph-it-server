package com.graphomatic.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

/**
 * Created by lcollins on 10/25/2015.
 */
@Document
class Scene {
    @Id
    String id;
    String name;
    String defaultSceneType = "default";
    List<SceneItem> sceneItems;
}
