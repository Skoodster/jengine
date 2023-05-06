package main.lwjglb.engine.scene;

import main.lwjglb.engine.graph.Model;
import java.util.*;
/**
 * Hold 3D scene values (models, lights, etc)
 */
public class Scene {
    private Map<String, Model> modelMap;
    private Projection projection;

    public Scene(int width, int height){
        modelMap = new HashMap<>();
        projection = new Projection(width, height);
    }
    
    public void addEntity(Entity entity){
        String modelId = entity.getModelId();
        Model model = modelMap.get(modelId);
        if (model == null){
            throw new RuntimeException("Could not find model ["+modelId+"]");
        }
        model.getEntitiesList().add(entity);
    }

    // For 2D stuf
    //public void addMesh(String meshId, Mesh mesh){
    //    meshMap.put(meshId, mesh);
    //}

    public void addModel(Model model){
        modelMap.put(model.getId(), model);
    }

    public void cleanup(){
        modelMap.values().stream().forEach(Model::cleanup);
    }

    public Map<String, Model> getModelMap(){
        return modelMap;
    }

    public Projection getProjection(){
        return projection;
    }

    public void resize(int width, int height){
        projection.updateProjMatrix(width, height);
    }
}
