/*
Name: Ridoy Roy
Date: 10/17/2025
Description: Represents a hand-made room loaded from JSON. 
             Automatically detects exits based on wall gaps.
*/

import java.util.ArrayList;

public class RoomTemplate {
    private Json json;
    private boolean[] exits; // [North, East, South, West]
    private final int ROOM_WIDTH = 1000;
    private final int ROOM_HEIGHT = 500;
    private final int GRID_SIZE = 50;

    public RoomTemplate(Json json) {
        this.json = json;
        this.exits = new boolean[4];
        analyzeExits();
    }

    public boolean[] getExits() {
        return exits;
    }

    // Returns a FRESH list of sprites (Deep Copy via parsing)
    public ArrayList<Sprite> getSprites() {
        ArrayList<Sprite> list = new ArrayList<>();
        Json spriteList = json.get("sprites");
        for(int i = 0; i < spriteList.size(); i++) {
            Json s = spriteList.get(i);
            String type = s.get("type").asString();
            // Filter "Door" objects if you saved them manually, 
            // usually we just want Trees/Chests
            if(type.equals("Door")) continue; 
            
            list.add(Model.loadSprite(s));
        }
        return list;
    }

    private void analyzeExits() {
        // 1. Load sprites temporarily to scan positions
        ArrayList<Sprite> tempSprites = getSprites();
        
        // 2. Define Gap Zones (Where doors should be)
        // North Gap: Center X, Top Y
        boolean northBlocked = isBlocked(tempSprites, ROOM_WIDTH/2, 0);
        // East Gap: Right X, Center Y
        boolean eastBlocked = isBlocked(tempSprites, ROOM_WIDTH, ROOM_HEIGHT/2);
        // South Gap: Center X, Bottom Y
        boolean southBlocked = isBlocked(tempSprites, ROOM_WIDTH/2, ROOM_HEIGHT);
        // West Gap: Left X, Center Y
        boolean westBlocked = isBlocked(tempSprites, 0, ROOM_HEIGHT/2);

        // If NOT blocked, it is an Exit
        exits[0] = !northBlocked; // North
        exits[1] = !eastBlocked;  // East
        exits[2] = !southBlocked; // South
        exits[3] = !westBlocked;  // West
    }

    private boolean isBlocked(ArrayList<Sprite> sprites, int x, int y) {
        // Check a small radius around the target door point
        // If a solid tree is found there, the exit is blocked.
        int checkRadius = 60; // Slightly larger than grid size
        
        for(Sprite s : sprites) {
            if(!s.hasTag("solid")) continue; // Ignore non-solids (Chests)
            
            // Simple distance check
            // Adjust coordinates to center of sprite for accuracy
            int sx = s.getX() + s.getW()/2;
            int sy = s.getY() + s.getH()/2;
            
            // Handle edges: Snap x/y to bounds for distance check
            int tx = x; int ty = y;
            if(x >= ROOM_WIDTH) tx = ROOM_WIDTH - 25;
            if(x <= 0) tx = 25;
            if(y >= ROOM_HEIGHT) ty = ROOM_HEIGHT - 25;
            if(y <= 0) ty = 25;

            if(Math.abs(sx - tx) < checkRadius && Math.abs(sy - ty) < checkRadius) {
                return true; // Found a tree blocking the door!
            }
        }
        return false; // Empty space found
    }
}
