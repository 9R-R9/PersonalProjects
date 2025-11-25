import java.awt.Graphics;

public abstract class Sprite{
    
    protected int X, Y, W, H;

    public Sprite(int x, int y, int w, int h){
        this.X = x;
        this.Y = y;
        this.W = w;
        this.H = h;
    }

    public abstract boolean update();

    public abstract void draw(Graphics g, int mapX, int mapY);

    public abstract Json marshal();

    //general purpose collision detection between 2 sprites
    public static boolean doesCollide(Sprite a, Sprite b){
        //check if they are not overlapping
        //right
        if(a.X >= b.X + b.W){
            return false;
        }
        //left
        if(a.X + a.W <= b.X){
            return false;
        }
        //up
        if(a.Y + a.H <= b.Y){
            return false;
        }
        //down
        if(a.Y >= b.Y + b.H){
            return false;
        }
        //if all of those fail then its overallped
        return true;
    }

    public boolean isObstacle(){return false;}
    public boolean isTree(){return false;}
    public boolean isLink(){return false;}
    public boolean isChest(){return false;}
    public boolean isBoomerang(){return false;}
    public boolean saved(){return false;}
    

    //getters
    public int getX(){
        return this.X;
    }
    public int getY(){
        return this.Y;
    }
    public int getW(){
        return this.W;
    }
    public int getH(){
        return this.H;
    }

    //setter
    public void setPosition(int x, int y){
        this.X = x;
        this.Y = y;
    }
}
