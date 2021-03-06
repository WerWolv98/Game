package com.werwolv.main;

import com.werwolv.api.API;
import com.werwolv.api.event.input.keyboard.KeyHeldEvent;
import com.werwolv.api.event.input.keyboard.KeyPressedEvent;
import com.werwolv.api.event.input.keyboard.KeyReleasedEvent;
import com.werwolv.api.event.input.keyboard.KeyTypedEvent;
import com.werwolv.api.event.input.mouse.EnumMouseButton;
import com.werwolv.api.event.input.mouse.MouseMovedEvent;
import com.werwolv.api.event.input.mouse.MousePressedEvent;
import com.werwolv.api.event.input.mouse.MouseReleasedEvent;
import com.werwolv.state.State;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {

	private long window;

	private double mouseX, mouseY;

    private static boolean[] pressedKeys = new boolean[1024];
    public static List<Long> pressedKeyList = new ArrayList<>();

    private boolean hasBeenResized = false;

    public Window() {
        API.ContextValues.WINDOW_WIDTH = 1080;
        API.ContextValues.WINDOW_HEIGHT = 720;
    }


    public static boolean isKeyPressed(int keyCode) {
        return pressedKeys[keyCode];
    }

    public boolean hasBeenResized() {
        return hasBeenResized;
    }

    public void setResized(boolean hasBeenResized) {
        this.hasBeenResized = hasBeenResized;
    }

    public void createWindow(boolean fullscreen) {
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW!");

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        API.ContextValues.MONITOR_WIDTH = vidMode.width();
        API.ContextValues.MONITOR_HEIGHT = vidMode.height();

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        if(fullscreen)
            window = glfwCreateWindow(API.ContextValues.MONITOR_WIDTH, API.ContextValues.MONITOR_HEIGHT, "Game", glfwGetPrimaryMonitor(), NULL);
        else
            window = glfwCreateWindow(API.ContextValues.WINDOW_WIDTH, API.ContextValues.WINDOW_HEIGHT, "Game", NULL, NULL);

        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);
            glfwSetWindowPos(window, (vidMode.width() - pWidth.get(0)) / 2, (vidMode.height() - pHeight.get(0)) / 2);
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        glfwSetWindowSizeCallback(window, (window, width, height) ->{
            API.ContextValues.WINDOW_WIDTH = width;
            API.ContextValues.WINDOW_HEIGHT = height;
            hasBeenResized = true;
        });

        glfwSetKeyCallback(window, (window, key, scanCode, action, mods) -> {
            if(key == -1) return;
                switch (action) {
                    case GLFW_PRESS:
                        if (!pressedKeys[key])
                            API.EVENT_BUS.postEvent(new KeyTypedEvent(key));

                        pressedKeys[key] = true;
                        pressedKeyList.add((long) key);
                        API.EVENT_BUS.postEvent(new KeyPressedEvent(key));
                        break;
                    case GLFW_RELEASE:
                        pressedKeys[key] = false;
                        pressedKeyList.remove((long) key);
                        API.EVENT_BUS.postEvent(new KeyReleasedEvent(key));
                        break;
                }
        });

        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            switch (action) {
                case GLFW_PRESS:
                    API.EVENT_BUS.postEvent(new MousePressedEvent(EnumMouseButton.getButtonFromID(button), this.mouseX, this.mouseY, State.getCurrentState().getCamera()));
                    break;
                case GLFW_RELEASE:
                    API.EVENT_BUS.postEvent(new MouseReleasedEvent(EnumMouseButton.getButtonFromID(button), this.mouseX, this.mouseY, State.getCurrentState().getCamera()));
                    break;
            }
        });

        glfwSetCursorPosCallback(window, (window, xPos, yPos) -> {
            this.mouseX = xPos;
            this.mouseY = yPos;
            API.EVENT_BUS.postEvent(new MouseMovedEvent(xPos, yPos, State.getCurrentState().getCamera()));
        });
    }

	public boolean shouldClose() {
		return glfwWindowShouldClose(window);
	}

	public long getWindow() {
		return window;
	}
}
