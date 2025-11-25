/*
Name: Ridoy Roy
Date: 10/17/2025
Description: Hybrid Generator (Hand-made Templates + Procedural Fallback).
*/

import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.Constructor;
import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.Collections;
import java.util.Random;
import java.io.File; // Needed for folder scanning

public class Model {

    private ArrayList<Sprite> sprites;
    private Link link;
    
    // --- WORLD STATE ---
    private HashMap<Point, ArrayList<Sprite>> visitedRooms;
    private HashMap<Point, boolean[]> roomExits;
    private int currentRoomX = 0;
    private int currentRoomY = 0;

    // --- TEMPLATES ---
    private ArrayList<RoomTemplate> templates;

    // Editor Tools
    private ArrayList<Sprite> itemsICanAdd;
    private int itemNum = 0;
    private int rupeeCount = 0;

    private final int GRID_SIZE = 50;
    private final int ROOM_WIDTH = 1000;
    private final int ROOM_HEIGHT = 500;
    
    private int mapX = 0; 
    private int mapY = 0;

    public Model(){
        this.visitedRooms = new HashMap<>();
        this.roomExits = new HashMap<>();
        this.templates = new ArrayList<>();
        
        // 1. Load Templates
        loadTemplates();

        this.sprites = new ArrayList<Sprite>(); 
        link = new Link(200, 200);
        this.sprites.add(link);

        this.itemsICanAdd = new ArrayList<Sprite>();
        this.itemsICanAdd.add(new Tree(0, 0));
        this.itemsICanAdd.add(new TreasureChests(0, 0));
        
        generateRoom(0, 0, -1);
        saveCurrentRoomState();
    }

    // --- Template Loading ---
    private void loadTemplates() {
        File folder = new File("maps");
        if(!folder.exists()) {
            System.out.println("No 'maps' folder found. Using procedural only.");
            return;
        }

        File[] files = folder.listFiles();
        if(files == null) return;

        for(File f : files) {
            if(f.isFile() && f.getName().endsWith(".json")) {
                try {
                    Json j = Json.load(f.getPath());
                    RoomTemplate t = new RoomTemplate(j);
                    templates.add(t);
                    
                    // Debug print
                    boolean[] e = t.getExits();
                    System.out.println("Loaded " + f.getName() + " Exits:[N:" + e[0] + " E:" + e[1] + " S:" + e[2] + " W:" + e[3] + "]");
                } catch(Exception e) {
                    System.out.println("Failed to load template: " + f.getName());
                }
            }
        }
    }
    
    private void saveCurrentRoomState() {
        ArrayList<Sprite> roomState = new ArrayList<>(sprites);
        roomState.remove(link);
        visitedRooms.put(new Point(currentRoomX, currentRoomY), roomState);
    }
    
    public void update(int screenWidth, int screenHeight){  
        Iterator<Sprite> it = sprites.iterator();
        while(it.hasNext()){
            Sprite s = it.next();
            boolean isAlive = s.update();
            rupeeCount += s.getScore();
            if(!isAlive){ 
                it.remove();
            }
        }

        for (int i = 0; i < sprites.size(); i++) {
            for (int j = i + 1; j < sprites.size(); j++) {
                Sprite s1 = sprites.get(i);
                Sprite s2 = sprites.get(j);
                if (Sprite.doesCollide(s1, s2)) {
                    s1.onCollision(s2);
                    s2.onCollision(s1);
                }
            }
        }

        checkRoomExit();
    }

    private void checkRoomExit() {
        int gapCenterX = ROOM_WIDTH/2 - GRID_SIZE/2;
        int gapCenterY = ROOM_HEIGHT/2 - GRID_SIZE/2;
        
        int nextGridX = currentRoomX;
        int nextGridY = currentRoomY;
        int entrySide = -1;
        boolean changedRoom = false;

        if(link.getX() > ROOM_WIDTH) {
            nextGridX++;
            entrySide = 3; 
            link.setPosition(50, gapCenterY); 
            changedRoom = true;
        }
        else if(link.getX() < -link.getW()) {
            nextGridX--;
            entrySide = 1; 
            link.setPosition(ROOM_WIDTH - 50, gapCenterY); 
            changedRoom = true;
        }
        else if(link.getY() > ROOM_HEIGHT) {
            nextGridY++;
            entrySide = 0; 
            link.setPosition(gapCenterX, 50);
            changedRoom = true;
        }
        else if(link.getY() < -link.getH()) {
            nextGridY--;
            entrySide = 2; 
            link.setPosition(gapCenterX, ROOM_HEIGHT - 50);
            changedRoom = true;
        }
        
        if(changedRoom) {
            link.setDestination(link.getX(), link.getY()); 
            loadOrGenerateRoom(nextGridX, nextGridY, entrySide);
        }
    }

    private void loadOrGenerateRoom(int rx, int ry, int entrySide) {
        saveCurrentRoomState();
        currentRoomX = rx;
        currentRoomY = ry;
        Point newPoint = new Point(rx, ry);
        
        if(visitedRooms.containsKey(newPoint)) {
            System.out.println("Loading persistent room: " + rx + "," + ry);
            sprites = visitedRooms.get(newPoint);
            sprites.add(link); 
        } else {
            System.out.println("Generating new room: " + rx + "," + ry);
            sprites = new ArrayList<>();
            sprites.add(link);
            generateRoom(rx, ry, entrySide);
            saveCurrentRoomState(); 
        }
    }

    public void generateRoom(int rx, int ry, int entrySide) {
        Random rand = new Random();
        
        // 1. Determine Constraints [N, E, S, W]
        // null = unrestricted, true = MUST open, false = MUST closed
        Boolean[] constraints = new Boolean[4]; 

        // Check Neighbors to set constraints
        constraints[0] = checkNeighborConstraint(rx, ry-1, 2); // North needs to match North's South
        constraints[1] = checkNeighborConstraint(rx+1, ry, 3); // East needs to match East's West
        constraints[2] = checkNeighborConstraint(rx, ry+1, 0); // South needs to match South's North
        constraints[3] = checkNeighborConstraint(rx-1, ry, 1); // West needs to match West's East

        // If we are entering from a side, that side MUST be open
        if(entrySide != -1) {
            constraints[entrySide] = true;
        }

        // 2. Try to find a matching Template
        ArrayList<RoomTemplate> candidates = new ArrayList<>();
        for(RoomTemplate t : templates) {
            boolean match = true;
            boolean[] exits = t.getExits();
            for(int i = 0; i < 4; i++) {
                if(constraints[i] != null && constraints[i] != exits[i]) {
                    match = false; 
                    break;
                }
            }
            if(match) candidates.add(t);
        }

        boolean useProcedural = true;
        if(!candidates.isEmpty()) {
            // 3. Pick Random Template
            RoomTemplate chosen = candidates.get(rand.nextInt(candidates.size()));
            sprites.addAll(chosen.getSprites());
            
            // Save exits to memory
            roomExits.put(new Point(rx, ry), chosen.getExits());
            System.out.println("Used Template!");
            useProcedural = false;
        }

        // 4. Fallback to Procedural if no template fits
        if(useProcedural) {
            generateProceduralFallback(rx, ry, constraints, rand);
        }
    }

    private void generateProceduralFallback(int rx, int ry, Boolean[] constraints, Random rand) {
        System.out.println("Using Procedural Fallback");
        boolean[] exits = new boolean[4];
        for(int i=0; i<4; i++) {
            if(constraints[i] != null) exits[i] = constraints[i];
            else exits[i] = rand.nextBoolean(); // Random if unconstrained
        }
        // Safety: Ensure at least 2 exits for start
        if(visitedRooms.isEmpty()) { exits[1]=true; exits[2]=true; }

        roomExits.put(new Point(rx, ry), exits);

        // Generate Walls and Hedges (Same logic as before)
        int numHedges = 8 + rand.nextInt(5); 
        for(int i = 0; i < numHedges; i++) {
            int hx = rand.nextInt((ROOM_WIDTH/GRID_SIZE) - 2) * GRID_SIZE + GRID_SIZE;
            int hy = rand.nextInt((ROOM_HEIGHT/GRID_SIZE) - 2) * GRID_SIZE + GRID_SIZE;
            boolean horizontal = rand.nextBoolean();
            int length = 3 + rand.nextInt(4); 
            for(int j = 0; j < length; j++) {
                int tx = hx + (horizontal ? j * GRID_SIZE : 0);
                int ty = hy + (horizontal ? 0 : j * GRID_SIZE);
                if(!isClearZone(tx, ty) && !isOccupied(tx, ty)) sprites.add(new Tree(tx, ty));
            }
        }

        for(int y = 0; y <= ROOM_HEIGHT; y += GRID_SIZE) {
            for(int x = 0; x <= ROOM_WIDTH; x += GRID_SIZE) {
                boolean isEdge = false;
                boolean isGap = false;

                if(y == 0) {
                    isEdge = true;
                    if(exits[0] && Math.abs(x - (ROOM_WIDTH/2 - GRID_SIZE/2)) < GRID_SIZE) isGap = true;
                }
                else if(y >= ROOM_HEIGHT) {
                    isEdge = true;
                    if(exits[2] && Math.abs(x - (ROOM_WIDTH/2 - GRID_SIZE/2)) < GRID_SIZE) isGap = true;
                }
                else if(x == 0) {
                    isEdge = true;
                    if(exits[3] && Math.abs(y - (ROOM_HEIGHT/2 - GRID_SIZE/2)) < GRID_SIZE) isGap = true;
                }
                else if(x >= ROOM_WIDTH) {
                    isEdge = true;
                    if(exits[1] && Math.abs(y - (ROOM_HEIGHT/2 - GRID_SIZE/2)) < GRID_SIZE) isGap = true;
                }

                if(isEdge && !isGap) {
                    if(!isOccupied(x, y)) sprites.add(new Tree(x, y));
                }
                
                if(!isEdge) {
                    if(isClearZone(x, y) || isOccupied(x, y)) continue;
                    float chance = rand.nextFloat();
                    if(chance < 0.12) sprites.add(new Tree(x, y));
                    else if(chance > 0.99) sprites.add(new TreasureChests(x, y));
                }
            }
        }
    }

    // Returns: true (Must Open), false (Must Close), null (Don't Care)
    private Boolean checkNeighborConstraint(int nx, int ny, int neighborWallIndex) {
        Point p = new Point(nx, ny);
        if(roomExits.containsKey(p)) {
            return roomExits.get(p)[neighborWallIndex];
        }
        return null; // Neighbor doesn't exist, so we don't care
    }

    // --- Utilities ---
    public static Sprite loadSprite(Json s) {
        String type = s.get("type").asString();
        if(type.equals("tree")) return new Tree(s);
        if(type.equals("Tree")) return new Tree(s);
        if(type.equals("chest")) return new TreasureChests(s);
        if(type.equals("TreasureChests")) return new TreasureChests(s);
        return null;
    }

    private boolean isOccupied(int x, int y) {
        for(Sprite s : sprites) {
            if(s.getX() == x && s.getY() == y) return true;
        }
        return false;
    }

    private boolean isClearZone(int x, int y) {
        int centerX = ROOM_WIDTH / 2;
        int centerY = ROOM_HEIGHT / 2;
        if(Math.abs(x - centerX) < 150 && Math.abs(y - centerY) < 150) return true;
        if(Math.abs(x - centerX) < 100 && y < 200) return true;
        if(Math.abs(x - centerX) < 100 && y > ROOM_HEIGHT - 200) return true;
        if(x < 200 && Math.abs(y - centerY) < 100) return true;
        if(x > ROOM_WIDTH - 200 && Math.abs(y - centerY) < 100) return true;
        return false;
    }

    private boolean isSolidGrid(int col, int row) {
        int x = col * GRID_SIZE;
        int y = row * GRID_SIZE;
        if(x < -1000 || x > 2000 || y < -1000 || y > 2000) return true;
        for(Sprite s : sprites) {
            if(s == link) continue; 
            if(s instanceof TreasureChests) continue;
            if(s.hasTag("solid")) {
                if(x < s.getX() + s.getW() && x + GRID_SIZE > s.getX() && y < s.getY() + s.getH() && y + GRID_SIZE > s.getY()) return true;
            }
        }
        return false;
    }

    public void moveLinkDirectly(int x, int y) {
        link.setDestination(x - link.getW()/2, y - link.getH()/2);
    }

    public void pathfindTo(int targetX, int targetY) {
        // (Keep existing pathfinding code exactly the same)
        int startCol = link.getX() / GRID_SIZE;
        int startRow = link.getY() / GRID_SIZE;
        int endCol = targetX / GRID_SIZE;
        int endRow = targetY / GRID_SIZE;
        if(startCol == endCol && startRow == endRow) return;
        Queue<Point> frontier = new LinkedList<>();
        HashMap<Point, Point> cameFrom = new HashMap<>();
        Point start = new Point(startCol, startRow);
        Point goal = new Point(endCol, endRow);
        frontier.add(start);
        cameFrom.put(start, null);
        boolean found = false;
        while(!frontier.isEmpty()) {
            Point current = frontier.poll();
            if(current.equals(goal)) { found = true; break; }
            Point[] neighbors = {
                new Point(current.x, current.y - 1), new Point(current.x, current.y + 1),
                new Point(current.x - 1, current.y), new Point(current.x + 1, current.y),
                new Point(current.x - 1, current.y - 1), new Point(current.x + 1, current.y - 1),
                new Point(current.x - 1, current.y + 1), new Point(current.x + 1, current.y + 1)
            };
            for(Point next : neighbors) {
                if(!cameFrom.containsKey(next) && !isSolidGrid(next.x, next.y)) {
                    boolean isDiagonal = (next.x != current.x) && (next.y != current.y);
                    if(isDiagonal) {
                        if(isSolidGrid(current.x, next.y) || isSolidGrid(next.x, current.y)) continue;
                    }
                    frontier.add(next);
                    cameFrom.put(next, current);
                }
            }
        }
        if(found) {
            ArrayList<Point> path = new ArrayList<>();
            Point current = goal;
            while(!current.equals(start)) {
                int worldX = current.x * GRID_SIZE + (GRID_SIZE - link.getW())/2;
                int worldY = current.y * GRID_SIZE + (GRID_SIZE - link.getH())/2;
                path.add(new Point(worldX, worldY));
                current = cameFrom.get(current);
            }
            Collections.reverse(path);
            // Add smoothing back
            ArrayList<Point> smoothPath = smoothPath(path);
            smoothPath.add(new Point(targetX - link.getW()/2, targetY - link.getH()/2));
            link.setPath(smoothPath);
        }
    }
    
    // Restored Smoothing Logic
    private ArrayList<Point> smoothPath(ArrayList<Point> path) {
        if (path.size() < 2) return path;
        ArrayList<Point> smoothed = new ArrayList<>();
        smoothed.add(path.get(0));
        Point current = path.get(0);
        int index = 0;
        while(index < path.size() - 1) {
            int checkIndex = path.size() - 1;
            while(checkIndex > index + 1) {
                if(hasLineOfSight(current, path.get(checkIndex))) break; 
                checkIndex--;
            }
            current = path.get(checkIndex);
            smoothed.add(current);
            index = checkIndex;
        }
        return smoothed;
    }

    private boolean hasLineOfSight(Point p1, Point p2) {
        double dist = p1.distance(p2);
        double step = 10; 
        double dx = (p2.x - p1.x) / dist;
        double dy = (p2.y - p1.y) / dist;
        for(double i = 0; i < dist; i += step) {
            int checkX = (int)(p1.x + dx * i);
            int checkY = (int)(p1.y + dy * i);
            if(wouldLinkCollide(checkX, checkY)) return false;
        }
        return true;
    }

    private boolean wouldLinkCollide(int x, int y) {
        int linkW = link.getW();
        int linkH = link.getH();
        for(Sprite s : sprites) {
            if(s == link) continue; 
            if(s instanceof TreasureChests) continue;
            if(s.hasTag("solid")) {
                if(x < s.getX() + s.getW() && x + linkW > s.getX() && y < s.getY() + s.getH() && y + linkH > s.getY()) return true;
            }
        }
        return false;
    }

    public Json marshal(){
        Json ob = Json.newObject();
        Json list = Json.newList();
        for(Sprite s : this.sprites){
            if(s.hasTag("saveable")){
                list.add(s.marshal());
            }
        }
        ob.add("sprites", list);
        return ob;
    }

    public void unmarshal(Json ob){
        sprites.clear();
        sprites.add(link);
        visitedRooms.clear();
        roomExits.clear();
        Json tempList = ob.get("sprites");
        for(int i = 0; i < tempList.size(); i++){
            sprites.add(loadSprite(tempList.get(i)));
        }
        currentRoomX = 0; currentRoomY = 0;
        saveCurrentRoomState();
    }

    public void addSprites(int x, int y){
        Sprite template = itemsICanAdd.get(itemNum);
        Sprite newSprite = null;
        try {
            Json templateJson = template.marshal();
            if(template.hasTag("grid_snap")) {
                int gridW = template.getW();
                int gridH = template.getH();
                x = (x / gridW) * gridW;
                y = (y / gridH) * gridH;
            }
            Json newArgs = Json.newObject();
            newArgs.add("type", templateJson.getString("type"));
            newArgs.add("x", x);
            newArgs.add("y", y);
            newArgs.add("w", template.getW());
            newArgs.add("h", template.getH());
            newSprite = loadSprite(newArgs);
            boolean overlaps = false;
            for(Sprite s : sprites) {
                if(Sprite.doesCollide(newSprite, s)) { overlaps = true; break; }
            }
            if(!overlaps) sprites.add(newSprite);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void removeSprites(int x, int y){
        Iterator<Sprite> it = sprites.iterator();
        while(it.hasNext()){
            Sprite s = it.next();
            if(s == link) continue; 
            if(x >= s.getX() && x < s.getX() + s.getW() && y >= s.getY() && y < s.getY() + s.getH()){
                Sprite template = itemsICanAdd.get(itemNum);
                if(s.getClass().equals(template.getClass())) {
                    it.remove();
                    break;
                }
            }
        }
    }
    
    public void clearMap(){
        Iterator<Sprite> it = sprites.iterator();
        while(it.hasNext()){
            Sprite s = it.next();
            if(s.hasTag("saveable")){
                it.remove();
            }
        }
    }

    public void spawnBoomerang(){
        int dir = link.getLastDirection(); 
        int speed = 20;
        int dx = 0, dy = 0;
        if(dir == 0) dy = speed; 
        if(dir == 1) dx = -speed; 
        if(dir == 2) dx = speed;  
        if(dir == 3) dy = -speed;
        int sx = link.getX() + link.getW()/2 - 15;
        int sy = link.getY() + link.getH()/2 - 15;
        sprites.add(new Boomerang(sx, sy, dx, dy));
    }

    public void movementDirectionToF(boolean up, boolean down, boolean left, boolean right){
        link.setInputs(up, down, left, right, sprites);
    }
    
    public ArrayList<Sprite> getSprites() { return sprites; }
    public int getMapPosX() { return mapX; }
    public int getMapPosY() { return mapY; }
    public int getRupeeCount() { return rupeeCount; }
    public Sprite getItemIAmAdding() { return itemsICanAdd.get(itemNum); }
    public void cycleAddItem() { itemNum = (itemNum + 1) % itemsICanAdd.size(); }
}