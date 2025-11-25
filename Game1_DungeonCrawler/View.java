import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class View extends JPanel{

	private Model model;

	public View(Controller c, Model m){
		c.setView(this);
		this.model = m;
		
	}
	
	@Override
	public void paintComponent(Graphics g){
		int mapX = model.getMapPosX(); 
		int mapY = model.getMapPosY();
		super.paintComponent(g);
		g.setColor(new Color(95, 155, 100));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		for(Sprite sprite : model.getSprites()){
			int spriteX = sprite.getX() - mapX;
			int spriteY = sprite.getY() - mapY;
			int spriteW = sprite.getW();
			int spriteH = sprite.getH();

			if(spriteX + spriteW >= 0 && 
				spriteY + spriteH >= 0 && 
				spriteX <= this.getWidth() && 
				spriteY <= this.getHeight()){
				sprite.draw(g, mapX, mapY);
			}
		}

			int rupeeCount = model.getRupeeCount();
			g.setColor(Color.GREEN);
			g.setFont(new Font("Arial", Font.BOLD, 20));
			g.drawString("Rupees: " + rupeeCount, this.getWidth() - 100, 20);

		if(Controller.editModeTorF()){
			int boxX = 0, boxY = 0, boxW = 100, boxH = 100;
			//if addmode true: green, if false: red, shoulda used this before its so much easier
			g.setColor(Controller.addModeTorF() ? Color.GREEN : Color.RED);
            g.fillRect(boxX, boxY, boxW, boxH);
			
			//get type of item
            Sprite itemToDraw = model.getItemIAmAdding();

            if(itemToDraw != null){
                BufferedImage itemImage = null;
                //check type
                if(itemToDraw.isTree()){
                    itemImage = Tree.getTreeImage();
                }else if(itemToDraw.isChest()){
                    itemImage = TreasureChests.getChestImage();
                }
                //draw inside box
                if(itemImage != null){
                    //size of image
                    int imageW = 75;
                    int imageH = 75;
                    int imageX = boxX + (boxW - imageW) / 2; 
                    int imageY = boxY + (boxH - imageH) / 2; 
                    g.drawImage(itemImage, imageX, imageY, imageW, imageH, null);
                }
			}
        }
	}
}
