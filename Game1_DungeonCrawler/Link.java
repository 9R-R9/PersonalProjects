/*
Name: Ridoy Roy
Date: 10/17/2025
Description: Link with Smoothed Path Following.
*/

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point; 
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Link extends Sprite {
    private static final int LINK_WIDTH = 40;
    private static final int LINK_HEIGHT = 55;
    private double speed = 10;
    private int direction = 0; 
    private int lastDirection = 0;
    private boolean isMoving = false;
    private static Image[] frames = null;
    
    private boolean up, down, left, right;
    
    // PATHFINDING STATE
    private ArrayList<Point> currentPath;
    
    // DRAGGING STATE
    private int targetX, targetY;
    private boolean hasTarget = false;

    public Link(int x, int y){
        super(x, y, LINK_WIDTH, LINK_HEIGHT);
        if(frames == null) loadFrames();
        currentPath = new ArrayList<>();
    }
    
    @Override
    public void initializeTags() {
        tags.add("player");
    }

    // --- NEW: Pathfinding Methods ---
    
    public void setPath(ArrayList<Point> path) {
        this.currentPath = path;
        this.hasTarget = false;
    }
    
    public void setDestination(int x, int y) {
        this.targetX = x;
        this.targetY = y;
        this.hasTarget = true;
        this.currentPath.clear();
    }

    @Override
    public boolean update() {
        // 1. Keyboard Override
        int dx = 0; int dy = 0;
        if(up) dy--;
        if(down) dy++;
        if(left) dx--;
        if(right) dx++;

        if(dx != 0 || dy != 0) {
            currentPath.clear();
            hasTarget = false;
        }

        isMoving = false;

        // 2. Execute Movement Logic
        if(dx != 0 || dy != 0) {
            isMoving = true;
            double moveSpeed = speed;
            if(dx != 0 && dy != 0) moveSpeed = speed / Math.sqrt(2);
            
            X += (int)(dx * moveSpeed);
            Y += (int)(dy * moveSpeed);
            
            updateDirection(dx, dy);
        } else if(!currentPath.isEmpty()) {
            followPath();
        } else if(hasTarget) {
            moveToTarget();
        }
        
        return true;
    }

    private void followPath() {
        if(currentPath.isEmpty()) return;

        double remainingSpeed = speed;
        
        // Loop to consume speed across multiple nodes if needed
        while(remainingSpeed > 0 && !currentPath.isEmpty()) {
            Point nextTarget = currentPath.get(0);
            double diffX = nextTarget.x - X;
            double diffY = nextTarget.y - Y;
            double dist = Math.sqrt(diffX*diffX + diffY*diffY);

            if(dist <= remainingSpeed) {
                // Arrive at node and continue
                X = nextTarget.x;
                Y = nextTarget.y;
                remainingSpeed -= dist;
                currentPath.remove(0);
            } else {
                // Move partial distance
                isMoving = true;
                double ratio = remainingSpeed / dist;
                double moveX = diffX * ratio;
                double moveY = diffY * ratio;
                
                X += (int)moveX;
                Y += (int)moveY;
                
                updateDirection((int)moveX, (int)moveY);
                remainingSpeed = 0;
            }
        }
    }
    
    private void moveToTarget() {
        double diffX = targetX - X;
        double diffY = targetY - Y;
        double dist = Math.sqrt(diffX*diffX + diffY*diffY);

        if(dist < speed) {
            X = targetX;
            Y = targetY;
            hasTarget = false;
            isMoving = false;
        } else {
            isMoving = true;
            double moveX = (diffX / dist) * speed;
            double moveY = (diffY / dist) * speed;
            
            X += (int)moveX;
            Y += (int)moveY;
            
            updateDirection((int)moveX, (int)moveY);
        }
    }

    private void updateDirection(int dx, int dy) {
        if(Math.abs(dy) >= Math.abs(dx)) {
            if(dy > 0) direction = 0; 
            else if(dy < 0) direction = 3; 
        } else {
            if(dx < 0) direction = 1; 
            else if(dx > 0) direction = 2; 
        }
        lastDirection = direction;
    }

    @Override
    public void onCollision(Sprite other) {
        if(other.hasTag("solid")) {
            pushOutOf(other);
            // REMOVED: Stopping logic. 
            // This allows Link to "slide" along walls or recover from corner clips.
        }
    }

    public void setInputs(boolean u, boolean d, boolean l, boolean r, ArrayList<Sprite> s){
        this.up = u; this.down = d; this.left = l; this.right = r;
    }

    public int getLastDirection() { return lastDirection; }

    @Override
    public void draw(Graphics g, int mapX, int mapY) {
        int frameIndex = lastDirection * 11;
        if(isMoving) {
            int cycle = (int)(System.currentTimeMillis() / 50) % 11;
            frameIndex += cycle;
        }
        g.drawImage(frames[frameIndex], X - mapX, Y - mapY, W, H, null);
    }

    @Override
    public Json marshal() {
        Json ob = Json.newObject();
        ob.add("type", "Link"); 
        ob.add("x", X); ob.add("y", Y);
        return ob;
    }

    private static void loadFrames(){
        frames = new Image[44];
        try{
            for(int i = 0; i < frames.length; i++){
                frames[i] = ImageIO.read(new File("images/link" + (i + 1) + ".png"));
            }
        } catch(IOException e) { e.printStackTrace(); }
    }
}