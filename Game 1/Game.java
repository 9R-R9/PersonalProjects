import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class Game extends JPanel implements KeyListener {

    // --- Settings ---
    private final int TILE_SIZE = 20;
    private final int COLS = 40;
    private final int ROWS = 30;
    private final int WIDTH = COLS * TILE_SIZE;
    private final int HEIGHT = ROWS * TILE_SIZE;

    // --- Tile Types ---
    private final int TILE_WALL = 0;
    private final int TILE_FLOOR = 1;
    private final int TILE_GOAL = 2;

    // --- Game State ---
    private int[][] map; // The grid: map[y][x]
    private int playerX, playerY; // Player grid coordinates
    private boolean gameWon = false;

    public Game() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        generateLevel();
    }

    // --- Generation Algorithm: Random Walker ---
    private void generateLevel() {
        map = new int[ROWS][COLS];
        Random rand = new Random();

        // 1. Fill with Walls
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                map[y][x] = TILE_WALL;
            }
        }

        // 2. Start Walker in the middle
        int walkerX = COLS / 2;
        int walkerY = ROWS / 2;
        
        // Player starts where the walker starts
        playerX = walkerX;
        playerY = walkerY;
        
        // 3. Walk and Carve
        int maxFloors = 400; // Target number of floor tiles
        int floorCount = 0;

        while (floorCount < maxFloors) {
            // Carve the current spot
            if (map[walkerY][walkerX] == TILE_WALL) {
                map[walkerY][walkerX] = TILE_FLOOR;
                floorCount++;
            }

            // Move Randomly
            int direction = rand.nextInt(4); // 0:Up, 1:Down, 2:Left, 3:Right
            
            if (direction == 0 && walkerY > 1) walkerY--;
            else if (direction == 1 && walkerY < ROWS - 2) walkerY++;
            else if (direction == 2 && walkerX > 1) walkerX--;
            else if (direction == 3 && walkerX < COLS - 2) walkerX++;
        }

        // 4. Place Goal (Just put it at the walker's last position)
        map[walkerY][walkerX] = TILE_GOAL;
        gameWon = false;
        repaint();
    }

    // --- Rendering ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw Map
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (map[y][x] == TILE_WALL) {
                    g.setColor(Color.DARK_GRAY);
                } else if (map[y][x] == TILE_FLOOR) {
                    g.setColor(Color.LIGHT_GRAY);
                } else if (map[y][x] == TILE_GOAL) {
                    g.setColor(Color.YELLOW);
                }
                g.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                
                // Optional: Draw grid lines for clarity
                g.setColor(new Color(0,0,0, 50));
                g.drawRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw Player
        g.setColor(Color.BLUE);
        // +2 for a little padding so it looks like it's inside the tile
        g.fillRect(playerX * TILE_SIZE + 2, playerY * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);

        // UI
        if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("YOU ESCAPED!", WIDTH / 2 - 150, HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press SPACE to Generate New Level", WIDTH / 2 - 160, HEIGHT / 2 + 40);
        }
    }

    // --- Input Handling ---
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (gameWon && key == KeyEvent.VK_SPACE) {
            generateLevel();
            return;
        }

        if (gameWon) return;

        int nextX = playerX;
        int nextY = playerY;

        if (key == KeyEvent.VK_UP) nextY--;
        if (key == KeyEvent.VK_DOWN) nextY++;
        if (key == KeyEvent.VK_LEFT) nextX--;
        if (key == KeyEvent.VK_RIGHT) nextX++;

        // Collision Check: Only move if the target tile is NOT a wall
        if (map[nextY][nextX] != TILE_WALL) {
            playerX = nextX;
            playerY = nextY;
            
            // Check Win Condition
            if (map[playerY][playerX] == TILE_GOAL) {
                gameWon = true;
            }
            
            repaint();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    // --- Main Entry Point ---
    public static void main(String[] args) {
        JFrame frame = new JFrame("Game 2: Procedural Dungeon");
        Game game = new Game();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}