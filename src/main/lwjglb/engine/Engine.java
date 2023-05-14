package main.lwjglb.engine;

import main.lwjglb.engine.graph.Render;
import main.lwjglb.engine.scene.Scene;

/**
 * Receives in the constructor: title of the window, window options, and a reference
 * to the implementation of the IAppLogic interface.
 * Constructor creates an instance of Window(), Render(), and Scene().
 *
 * cleanup() invokes the other classes'
 */
public class Engine {
    public static final int TARGET_UPS = 30;
    private final IAppLogic appLogic;
    private final Window window;
    private Render render;
    private boolean running;
    private Scene scene;
    private int targetFps;
    private int targetUps;

    public Engine(String windowTitle, Window.WindowOptions opts, IAppLogic appLogic){
        window = new Window(windowTitle, opts, () -> {
            resize();
            return null;
        });
        targetFps = opts.fps;
        targetUps = opts.ups;
        this.appLogic = appLogic;
        render = new Render();
        scene = new Scene(window.getWidth(), window.getHeight());
        appLogic.init(window, scene, render);
        running = true;
    }

    private void cleanup() {
        appLogic.cleanup();
        render.cleanup();
        scene.cleanup();
        window.cleanup();
    }

    private void resize(){
        scene.resize(window.getWidth(), window.getHeight());
    }

    // Define gameloop
    private void run(){
        long initialTime = System.currentTimeMillis();
        float timeU = 1000.0f / targetUps; // Time between updates in ms
        float timeR = targetFps > 0 ? 1000.0f / targetFps : 0; // Time between render calls in ms
        float deltaUpdate = 0; 
        float deltaFps = 0; // Rely on Vsync refresh rate & set FPS to 0

        long updateTime = initialTime;
        while (running && !window.windowShouldClose()){
            window.pollEvents();

            long now = System.currentTimeMillis();
            deltaUpdate += (now - initialTime) / timeU;
            deltaFps += (now - initialTime) / timeR;

            // If max elapsed time for render -> call appLogic.input
            // If max elapsed time for render -> trigger render calls by calling render.render
            // If max elapsed time for update -> call appLogic.update
            if (targetFps <= 0 || deltaFps >= 1){
                window.getMouseInput().input();
                appLogic.input(window, scene, now - initialTime);
            }
            if (deltaUpdate >= 1){
                long diffTimeMillis = now - updateTime;
                appLogic.update(window, scene, diffTimeMillis);
                updateTime = now;
                deltaUpdate--;
            }
            if (targetFps <= 0 || deltaFps >= 1){
                render.render(window, scene);
                deltaFps--;
                window.update();
            }
            initialTime = now;
        }
        cleanup(); // Free resources
    }

    // GLFW and polling events required to be initialized from the main thread.
    // Execute everything from the main thread to avoid creating a new thread in start()
    public void start(){
        running = true;
        run();
    }
    public void stop(){
        running = false;
    }
}
