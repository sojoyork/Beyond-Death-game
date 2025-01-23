import javax.swing.*; 
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class BeyondDeath3D extends JPanel implements KeyListener, ActionListener {
    private double playerX = 3.5, playerY = 3.5;  // Player's position
    private double playerAngle = Math.PI / 4; // Angle the player is facing
    private double moveSpeed = 0.1; // Movement speed
    private double rotationSpeed = 0.05; // Rotation speed
    private javax.swing.Timer timer;  // Use javax.swing.Timer to avoid ambiguity

    private boolean movingForward = false;
    private boolean movingBackward = false;
    private boolean rotatingLeft = false;
    private boolean rotatingRight = false;

    // Map representation (1: wall, 0: empty space, 2: enemy)
    private int mapWidth = 10, mapHeight = 10;
    private int[][] map = {
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 0, 1, 1, 1, 1, 1, 1, 0, 1},
        {1, 0, 1, 0, 0, 0, 0, 1, 0, 1},
        {1, 0, 1, 0, 1, 1, 0, 1, 0, 1},
        {1, 0, 1, 0, 0, 1, 0, 0, 0, 1},
        {1, 0, 1, 1, 0, 1, 1, 0, 1, 1},
        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        {1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };

    // List of enemies (represented as coordinates)
    private ArrayList<Point> enemies = new ArrayList<>(Arrays.asList(
        new Point(4, 4),
        new Point(6, 6)
    ));

    private boolean seeThroughMode = false; // Toggle for "See-through" mode

    public BeyondDeath3D() {
        JFrame frame = new JFrame("Beyond-Death 3D");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.add(this);
        frame.setVisible(true);
        frame.addKeyListener(this);

        timer = new javax.swing.Timer(16, this); // ~60 FPS
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int screenWidth = getWidth();
        int screenHeight = getHeight();

        // Raycasting for walls
        for (int x = 0; x < screenWidth; x++) {
            double rayAngle = playerAngle - Math.PI / 4 + ((double) x / screenWidth) * Math.PI / 2; // FOV
            double distance = castRay(rayAngle);
            int lineHeight = (int) (screenHeight / distance);

            // Calculate shading based on distance (closer = darker)
            float shade = Math.min(1.0f, (float) (distance / 10.0));
            g.setColor(new Color(shade, shade, shade)); // Shading effect

            g.fillRect(x, (screenHeight - lineHeight) / 2, 1, lineHeight);
        }

        // "See-through" mode overlay (if active)
        if (seeThroughMode) {
            g.setColor(new Color(0, 255, 0, 100)); // Brighter translucent green for stronger see-through effect
            g.fillRect(0, 0, screenWidth, screenHeight);

            // Draw enemies as visible through walls in 3D
            g.setColor(Color.RED);
            for (Point enemy : enemies) {
                double enemyDistance = Math.sqrt(Math.pow(enemy.x - playerX, 2) + Math.pow(enemy.y - playerY, 2));
                if (enemyDistance < 10) { // Show enemy if within a certain range
                    // 3D perspective: calculate size based on distance (closer = bigger)
                    int enemySize = (int) (screenHeight / (enemyDistance + 1));

                    // Calculate the position on the screen based on the angle and distance
                    int screenX = (int) ((enemy.x - playerX) * screenWidth / 10) + screenWidth / 2;
                    int screenY = (int) ((enemy.y - playerY) * screenHeight / 10) + screenHeight / 2;

                    // Draw the enemy as a 3D-like object
                    g.fillOval(screenX - enemySize / 2, screenY - enemySize / 2, enemySize, enemySize);
                }
            }
        }

        // Display message in see-through mode
        if (seeThroughMode) {
            g.setColor(Color.WHITE);
            g.drawString("SEE-THROUGH MODE ACTIVE", 10, 40);
        }

        // HUD
        g.setColor(Color.WHITE);
        g.drawString("See-Through Mode: " + (seeThroughMode ? "ON" : "OFF"), 10, 20);
    }

    private double castRay(double angle) {
        double rayX = playerX;
        double rayY = playerY;
        double rayDirX = Math.cos(angle);
        double rayDirY = Math.sin(angle);
        double distance = 0;

        while (distance < 10) {
            rayX += rayDirX * 0.05;
            rayY += rayDirY * 0.05;
            int mapX = (int) rayX;
            int mapY = (int) rayY;

            if (mapX < 0 || mapX >= mapWidth || mapY < 0 || mapY >= mapHeight || map[mapX][mapY] == 1) {
                break;
            }
            distance += 0.05;
        }

        return distance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Movement and rotation logic
        if (movingForward) {
            double newX = playerX + Math.cos(playerAngle) * moveSpeed;
            double newY = playerY + Math.sin(playerAngle) * moveSpeed;

            if (map[(int) newX][(int) playerY] == 0) playerX = newX;
            if (map[(int) playerX][(int) newY] == 0) playerY = newY;
        }

        if (movingBackward) {
            double newX = playerX - Math.cos(playerAngle) * moveSpeed;
            double newY = playerY - Math.sin(playerAngle) * moveSpeed;

            if (map[(int) newX][(int) playerY] == 0) playerX = newX;
            if (map[(int) playerX][(int) newY] == 0) playerY = newY;
        }

        if (rotatingLeft) {
            playerAngle -= rotationSpeed;
        }

        if (rotatingRight) {
            playerAngle += rotationSpeed;
        }

        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                movingForward = true;
                break;
            case KeyEvent.VK_S:  // S key now moves backward
                movingBackward = true;
                break;
            case KeyEvent.VK_SPACE: // Spacebar now toggles see-through mode
                seeThroughMode = !seeThroughMode;  // Toggle see-through mode
                break;
            case KeyEvent.VK_A:
                rotatingLeft = true;
                break;
            case KeyEvent.VK_D:
                rotatingRight = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                movingForward = false;
                break;
            case KeyEvent.VK_S:  // Stop moving backward when S is released
                movingBackward = false;
                break;
            case KeyEvent.VK_SPACE:
                break;
            case KeyEvent.VK_A:
                rotatingLeft = false;
                break;
            case KeyEvent.VK_D:
                rotatingRight = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    public static void main(String[] args) {
        new BeyondDeath3D();
    }
}
