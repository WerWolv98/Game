package world;

import com.werwolv.entities.Entity;
import com.werwolv.tile.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class World {

    public static final int WORLD_WIDTH = 4096;
    public static final int WORLD_HEIGHT = 512;

    private List<Entity> entities = new ArrayList<>();
    private Map<Integer, Chunk> chunks = new HashMap<>();

    public void spawnEntity(Entity entity) {
        this.entities.add(entity);
    }

    public Chunk getChunk(int coord) {
        if(!chunks.containsKey(coord))
            this.chunks.put(coord, new Chunk(new int[Chunk.CHUNK_WIDTH][World.WORLD_HEIGHT]));

        return chunks.get(coord);
    }

    public int getChunkCount() {
        return chunks.size();
    }

    private int getTile(int posX, int posY) {
        int chunk = (int)Math.floor(posX / Chunk.CHUNK_WIDTH);

        return chunks.get(chunk).getTiles()[posX][posY];
    }

    public void setTile(Tile tile, int posX, int posY) {
        int chunk = (int)Math.floor(posX / Chunk.CHUNK_WIDTH);

        if(!chunks.containsKey(chunk))
            this.chunks.put(chunk, new Chunk(new int[Chunk.CHUNK_WIDTH][World.WORLD_HEIGHT]));

        this.getChunk(chunk).setTile(tile, posX - (chunk * Chunk.CHUNK_WIDTH), posY);
    }

    public void setTile(int tileId, int posX, int posY) {
        int chunk = (int)Math.floor(posX / Chunk.CHUNK_WIDTH);

        if(!chunks.containsKey(chunk))
            this.chunks.put(chunk, new Chunk(new int[Chunk.CHUNK_WIDTH][World.WORLD_HEIGHT]));

        this.getChunk(chunk).setTile(tileId, posX - (chunk * Chunk.CHUNK_WIDTH), posY);
    }

}
