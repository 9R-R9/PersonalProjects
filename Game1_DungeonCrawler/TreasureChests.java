import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TreasureChests extends Sprite{
    
    private static final int CHEST_WIDTH = 40;
    private static final int CHEST_HEIGHT = 45;
    private static final int RUPEE_WIDTH = 30;
    private static final int RUPEE_HEIGHT = 45;

    private static BufferedImage chestImage = null;
    private static BufferedImage rupeeImage = null;

    private boolean isOpen = false;

    private int time = 0;

    private static final int RUPEE_UNTOUCHABLE_TIME = 10;
    private static final int RUPEE_ALIVE_TIME = 100;

    private int openingTimer = 0;
    private int lifespanTimer = 0;


    public TreasureChests(int x, int y){

        super(x, y, CHEST_WIDTH, CHEST_HEIGHT);
        
        loadImages();
	}

    @Override
    public boolean update(){
        //only do timer logic if the chest is open
        if(isOpen){
            if(openingTimer > 0){
                openingTimer--;
            }
            else if(lifespanTimer > 0){
                lifespanTimer--;
            }
            if(openingTimer <= 0 && lifespanTimer <= 0){
                return false;
            }
        }
        
        return true; 
    }

    @Override
    public void draw(Graphics g, int mapX, int mapY){
        if(!isOpen){
            //draw as a chest
            g.drawImage(chestImage, X - mapX, Y - mapY, W, H, null);
        }else{
            //draw as a rupee
            g.drawImage(rupeeImage, X - mapX, Y - mapY, W, H, null);
        }
    }

    @Override
    public boolean isObstacle(){return true;}
    @Override
    public boolean isChest(){return true;}
    @Override
    public boolean saved(){return true;}

    private void loadImages(){
        if(chestImage == null){
            try{
                chestImage = ImageIO.read(new File("images/treasurechest.png"));
            } catch (Exception e){ e.printStackTrace(); }
        }
        if(rupeeImage == null){
            try{
                rupeeImage = ImageIO.read(new File("images/rupee.png"));
            } catch (Exception e){ e.printStackTrace(); }
        }
    }

	public TreasureChests(Json ob){
        super((int) ob.getLong("x"), (int) ob.getLong("y"), (int) ob.getLong("w"), (int) ob.getLong("h"));
        loadImages();
    }
    
    public void interact(Sprite interactor){
        //only link or a boomerang can trigger interactions
        if(!interactor.isLink() && !interactor.isBoomerang()){
            return;
        }
        //open chest
        if(!isOpen){
            isOpen = true;
            openingTimer = RUPEE_UNTOUCHABLE_TIME;
            lifespanTimer = RUPEE_ALIVE_TIME;
            //change size to rupee
            this.X += (CHEST_WIDTH - RUPEE_WIDTH) / 2;
            this.Y -= (RUPEE_HEIGHT - CHEST_HEIGHT);
            this.W = RUPEE_WIDTH;
            this.H = RUPEE_HEIGHT;
        }
        //collect the rupee (if not invincible)
        else if(isOpen && openingTimer <= 0){
            //remove instantly
            lifespanTimer = 0;
        }
    }

    public static BufferedImage getChestImage(){
        if (chestImage == null){
            try{
                chestImage = ImageIO.read(new File("images/treasurechest.png"));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return chestImage;
    }

    public int getOpeningTimer(){
        return openingTimer;
    }

    public boolean getIsOpen(){
        return isOpen;
    }

    
    @Override
    public Json marshal(){
        Json ob = Json.newObject();
        ob.add("type", "chest");
        ob.add("x", this.X);
        ob.add("y", this.Y);
        ob.add("w", this.W);
        ob.add("h", this.H);
        return ob;
    }

    @Override 
    public String toString(){
        return "TreasureChest (x,y) = (" + X + ", " + Y + "), w = " + W + ", h = " + H + ", isOpen = " + isOpen;
    }

}
