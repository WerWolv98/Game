package com.werwolv.states;

import com.sun.glass.events.KeyEvent;
import com.werwolv.api.API;
import com.werwolv.engine.resource.ModelLoader;
import com.werwolv.engine.Model;
import com.werwolv.engine.renderer.ModelRenderer;
import com.werwolv.engine.audio.SoundSource;
import com.werwolv.engine.resource.Texture;
import com.werwolv.entities.EntityPlayer;
import com.werwolv.main.Window;
import com.werwolv.tile.Tile;
import com.werwolv.world.Chunk;
import com.werwolv.world.World;
import com.werwolv.world.WorldGenerator;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.EmptyStackException;

public class GameState extends State{

    public World world;
    public EntityPlayer player;

    private Model model = ModelLoader.loadOBJ("game:modelTile");

    private Matrix4f worldSpace = new Matrix4f().scale(API.ContextValues.WORLD_SCALE);
    private Matrix4f invWorldSpace = new Matrix4f().scale(API.ContextValues.WORLD_SCALE);

    private SoundSource source;

    private ModelRenderer modelRenderer = new ModelRenderer();

    public GameState() {

        worldSpace.invert(invWorldSpace);

        this.player = API.thePlayer;
        this.world = API.theWorld;

	    this.getCamera().follow(this.player);
        this.getCamera().setLerp(0.1F);

        this.world.spawnEntity(this.player);
    }

    @Override
    public void init() {
        //source = new SoundSource("game:test", 1.0F, 1.0F, false);

        WorldGenerator worldGen = new WorldGenerator(this.world, 123);
        worldGen.generate(0, 256);
    }

    @Override
    public void update(long delta) {
        if(Window.isKeyPressed(KeyEvent.VK_W))
            this.player.move(0,0.1F);
        if(Window.isKeyPressed(KeyEvent.VK_A))
            this.player.move(-0.1F,0);
        if(Window.isKeyPressed(KeyEvent.VK_S))
            this.player.move(0,-0.1F);
        if(Window.isKeyPressed(KeyEvent.VK_D))
            this.player.move(0.1F,0);

        if(Window.isKeyPressed(KeyEvent.VK_F) && !this.player.getPrevPositions().isEmpty()){
            try {
                this.player.getPrevPositions().pop();
                this.player.getPrevPositions().pop();
                this.player.getPrevPositions().pop();

                Vector2f pos = this.player.getPrevPositions().pop();
                this.player.setX(pos.x);
                this.player.setY(pos.y);
            } catch(EmptyStackException e) {

            }
        }

        if(Window.isKeyPressed(KeyEvent.VK_R))
            if(!source.isPlaying()) {
                source.setPosition(0, 0, 0);
                source.play();
            }
    }

	@Override
	public void render() {
        float width = API.ContextValues.FULL_SCREEN ? API.ContextValues.MONITOR_WIDTH : API.ContextValues.WINDOW_WIDTH;
        float height = API.ContextValues.FULL_SCREEN ? API.ContextValues.MONITOR_HEIGHT : API.ContextValues.WINDOW_HEIGHT;

        int chunksOnScreen = (int) Math.ceil(width / (Chunk.CHUNK_WIDTH * API.ContextValues.WORLD_SCALE)) + 2;
        int verticalTilesOnScreen = (int) Math.ceil((height / API.ContextValues.WORLD_SCALE)) + 2;

        int cameraChunk = (int) Math.ceil(this.getCamera().getX());
        int cameraVerticalTile = (int) Math.ceil(this.getCamera().getY());

        for(int chunk = cameraChunk - chunksOnScreen / 2; chunk < cameraChunk + chunksOnScreen / 2; chunk++) {
            for(int x = 0; x < Chunk.CHUNK_WIDTH; x++) {
                for (int y = Math.max(0, cameraVerticalTile - verticalTilesOnScreen / 2); y < cameraVerticalTile + verticalTilesOnScreen / 2; y++) {
                    Tile tile = this.world.getChunk(chunk).getGridObjects()[x][y];
                    if(tile != null && tile.getTileID() != 0) {
                        //tileRenderer.renderTile(tile.getTileID(), chunk * Chunk.CHUNK_WIDTH + x, y, worldSpace, State.CAMERA);
                        modelRenderer.renderTile(model, API.ResourceRegistry.getTextureFromID(tile.getTileID()), chunk * Chunk.CHUNK_WIDTH + x, y, worldSpace, this.getCamera());
                    }
                }
            }
        }

        modelRenderer.renderColor(model, new Vector3f(1, 1, 0), player.getX(), player.getY(), worldSpace, this.getCamera());
        modelRenderer.renderColor(model, new Vector3f(1, 0, 1),  this.getCamera().getX() + ((int)State.mouseX - API.ContextValues.WINDOW_WIDTH / 2) / API.ContextValues.WORLD_SCALE, this.getCamera().getY() + ((int)-State.mouseY + API.ContextValues.WINDOW_HEIGHT / 2) / API.ContextValues.WORLD_SCALE, worldSpace, this.getCamera());
    }
}
