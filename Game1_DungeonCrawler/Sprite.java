import java.awt.Graphics;
import java.util.ArrayList;

public abstract class Sprite {
    
    protected int X, Y, W, H;
    protected ArrayList<String> tags;

    public Sprite(int x, int y, int w, int h){
        this.X = x;
        this.Y = y;
        this.W = w;
        this.H = h;
        this.tags = new ArrayList<>();
        initializeTags();
    }

    public abstract void initializeTags();
    public abstract boolean update();
    public abstract void draw(Graphics g, int mapX, int mapY);
    public abstract Json marshal();

    public void onCollision(Sprite other) {
        // Default: do nothing
    }

    // NEW: Generic Score System
    public int getScore() {
        return 0; 
    }

    public void addTag(String tag) {
        if(!tags.contains(tag)) tags.add(tag);
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public static boolean doesCollide(Sprite a, Sprite b){
        if(a.X >= b.X + b.W) return false;
        if(a.X + a.W <= b.X) return false;
        if(a.Y + a.H <= b.Y) return false;
        if(a.Y >= b.Y + b.H) return false;
        return true;
    }

    public void pushOutOf(Sprite obstacle) {
        int overlapRight = (this.X + this.W) - obstacle.X;
        int overlapLeft = (obstacle.X + obstacle.W) - this.X;
        int overlapDown = (this.Y + this.H) - obstacle.Y;
        int overlapUp = (obstacle.Y + obstacle.H) - this.Y;

        int minOverlap = Math.min(Math.min(overlapRight, overlapLeft), Math.min(overlapDown, overlapUp));

        if(minOverlap == overlapRight) this.X -= overlapRight;
        else if(minOverlap == overlapLeft) this.X += overlapLeft;
        else if(minOverlap == overlapDown) this.Y -= overlapDown;
        else if(minOverlap == overlapUp) this.Y += overlapUp;
    }

    public int getX() { return X; }
    public int getY() { return Y; }
    public int getW() { return W; }
    public int getH() { return H; }
    public void setPosition(int x, int y) { this.X = x; this.Y = y; }
}