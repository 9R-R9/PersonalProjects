/*
Name: Ridoy Roy
Date: 10/17/2025
Description: Tree with grid snapping tag.
*/

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Tree extends Sprite {
    public static final int TREE_WIDTH = 50;
    public static final int TREE_HEIGHT = 65;
    private static BufferedImage image = null;

    public Tree(int x, int y){
        super(x, y, TREE_WIDTH, TREE_HEIGHT);
        if(image == null) loadImage();
    }

    public Tree(Json ob){
        super((int)ob.getLong("x"), (int)ob.getLong("y"), TREE_WIDTH, TREE_HEIGHT);
        if(image == null) loadImage();
    }

    @Override
    public void initializeTags() {
        addTag("solid");      
        addTag("saveable");
        
        // NEW: Tells Model to snap this sprite to grid coordinates
        addTag("grid_snap");  
    }

    @Override
    public void onCollision(Sprite other) {
        // Trees are passive
    }

    @Override
    public boolean update() { return true; }

    @Override
    public void draw(Graphics g, int mapX, int mapY) {
        g.drawImage(image, X - mapX, Y - mapY, W, H, null);
    }

    @Override
    public Json marshal() {
        Json ob = Json.newObject();
        ob.add("type", "Tree");
        ob.add("x", X); ob.add("y", Y);
        return ob;
    }

    private static void loadImage(){
        try { image = ImageIO.read(new File("images/tree.png")); } 
        catch (Exception e) { e.printStackTrace(); }
    }
}