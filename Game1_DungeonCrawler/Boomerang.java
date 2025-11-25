import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Boomerang extends Sprite {

    public static final int BOOMERANG_WIDTH = 30;
    public static final int BOOMERANG_HEIGHT = 30;

    private static BufferedImage[] boomerangFrames = null;
    private int currentFrameIndex = 0;
    private static final int NUM_BOOMERANG_FRAMES = 5;
    private static final int ANIMATION_SPEED = 2; 
    private int animationTimer = 0;

    private int dx, dy;
    private int lifespan = 50;

    public Boomerang(int x, int y, int dx, int dy) {
        super(x, y, BOOMERANG_WIDTH, BOOMERANG_HEIGHT);
        this.dx = dx;
        this.dy = dy;
        
        //load all animation frames if they havent been loaded
        if(boomerangFrames == null) {
            loadBoomerangFrames();
        }
    }

    private void loadBoomerangFrames() {
        boomerangFrames = new BufferedImage[NUM_BOOMERANG_FRAMES];
        boomerangFrames[1] = null;
        for(int i = 1; i < NUM_BOOMERANG_FRAMES; i++) {
            try {
                boomerangFrames[i] = ImageIO.read(new File("images/boomerang" + i + ".png"));
            } catch (Exception e) { 
                e.printStackTrace(); 
                System.err.println("Failed to load boomerang image: images/boomerang" + i + ".png");
            }
        }
    }

    @Override
    public boolean update() {
        
        X += dx;
        Y += dy;

        //countdown
        lifespan--;
        
        //animation
        animationTimer++;
        if(animationTimer >= ANIMATION_SPEED) {
            currentFrameIndex = (currentFrameIndex + 1) % NUM_BOOMERANG_FRAMES;
            animationTimer = 0;
        }

        //return false when it dies
        return (lifespan > 0); 
    }

    @Override
    public void draw(Graphics g, int mapX, int mapY){
        if(boomerangFrames != null && boomerangFrames[currentFrameIndex] != null) {
            g.drawImage(boomerangFrames[currentFrameIndex], X - mapX, Y - mapY, W, H, null);
        }
    }

    public void disappear(){
        this.lifespan = 0;
    }

    @Override
    public boolean isBoomerang() { 
        return true; 
    }

    @Override
    public boolean saved() { 
        return false; 
    }

    @Override
    public Json marshal(){ 
        return null;
    }

    @Override 
    public String toString()
    {
        return "Boomerang (x,y) = (" + X + ", " + Y + "), w = " + W + ", h = " + H;
    }
}