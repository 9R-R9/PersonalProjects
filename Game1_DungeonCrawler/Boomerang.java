/*
Name: Ridoy Roy
Date: 10/17/2025
Description: Boomerang with fixed animation.
*/
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Boomerang extends Sprite {
    private int dx, dy;
    private int life = 100;
    
    private static BufferedImage[] frames = null;
    private int currentFrame = 0;
    private int animationTimer = 0;

    public Boomerang(int x, int y, int dx, int dy) {
        super(x, y, 30, 30);
        this.dx = dx; this.dy = dy;
        if(frames == null) loadImages();
    }

    public Boomerang(Json ob) {
        super(0,0,0,0);
    }

    @Override
    public void initializeTags() {
        addTag("projectile");
    }

    @Override
    public boolean update() {
        X += dx;
        Y += dy;
        life--;

        // Animation: Change frame every 2 updates
        animationTimer++;
        if(animationTimer >= 2) { 
            currentFrame++;
            if(currentFrame >= 4) currentFrame = 0;
            animationTimer = 0;
        }

        return life > 0;
    }

    @Override
    public void onCollision(Sprite other) {
        if(other.hasTag("solid")) {
            life = 0; // Break on walls
        }
        if(other instanceof TreasureChests) {
            life = 0; // Break on chests (allows chest to detect hit)
        }
    }

    @Override
    public void draw(Graphics g, int mapX, int mapY) {
        if(frames != null && frames[currentFrame] != null) 
            g.drawImage(frames[currentFrame], X - mapX, Y - mapY, W, H, null);
    }

    @Override
    public Json marshal() { return null; }

    private void loadImages() {
        frames = new BufferedImage[4];
        try {
            // Assumes images are boomerang1.png ... boomerang4.png
            for(int i = 0; i < 4; i++) {
                frames[i] = ImageIO.read(new File("images/boomerang" + (i + 1) + ".png"));
            }
        } catch(Exception e) { 
            e.printStackTrace(); 
        }
    }
}