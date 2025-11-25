/*
Name: Ridoy Roy
Date: 10/17/2025
Description: Generic View - Fixes "isTree" errors.
*/

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
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
		
        // Draw Background
		g.setColor(new Color(95, 155, 100));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
        // Draw All Sprites
		for(Sprite sprite : model.getSprites()){
			sprite.draw(g, mapX, mapY);
		}

        // Draw HUD
        int rupeeCount = model.getRupeeCount();
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Rupees: " + rupeeCount, this.getWidth() - 150, 30);

        // Draw Editor UI
		if(Controller.editModeTorF()){
			int boxX = 20, boxY = 20, boxW = 80, boxH = 80;
			g.setColor(Controller.addModeTorF() ? Color.GREEN : Color.RED);
            g.drawRect(boxX, boxY, boxW, boxH);
            g.setColor(new Color(0,0,0, 100));
            g.fillRect(boxX, boxY, boxW, boxH);
			
			// Generic Editor Drawing
            Sprite itemToDraw = model.getItemIAmAdding();

            if(itemToDraw != null){
                // Save original position
                int originalX = itemToDraw.getX();
                int originalY = itemToDraw.getY();
                
                // Move template to UI box
                int centerX = boxX + (boxW - itemToDraw.getW()) / 2;
                int centerY = boxY + (boxH - itemToDraw.getH()) / 2;
                itemToDraw.setPosition(centerX, centerY);
                
                // Draw template
                itemToDraw.draw(g, 0, 0);
                
                // Restore template
                itemToDraw.setPosition(originalX, originalY);
            }
            g.setColor(Color.WHITE);
            g.drawString("Edit Mode", boxX, boxY - 5);
        }
	}
}