package main.lwjglb.engine.graph;

import main.lwjglb.engine.scene.Entity; // not impl
import java.util.*;

public class Model {
    private final String id;
    private List<Entity> entitiesList;
    private List<Mesh> meshList;

    public Model(String id, List<Mesh> meshList){
        this.id = id;
        this.meshList = meshList;
        entitiesList = new ArrayList<>();
    }

    public void cleanup(){
        meshList.stream().forEach(Mesh::cleanup);
    }

    public List<Entity> getEntitiesList(){
        return entitiesList;
    }

    public String getId(){
        return id;
    }

    public List<Mesh> getMeshList(){
        return meshList;
    }
}
