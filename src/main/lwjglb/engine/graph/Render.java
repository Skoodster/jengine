package main.lwjglb.engine.graph;

import org.lwjgl.opengl.GL;
import main.lwjglb.engine.Window;
import main.lwjglb.engine.scene.Scene;

import static org.lwjgl.opengl.GL11.*;
/**
 * Clears screen
 */
public class Render {
    private SceneRender sceneRender;

    public Render() {
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        sceneRender = new SceneRender();
    }

    public void cleanup(){
        sceneRender.cleanup();
    }

    public void render(Window window, Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        sceneRender.render(scene);
    }
}
