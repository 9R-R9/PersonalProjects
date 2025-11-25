/*
Name: Ridoy Roy
Date: 10/17/2025
Description: Chest logic - Solid Rupee during delay.
*/
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TreasureChests extends Sprite {
    private static BufferedImage chestImg = null;
    private static BufferedImage rupeeImg = null;
    private boolean isOpen = false;
    private int stateTimer = 0;
    
    // Collection Logic
    private int collectionDelay = 0; 
    private int pointValue = 0;

    public TreasureChests(int x, int y){
        super(x, y, 40, 45); // Chest size
        loadImages();
    }

    public TreasureChests(Json ob){
        super((int)ob.getLong("x"), (int)ob.getLong("y"), 40, 45);
        loadImages();
    }

    @Override
    public void initializeTags() {
        addTag("solid");
        addTag("saveable");
    }

    @Override
    public void onCollision(Sprite other) {
        if(other.hasTag("player") || other.hasTag("projectile")) {
            if(!isOpen) {
                // Phase 1: OPENING
                isOpen = true;
                
                // FIX 1: Do NOT remove "solid" tag yet! 
                // We want Link to bump into the rupee while it's invincible.
                // tags.remove("solid"); 

                // FIX 2: Resize hitbox to match Rupee (thinner than chest)
                // Chest is 40 wide, Rupee is 30 wide. Shift X by 5 to center it.
                this.X += 5; 
                this.W = 30; 
                
                stateTimer = 100; // Lifespan
                collectionDelay = 20; // 1 second invincibility
                System.out.println("Chest Opened! Rupee is solid and invincible.");
            } else {
                // Phase 2: COLLECTING
                if(pointValue == 0 && collectionDelay <= 0) { 
                    pointValue = 1;
                    stateTimer = 0; // Remove instantly
                    System.out.println("Rupee Collected!");
                }
            }
        }
    }
    
    @Override
    public int getScore() {
        int score = pointValue;
        pointValue = 0; 
        return score;
    }

    @Override
    public boolean update() {
        if(isOpen) {
            stateTimer--;
            if(collectionDelay > 0) collectionDelay--;
            if(stateTimer <= 0) return false; 
        }
        return true;
    }

    @Override
    public void draw(Graphics g, int mapX, int mapY) {
        if(!isOpen) {
            g.drawImage(chestImg, X - mapX, Y - mapY, W, H, null);
        } else {
            // Draw using current W and H (which we resized to 30x45)
            g.drawImage(rupeeImg, X - mapX, Y - mapY, W, H, null);
        }
    }

    @Override
    public Json marshal() {
        Json ob = Json.newObject();
        ob.add("type", "chest");
        ob.add("x", X); ob.add("y", Y);
        return ob;
    }

    private void loadImages(){
        try {
            if(chestImg == null) chestImg = ImageIO.read(new File("images/treasurechest.png"));
            if(rupeeImg == null) rupeeImg = ImageIO.read(new File("images/rupee.png"));
        } catch(Exception e) { e.printStackTrace(); }
    }
}