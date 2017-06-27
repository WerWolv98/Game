package com.werwolv.api;

import com.werwolv.api.eventbus.EventBus;
import com.werwolv.api.modloader.ModLoader;
import com.werwolv.tile.Tile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class API {

    public static final EventBus EVENT_BUS = new EventBus();
    public static final ModLoader MOD_LOADER = new ModLoader();

    public static class TileRegistry {
        private static Map<Integer, Tile> registeredTiles = new HashMap<>();

        public static void registerTile(Tile tile) {
            if(registeredTiles.containsKey(tile.getTileID())) {
                Log.wtf("TileRegistry", "Tile with ID " + tile.getTileID() + " is already registered!");
                return;
            }

            registeredTiles.put(tile.getTileID(), tile);
        }

        public static void unregisterTile(Tile tile) {
            if(!registeredTiles.containsKey(tile.getTileID())) {
                Log.wtf("TileRegistry", "Tile with ID " + tile.getTileID() + " isn't registered! Can't unregister!");
                return;
            }

                registeredTiles.remove(tile.getTileID());
        }

        public static void unregisterTile(int tileID) {
            if(!registeredTiles.containsKey(tileID)) {
                Log.wtf("TileRegistry", "Tile with ID " + tileID + " isn't registered! Can't unregister!");
                return;
            }

            registeredTiles.remove(tileID);
        }

        public static Tile getTileFromID(int id) {
            return registeredTiles.get(id);
        }
    }

    public static class ResourceRegistry {
        private static Map<Integer, ModResource> loadedResources = new HashMap<>();
        private static int currentResourceIndex = 0;

        //TODO: Error, not loading mod resources correctly
        public static int registerResource(String path) {
            if(ClassLoader.getSystemClassLoader().getResource(path) == null) {
                Log.wtf("ResourceRegistry", "Cannot load file " + path);
                return -1;
            }

            Log.i("ResourceRegistry", "Loaded resource " + path + " as texture ID " + currentResourceIndex);
            loadedResources.put(currentResourceIndex++, new ModResource(path));
            return currentResourceIndex - 1;
        }

        public static ModResource getResourceFromID(int id) {
            return loadedResources.get(id);
        }
    }

}
