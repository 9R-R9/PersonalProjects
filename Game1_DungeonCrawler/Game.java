import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import javax.swing.*;

// Added MouseListener (for clicking) and ActionListener (for the movement timer)
public class Game extends JPanel implements KeyListener, MouseListener, ActionListener {

    // --- Settings ---
    private final int TILE_SIZE = 20;
    private final int COLS = 40;
    private final int ROWS = 30;
    private final int WIDTH = COLS * TILE_SIZE;
    private final int HEIGHT = ROWS * TILE_SIZE;
    private final int MOVEMENT_DELAY = 100; // Time in ms between steps (automatic walking)

    // --- Tile Types ---
    private final int TILE_WALL = 0;
    private final int TILE_FLOOR = 1;
    private final int TILE_GOAL = 2;

    // --- Game State ---
    private int[][] map; // The grid: map[y][x]
    private int playerX, playerY; // Player grid coordinates
    private boolean gameWon = false;
    
    // New variables for Mouse Movement
    private Timer moveTimer;
    private List<Point> currentPath;

    public Game() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this); // Listen for mouse clicks

        generateLevel();
        
        // Initialize pathfinding tools
        currentPath = new ArrayList<>();
        moveTimer = new Timer(MOVEMENT_DELAY, this);
        moveTimer.start();
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
        
        // Clear old paths when regenerating
        if (currentPath != null) currentPath.clear();
        
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

        // Optional: Draw the path if one exists
        if (currentPath != null && !currentPath.isEmpty()) {
            g.setColor(new Color(0, 255, 255, 100)); // Semi-transparent Cyan
            for (Point p : currentPath) {
                g.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
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
        // Interrupt auto-movement if user presses a key
        if (!currentPath.isEmpty()) currentPath.clear();

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

    // --- Mouse Handling & Pathfinding ---

    @Override
    public void mousePressed(MouseEvent e) {
        // Only react to Right Click
        if (SwingUtilities.isRightMouseButton(e) && !gameWon) {
            int targetX = e.getX() / TILE_SIZE;
            int targetY = e.getY() / TILE_SIZE;

            // Ensure click is within bounds and not on a wall
            if (targetX >= 0 && targetX < COLS && targetY >= 0 && targetY < ROWS) {
                if (map[targetY][targetX] != TILE_WALL) {
                    calculatePath(targetX, targetY);
                    repaint();
                }
            }
        }
    }

    // BFS Algorithm to find shortest path
    private void calculatePath(int targetX, int targetY) {
        Queue<Point> frontier = new LinkedList<>();
        HashMap<Point, Point> cameFrom = new HashMap<>();
        
        Point start = new Point(playerX, playerY);
        Point goal = new Point(targetX, targetY);

        frontier.add(start);
        cameFrom.put(start, null);

        boolean found = false;

        while (!frontier.isEmpty()) {
            Point current = frontier.poll();

            if (current.equals(goal)) {
                found = true;
                break;
            }

            // Check neighbors (Up, Down, Left, Right)
            Point[] neighbors = {
                new Point(current.x, current.y - 1),
                new Point(current.x, current.y + 1),
                new Point(current.x - 1, current.y),
                new Point(current.x + 1, current.y)
            };

            for (Point next : neighbors) {
                // Bounds check
                if (next.x >= 0 && next.x < COLS && next.y >= 0 && next.y < ROWS) {
                    // Check if floor and not visited
                    if (map[next.y][next.x] != TILE_WALL && !cameFrom.containsKey(next)) {
                        frontier.add(next);
                        cameFrom.put(next, current);
                    }
                }
            }
        }

        if (found) {
            // Reconstruct path
            currentPath.clear();
            Point current = goal;
            while (!current.equals(start)) {
                currentPath.add(current);
                current = cameFrom.get(current);
            }
            Collections.reverse(currentPath); // Path is backwards, so flip it
        }
    }

    // Timer Loop for Auto-Movement
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameWon && currentPath != null && !currentPath.isEmpty()) {
            Point nextStep = currentPath.remove(0); // Get next tile
            playerX = nextStep.x;
            playerY = nextStep.y;

            // Check Win
            if (map[playerY][playerX] == TILE_GOAL) {
                gameWon = true;
                currentPath.clear();
            }
            repaint();
        }
    }

    // Unused Mouse Methods
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // --- Main Entry Point ---
    public static void main(String[] args) {
        JFrame frame = new JFrame("Game");
        Game game = new Game();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}