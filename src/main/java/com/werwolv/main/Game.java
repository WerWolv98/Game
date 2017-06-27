package com.werwolv.main;

import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import com.google.common.collect.Lists;
import com.werwolv.api.API;
import com.werwolv.api.event.init.InitializationEvent;
import com.werwolv.api.event.init.PostInitializationEvent;
import com.werwolv.api.event.init.PreInitializationEvent;
import com.werwolv.states.State;

public class Game implements Runnable{

	public static final Game INSTANCE = new Game("Game", 1080, 720);
    public static final boolean DEBUG_MODE = false;

    public static final File MODS_FOLDER = new File("mods");


	private BufferStrategy bs;
	private Graphics2D g;
	private Thread thread;
	private boolean running = false;

    private String title;
    private int width, height;
	private Window window;
		
	private Game(String title, int width, int height){
		this.title = title;
		this.width = width;
		this.height = height;
	}
	
	public void init(){
		State.setCurrentState(State.gameState);
		window = new Window(title, width, height);
		API.EVENT_BUS.registerEventHandlers();

		if(!MODS_FOLDER.exists())
            MODS_FOLDER.mkdirs();

		Arrays.asList(MODS_FOLDER.listFiles()).stream().filter(mod -> mod.getName().endsWith(".jar")).forEach(mod -> API.MOD_LOADER.loadMod(mod.getAbsolutePath()));

		API.EVENT_BUS.postEvent(new PreInitializationEvent());
        API.EVENT_BUS.postEvent(new InitializationEvent());
        API.EVENT_BUS.postEvent(new PostInitializationEvent());

    }
	
	public void update(){
		if(State.getCurrentState() != null)
			State.getCurrentState().update();

        API.EVENT_BUS.processEvents();
    }
	
	public void render(){
		bs = window.getCanvas().getBufferStrategy();
		if(bs == null){
			window.getCanvas().createBufferStrategy(3);
			return;
		}
		g = (Graphics2D) bs.getDrawGraphics();
		
		g.clearRect(0, 0, width, height);
		
		if(State.getCurrentState() != null)
			State.getCurrentState().render(g);
		
		bs.show();
		g.dispose();
		
	}
	
	public synchronized void start(){
		if(running) return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public synchronized void stop(){
		if(!running) return;
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run(){

		init();

		int tps = 250;
		double timePerTick = 1000000000 / tps;
		double delta = 0;
		long now;
		long lastTime = System.nanoTime();
		long timer = 0;

		while(running){
			now = System.nanoTime();
			delta += (now - lastTime) / timePerTick;
			timer += now - lastTime;
			lastTime = now;

			if(delta >= 1){
				update();
				delta--;
			}
			if(timer >= 1000000000){
				timer = 0;
			}
			render();
		}

		stop();

	}
	
}