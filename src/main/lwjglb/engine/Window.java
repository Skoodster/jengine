package main.lwjglb.engine;

import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;
import org.tinylog.Logger;

import java.util.concurrent.Callable;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Encapsulates all GLFW Window initilization code allowing some basic parameterization of
 * its characteristics (title and size).
 * Needs to be aware of resizing -> setup callback that will be invoked everytime the window
 * is resized. Callback receives width & height in px of the framebuffer
 */
public class Window {
    private final long windowHandle;
    private int height;
    private MouseInput mouseInput;
    private Callable<Void> resizeFunc;
    private int width;

    public Window(String title, WindowOptions opts, Callable<Void> resizeFunc){
        this.resizeFunc = resizeFunc;

        // Initialize GLFW
        if(!glfwInit()){
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Config GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        if (opts.compatibleProfile){
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        } 
        else{
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }
        if (opts.width > 0 && opts.height > 0){
            this.width = opts.width;
            this.height = opts.height;
        }

        // Get resolution of primary monitor
        else{
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            width = vidMode.width();
            height = vidMode.height();
        }

        // Create window
        windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (windowHandle == NULL){
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle, (window, w, h) -> resized(w,h));
        glfwSetErrorCallback((int errorCode, long msgPtr) -> 
            Logger.error("Error code [{}], msg [{}]", errorCode, MemoryUtil.memUTF8(msgPtr))
        );

        //Setup a key callback
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> { 
            keyCallBack(key, action);
        });

        // VSYNC
        // Sync refresh rate with video card. Interval specifies to wait atleast one screen update
        // before drawing to the screen.
        // Refresh rate matches FPS (60hz == 60FPS)
        glfwMakeContextCurrent(windowHandle);
        if (opts.fps > 0){
            glfwSwapInterval(0);
        }
        else{
            glfwSwapInterval(1);
        }
        glfwShowWindow(windowHandle); // Make window visible

        int[] arrWidth = new int[1];
        int[] arrHeight = new int[1];
        glfwGetFramebufferSize(windowHandle, arrWidth, arrHeight);
        width = arrWidth[0];
        height = arrHeight[0];

        mouseInput = new MouseInput(windowHandle);
    }

    // Free the window callbacks and destroy the window
    // Terminate GLFW and free the error callback
    public void cleanup(){
        glfwFreeCallbacks(windowHandle);
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null){
            callback.free();
        }
    }

    public int getHeight(){
        return height;
    }

    public MouseInput getMouseInput(){
        return mouseInput;
    }

    public int getWidth(){
        return width;
    }

    public long getWindowHandle(){
        return windowHandle;
    }

    // Used in game loop
    public boolean isKeyPressed(int keyCode){
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }

    // Calls the key callback whenever a key is pressed, repeated, or released
    public void keyCallBack(int key, int action){
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE){
            glfwSetWindowShouldClose(windowHandle, true); // will be detected in rendering loop
        }
    }

    // Poll for window events. The key callback will only be invoked during this call
    public void pollEvents(){
        glfwPollEvents();
    }

    protected void resized(int width, int height){
        this.width = width;
        this.height = height;
        try {
            resizeFunc.call();
        }
        catch (Exception excp){
            Logger.error("Error calling resize callback", excp);
        }
    }
    
    public void update(){
        glfwSwapBuffers(windowHandle);
    }

    public boolean windowShouldClose(){
        return glfwWindowShouldClose(windowHandle);
    }
    
    public static class WindowOptions{
        public boolean compatibleProfile;
        public int fps;
        public int height;
        public int ups = Engine.TARGET_UPS;
        public int width;
    }
}
