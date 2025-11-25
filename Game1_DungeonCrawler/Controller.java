import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Controller implements MouseListener, KeyListener
{
	private boolean keepGoing;

	private Model model;
	private View view;

	private static boolean editMode;
	private static boolean addMode;
	private boolean mapClear;

	private boolean Up = false;
	private boolean Down = false;
	private boolean Left = false;
	private boolean Right = false;
	private final int EDIT_BOX_X = 0;
	private final int EDIT_BOX_Y = 0;
	private final int EDIT_BOX_W = 100;
	private final int EDIT_BOX_H = 100;
	
	public Controller(Model m){
		model = m;
		keepGoing = true;
	}

	public boolean update(){

		model.movementDirectionToF(Up, Down, Left, Right);

		return keepGoing;
	}


	public void setView(View v){
		view = v;
	}
	
	public void mousePressed(MouseEvent e){
		int mapX = e.getX() + model.getMapPosX();
		int mapY = e.getY() + model.getMapPosY();
		// int mapW = 50;
		// int gridH = 65; 
		int mouseX = e.getX();
    	int mouseY = e.getY();

		if(this.editMode == true){
			if(mouseX >= EDIT_BOX_X && mouseX < EDIT_BOX_X + EDIT_BOX_W &&
            mouseY >= EDIT_BOX_Y && mouseY < EDIT_BOX_Y + EDIT_BOX_H)
        	{
				
				model.cycleAddItem();
				System.out.println("Cycled edit item.");
        	}else{
				if(this.addMode == true){
					this.model.addSprites(mapX, mapY);
				}else if(this.addMode == false){
					this.model.removeSprites(mapX, mapY);
				}else{
					return;
				}
			}
		}
	}

	public void mouseReleased(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseClicked(MouseEvent e){}
	public void mouseMoved(MouseEvent e){}

	public void keyReleased(KeyEvent e){
		switch(e.getKeyCode()){
			case KeyEvent.VK_Q:
				System.exit(0);
				break;
			case KeyEvent.VK_ESCAPE:
				System.exit(0);
				break;
			case KeyEvent.VK_E: 
				editMode = !editMode;
				if(this.editMode == true){
					this.addMode = true;
				}
				break;
			case KeyEvent.VK_A: 
				if(this.editMode == true){
					this.addMode = true;
				}
				break;
			case KeyEvent.VK_R: 
				this.addMode = false;
				break;
			case KeyEvent.VK_C: 
				this.mapClear = true;
				if(this.editMode == true){
					this.model.clearMap();
				}
				this.mapClear = false;
				break;
			case KeyEvent.VK_S:
				try{
					Json saveFile = model.marshal();
					System.out.println("Saving this data: " + saveFile.toString());
					saveFile.save("map.json");
					System.out.println("Map saved successfully!");
				} catch (Exception z){
					z.printStackTrace();
				}
				break;
			case KeyEvent.VK_L:
				Json obLoad = Json.load("map.json");
				model.unmarshal(obLoad);
				break;
			case KeyEvent.VK_UP:    
				Up = false;
				break;
			case KeyEvent.VK_DOWN:  
				Down = false;
				break;
			case KeyEvent.VK_LEFT:  
				Left = false;
				break;
			case KeyEvent.VK_RIGHT: 
				Right = false;
				break;
		}
	}

	public void keyTyped(KeyEvent e){}
	
	public void keyPressed(KeyEvent e){
		switch(e.getKeyCode()){
			case KeyEvent.VK_UP:    
				Up = true;
				break;
			case KeyEvent.VK_DOWN:  
				Down = true;
				break;
			case KeyEvent.VK_LEFT:  
				Left = true;
				break;
			case KeyEvent.VK_RIGHT: 
				Right = true;
				break;
			case KeyEvent.VK_SPACE:
                model.spawnBoomerang();
                break;
		}
		
	}

	public static boolean editModeTorF(){
		return editMode;
	}

	public static boolean addModeTorF(){
		return addMode;
	}
}

