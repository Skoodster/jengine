package main.lwjglb.engine.graph;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.util.*;

import static org.lwjgl.opengl.GL20.*;
/**
 * Create references to the uniforms and sets up their values
 * Constructor receives identifier of the shader program and defines a Map to store the references
 */
public class UniformsMap {
    private int programId;
    private Map<String, Integer> uniforms;

    public UniformsMap(int programId){
        this.programId = programId;
        uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName){

        // Receives shader program identifier and name of uniform
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new RuntimeException("Could not find uniform ["+uniformName+"] in shader program [" + programId+"]");
        }
        uniforms.put(uniformName, uniformLocation);
    }

    // Loads 4x4 Matrix
    public void setUniform(String uniformName, Matrix4f value){
        try (MemoryStack stack = MemoryStack.stackPush()){
            Integer location = uniforms.get(uniformName);
            if (location == null){
                throw new RuntimeException("Could not find uniform [" + uniformName+ "]");
            }
            glUniformMatrix4fv(location.intValue(), false, value.get(stack.mallocFloat(16)));
        }
    }
}
