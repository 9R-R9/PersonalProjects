import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Link extends Sprite{

	private static final int LINK_WIDTH = 40;
	private static final int LINK_HEIGHT = 55;
	//zoomin
	private double speed = 10;
	private int thisFrame = 1;
	//0 = up, 1 = down, 2 = left, 3 = right
	private int direction = 0;
	private int lastDirection;
	private boolean isMovingTorF = false;
	private static Image[] frames = null;
    private ArrayList<Sprite> sprites;
    private boolean Up; 
    private boolean Down;
    private boolean Left;
    private boolean Right;
    private int lastX;
    private int lastY;


	public Link(int x, int y){
		
		super(x, y, LINK_WIDTH, LINK_HEIGHT);

		if(frames == null){
            loadFrames();
        }
	}
	
    @Override
	public boolean update(){
		if(isMovingTorF){
            int firstFrame = direction * 11;
            int lastFrame = firstFrame + 10;

			if(thisFrame < firstFrame || thisFrame > lastFrame){
                thisFrame = firstFrame;
            }else{
                thisFrame++;
                if(thisFrame > lastFrame){
					thisFrame = firstFrame;
				}
            }
        }else{
            thisFrame = lastDirection * 11;
        }

        int directionX = 0;
        int directionY = 0;

        if(Up){
            directionY -= 1;
        }
        if(Down){
            directionY += 1;
        }
        if(Left){
            directionX -= 1;
        }
        if(Right){
            directionX += 1;
        }

        //saved pos
        lastX = this.getX();
        lastY = this.getY();

        if(directionX == 0 && directionY == 0){
            this.isLinkMoving(false);
        }else{
            this.isLinkMoving(true);
        }

        moveLink(directionX, 0);
        moveLink(0, directionY);

        return true;
	}

    public void setInputs(boolean up, boolean down, boolean left, boolean right, ArrayList<Sprite> sprites){
        this.Up = up;
        this.Down = down;
        this.Left = left;
        this.Right = right;
        this.sprites = sprites;
    }

	private static void loadFrames(){
        //4 directions * 11 frames per direction
		frames = new Image[44];
        try{
            for(int i = 0; i < frames.length; i++){
                File file = new File("images/link" + (i + 1) + ".png");
                frames[i] = ImageIO.read(file);
                System.out.println("Loaded: " + file.getAbsolutePath() + " exists " + file.exists());
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public Json marshal(){
        Json ob = Json.newObject();
        ob.add("x", this.X);
        ob.add("y", this.Y);
        ob.add("w", this.W);
        ob.add("h", this.H);
        return ob;
    }

    @Override
	public void draw(Graphics g, int mapX, int mapY){
        g.drawImage(frames[thisFrame], X - mapX, Y - mapY, LINK_WIDTH, LINK_HEIGHT, null);
    }

    @Override
    public boolean isLink(){return true;}
    @Override
    public boolean saved(){return false;}

	public void setPosition(int x, int y){
		this.X = x;
		this.Y = y;
	}

	public void moveLink(int directionX, int directionY){
        if(directionX == 0 && directionY == 0){
            isMovingTorF = false;
            return;
        }
        isMovingTorF = true;

		//making it so diagonal movement isn't faster than cardinal direction
        if(directionX != 0 && directionY != 0){
            double inv = 1.0 / Math.sqrt(2);
            X += (int) Math.round(directionX * speed * inv);
            Y += (int) Math.round(directionY * speed * inv);
        }else{
            X += directionX * speed;
            Y += directionY * speed;
        }

		//up
        if(directionY < 0){
			direction = 3;
		}
		//down
        if(directionY > 0){
			direction = 0;
		}
		//left
        if(directionX < 0){
			direction = 1;
		}
		//right
        if(directionX > 0){
			direction = 2;
		}

        lastDirection = direction;
    }
    
    public int getLastDirection(){
        return this.lastDirection;
    }

    public void isLinkMoving(boolean movingToF){
        this.isMovingTorF = movingToF;
    }

	@Override 
    public String toString(){
        return "Link (x,y) = (" + X + ", " + Y + "), w = " + W + ", h = " + H;
    }
}
