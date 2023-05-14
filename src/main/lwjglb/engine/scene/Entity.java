package main.lwjglb.engine.scene;

import org.joml.*;

public class Entity {
    private final String id;
    private final String modelId;
    private Matrix4f modelMatrix;
    private Vector3f position;
    private Quaternionf rotation;
    private float scale;

    // Model instance has a unique identifier and defines attributes for its position, scale, & rotation
    public Entity(String id, String modelId){
        this.id = id;
        this.modelId = modelId;
        modelMatrix = new Matrix4f(); 
        position = new Vector3f(); // Position as a 3 component vector
        rotation = new Quaternionf(); // Rotation as a quaternion. Allow express rotations w/o Euler angles
        scale = 1; // Scale
    }

    public String getId(){
        return id;
    }

    public String getModelId(){
        return modelId;
    }

    public Matrix4f getModelMatrix(){
        return modelMatrix;
    }

    public Vector3f getPosition(){
        return position;
    }

    public Quaternionf getRotation(){
        return rotation;
    }

    public float getScale(){
        return scale;
    }

    public final void setPosition(float x, float y, float z){
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void setRotation(float x, float y, float z, float angle){
        this.rotation.fromAxisAngleRad(x, y, z, angle);
    }

    public void setScale(float scale){
        this.scale = scale;
    }

    public void updateModelMatrix(){
        modelMatrix.translationRotateScale(position, rotation, scale);
    }
}
