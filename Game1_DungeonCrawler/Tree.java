import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Tree extends Sprite{

    public static final int TREE_WIDTH = 50;
    public static final int TREE_HEIGHT = 65;

    private static BufferedImage treeImage = null;

    public Tree(int x, int y){

        super(x, y, TREE_WIDTH, TREE_HEIGHT);
        getTreeImage();
	}

    @Override
    public boolean update(){
        return true;
    }

    @Override
    public void draw(Graphics g, int mapX, int mapY){
        g.drawImage(treeImage, X - mapX, Y - mapY, W, H, null);
    }

    @Override
    public boolean isObstacle(){return true;}
    @Override
    public boolean isTree(){return true;}
    @Override
    public boolean saved(){return true;}

    public static BufferedImage getTreeImage(){
        if (treeImage == null){
            try{
                treeImage = ImageIO.read(new File("images/tree.png"));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return treeImage;
    }
    
	public Tree(Json ob){
        super((int) ob.getLong("x"), (int) ob.getLong("y"), (int) ob.getLong("w"), (int) ob.getLong("h"));
        getTreeImage();
    }

    @Override
    public Json marshal(){
        Json ob = Json.newObject();
        ob.add("type", "tree");
        ob.add("x", this.X);
        ob.add("y", this.Y);
        ob.add("w", this.W);
        ob.add("h", this.H);
        return ob;
    }

    @Override 
    public String toString()
    {
        return "Tree (x,y) = (" + X + ", " + Y + "), w = " + W + ", h = " + H;
    }

}
