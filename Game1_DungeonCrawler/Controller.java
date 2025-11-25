/*
Name: Ridoy Roy
Date: 10/17/2025
Description: Controller handling Pathfinding vs Dragging.
*/

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.SwingUtilities;

public class Controller implements MouseListener, KeyListener, MouseMotionListener
{
	private boolean keepGoing;
	private Model model;
	private View view;

	private static boolean editMode = false;
	private static boolean addMode = true;

	private boolean Up = false;
	private boolean Down = false;
	private boolean Left = false;
	private boolean Right = false;
    
    private boolean queueBoomerang = false;

	private final int EDIT_BOX_X = 20;
	private final int EDIT_BOX_Y = 20;
	private final int EDIT_BOX_W = 80;
	private final int EDIT_BOX_H = 80;
	
	public Controller(Model m){
		model = m;
		keepGoing = true;
	}

	public boolean update(){
		model.movementDirectionToF(Up, Down, Left, Right);
        
        if(queueBoomerang) {
            model.spawnBoomerang();
            queueBoomerang = false;
        }

		return keepGoing;
	}

	public void setView(View v){
		view = v;
	}
	
    private void editMapLocation(int mouseX, int mouseY) {
        if(!editMode) return;

        if(mouseX >= EDIT_BOX_X && mouseX < EDIT_BOX_X + EDIT_BOX_W &&
           mouseY >= EDIT_BOX_Y && mouseY < EDIT_BOX_Y + EDIT_BOX_H) {
            return;
        }

        int mapX = mouseX + model.getMapPosX();
        int mapY = mouseY + model.getMapPosY();

        if(addMode){
            model.addSprites(mapX, mapY);
        } else {
            model.removeSprites(mapX, mapY);
        }
    }

	public void mousePressed(MouseEvent e){
        int mouseX = e.getX();
        int mouseY = e.getY();

        // Right Click: Pathfind (Smart Move)
        if(SwingUtilities.isRightMouseButton(e)) {
            int destX = mouseX + model.getMapPosX();
            int destY = mouseY + model.getMapPosY();
            // Single Click = Pathfinding
            model.pathfindTo(destX, destY);
            return; 
        }

        // Left Click: Edit Mode
        if(editMode){
            if(mouseX >= EDIT_BOX_X && mouseX < EDIT_BOX_X + EDIT_BOX_W &&
               mouseY >= EDIT_BOX_Y && mouseY < EDIT_BOX_Y + EDIT_BOX_H)
            {
                model.cycleAddItem();
            } else {
                editMapLocation(mouseX, mouseY);
            }
        }
	}

    public void mouseDragged(MouseEvent e) {
        // Right Click Drag: Follow Mouse (Direct Move)
        if(SwingUtilities.isRightMouseButton(e)) {
            int destX = e.getX() + model.getMapPosX();
            int destY = e.getY() + model.getMapPosY();
            // Dragging = Direct Following
            model.moveLinkDirectly(destX, destY);
        } 
        // Left Click Drag: Edit Mode
        else if(editMode) {
            editMapLocation(e.getX(), e.getY());
        }
    }

    public void mouseMoved(MouseEvent e) {}

	public void mouseReleased(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseClicked(MouseEvent e){}

	public void keyReleased(KeyEvent e){
		switch(e.getKeyCode()){
			case KeyEvent.VK_Q:
			case KeyEvent.VK_ESCAPE:
				System.exit(0);
				break;
			case KeyEvent.VK_E: 
				editMode = !editMode;
				if(editMode) addMode = true; 
				break;
			case KeyEvent.VK_A: 
				if(editMode) addMode = true;
				break;
			case KeyEvent.VK_R: 
                if(editMode) addMode = false;
				break;
			case KeyEvent.VK_C: 
				if(editMode) model.clearMap();
				break;
			case KeyEvent.VK_S:
				try{
					Json saveFile = model.marshal();
					saveFile.save("map.json");
					System.out.println("Map saved!");
				} catch (Exception z){
					z.printStackTrace();
				}
				break;
			case KeyEvent.VK_L:
				try {
                    Json obLoad = Json.load("map.json");
                    model.unmarshal(obLoad);
                    System.out.println("Map loaded!");
                } catch (Exception ex) {
                    System.out.println("Could not load map.");
                }
				break;
			case KeyEvent.VK_UP:    Up = false; break;
			case KeyEvent.VK_DOWN:  Down = false; break;
			case KeyEvent.VK_LEFT:  Left = false; break;
			case KeyEvent.VK_RIGHT: Right = false; break;
		}
	}

	public void keyTyped(KeyEvent e){}
	
	public void keyPressed(KeyEvent e){
		switch(e.getKeyCode()){
			case KeyEvent.VK_UP:    Up = true; break;
			case KeyEvent.VK_DOWN:  Down = true; break;
			case KeyEvent.VK_LEFT:  Left = true; break;
			case KeyEvent.VK_RIGHT: Right = true; break;
			case KeyEvent.VK_SPACE: queueBoomerang = true; break;
		}
	}

	public static boolean editModeTorF(){ return editMode; }
	public static boolean addModeTorF(){ return addMode; }
}