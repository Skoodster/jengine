package main.lwjglb.engine.graph;

// import main.lwjglb.engine.Window;
import main.lwjglb.engine.scene.*;

import java.util.*;

import static org.lwjgl.opengl.GL30.*;
/**
 * Performs the render of all models in the scene
 */
public class SceneRender {
    private ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;

    public SceneRender(){
        // Create two ShaderModuleData instances (one for vertex and other for fragment)
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("src/shaders/scene.vert", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("src/shaders/scene.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);
        createUniforms();
    }

    // Free resources (shader program)
    public void cleanup(){
        shaderProgram.cleanup();
    }

    private void createUniforms(){
        uniformsMap = new UniformsMap(shaderProgram.getProgramId());
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("modelMatrix");
        uniformsMap.createUniform("txtSampler");
    }

    // Doin the drawing to screen
    // Iterate over meshes stored in Scene(), bind them, and draw the vertices of the VAO
    public void render(Scene scene){
        shaderProgram.bind();

        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("txtSampler", 0);

        Collection<Model> models = scene.getModelMap().values();
        TextureCache textureCache = scene.getTextureCache();
        for (Model model : models){
            List<Entity> entities = model.getEntitiesList();

            for (Material material : model.getMaterialList()){
                Texture texture = textureCache.getTexture(material.getTexturePath());
                glActiveTexture(GL_TEXTURE0);
                texture.bind();

                for (Mesh mesh : material.getMeshList()){
                    glBindVertexArray(mesh.getVaoId());
                    for (Entity entity : entities){
                        uniformsMap.setUniform("modelMatrix", entity.getModelMatrix());
                        glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                    }
                }
            }
        }


        glBindVertexArray(0);

        // Unbind VAO and shader program to restore the state
        shaderProgram.unbind();

        
    }

    
}
/**
 * for (Model model : models){
            model.getMeshList().stream().forEach(mesh -> {
                glBindVertexArray(mesh.getVaoId());
                List<Entity> entities = model.getEntitiesList();
                for (Entity entity : entities){
                    uniformsMap.setUniform("modelMatrix", entity.getModelMatrix());
                    // Mode: Specifies primitives for rendering
                    // Count: Specifies number of elements to be rendered
                    // Type: Specifies type of value in indices data
                    // Indices: Specifies offset to apply to the indices data to start rendering
                    glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                }
                
            });
        }
 */