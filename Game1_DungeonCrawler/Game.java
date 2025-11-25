import java.awt.Toolkit;
import javax.swing.JFrame;

public class Game extends JFrame{
	private boolean keepGoing;
	private Model model;
	private Controller controller;
	private View view;

	public Game(){
		keepGoing = true;
		model = new Model();

		try{
			Json obLoad = Json.load("map.json");
			model.unmarshal(obLoad);
			System.out.println("Map loaded successfully!");
		} catch (Exception e){
			System.out.println("No map.json found, starting fresh.");
		}

		controller = new Controller(model);
		view = new View(controller, model);

		this.setTitle("A4: Rupee Adventure");
		this.setSize(1000, 500);
		this.setFocusable(true);
		this.getContentPane().add(view);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.addKeyListener(controller);
		view.addMouseListener(controller);
		
	}

	public void run(){
		do{
			keepGoing = controller.update();
			model.update(view.getWidth(), view.getHeight());
			view.repaint(); // This will indirectly call View.paintComponent
			Toolkit.getDefaultToolkit().sync(); // Updates screen

			// Go to sleep for 50 milliseconds
			try{
				Thread.sleep(50);
			} catch(Exception e){
				e.printStackTrace();
				System.exit(1);
			}
		}
		while(keepGoing);
	}

	public static void main(String[] args){
		Game g = new Game();
		g.run();
	}
}
