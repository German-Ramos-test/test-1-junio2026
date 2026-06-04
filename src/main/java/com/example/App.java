package com.example;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mini Juego 2D - Esquiva Cuadrados");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            GamePanel gamePanel = new GamePanel();
            frame.setContentPane(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            gamePanel.requestFocusInWindow();
        });
    }

    private static class GamePanel extends JPanel {
        private static final int WIDTH = 800;
        private static final int HEIGHT = 500;

        private static final int PLAYER_SIZE = 36;
        private static final int PLAYER_SPEED = 6;

        private static final int ENEMY_SIZE = 32;
        private static final int ENEMY_MIN_SPEED = 3;
        private static final int ENEMY_MAX_SPEED = 7;

        private static final int TIMER_DELAY_MS = 16;

        private final Rectangle player;
        private final Rectangle enemy;
        private final Random random;

        private int enemySpeed;
        private int score;
        private boolean running;

        private boolean leftPressed;
        private boolean rightPressed;
        private boolean upPressed;
        private boolean downPressed;

        private final Timer gameTimer;

        GamePanel() {
            setPreferredSize(new Dimension(WIDTH, HEIGHT));
            setBackground(new Color(18, 24, 40));
            setFocusable(true);

            this.player = new Rectangle(80, HEIGHT / 2 - PLAYER_SIZE / 2, PLAYER_SIZE, PLAYER_SIZE);
            this.enemy = new Rectangle(WIDTH - 120, 80, ENEMY_SIZE, ENEMY_SIZE);
            this.random = new Random();
            this.enemySpeed = ENEMY_MIN_SPEED + random.nextInt(ENEMY_MAX_SPEED - ENEMY_MIN_SPEED + 1);
            this.score = 0;
            this.running = true;

            this.gameTimer = new Timer(TIMER_DELAY_MS, event -> {
                updateGame();
                repaint();
            });
            this.gameTimer.start();

            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_A:
                            leftPressed = true;
                            break;
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_D:
                            rightPressed = true;
                            break;
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_W:
                            upPressed = true;
                            break;
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_S:
                            downPressed = true;
                            break;
                        case KeyEvent.VK_R:
                            if (!running) {
                                resetGame();
                            }
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_A:
                            leftPressed = false;
                            break;
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_D:
                            rightPressed = false;
                            break;
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_W:
                            upPressed = false;
                            break;
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_S:
                            downPressed = false;
                            break;
                        default:
                            break;
                    }
                }
            });
        }

        private void updateGame() {
            if (!running) {
                return;
            }

            if (leftPressed) {
                player.x -= PLAYER_SPEED;
            }
            if (rightPressed) {
                player.x += PLAYER_SPEED;
            }
            if (upPressed) {
                player.y -= PLAYER_SPEED;
            }
            if (downPressed) {
                player.y += PLAYER_SPEED;
            }

            clampPlayerToBounds();

            enemy.y += enemySpeed;
            if (enemy.y <= 0 || enemy.y + enemy.height >= HEIGHT) {
                enemySpeed *= -1;
            }

            enemy.x -= 4;
            if (enemy.x + enemy.width < 0) {
                score++;
                resetEnemyPosition();
            }

            if (player.intersects(enemy)) {
                running = false;
                Toolkit.getDefaultToolkit().beep();
            }
        }

        private void clampPlayerToBounds() {
            if (player.x < 0) {
                player.x = 0;
            }
            if (player.y < 0) {
                player.y = 0;
            }
            if (player.x + player.width > WIDTH) {
                player.x = WIDTH - player.width;
            }
            if (player.y + player.height > HEIGHT) {
                player.y = HEIGHT - player.height;
            }
        }

        private void resetEnemyPosition() {
            enemy.x = WIDTH + random.nextInt(220);
            enemy.y = random.nextInt(HEIGHT - ENEMY_SIZE);

            int speed = ENEMY_MIN_SPEED + random.nextInt(ENEMY_MAX_SPEED - ENEMY_MIN_SPEED + 1);
            enemySpeed = random.nextBoolean() ? speed : -speed;
        }

        private void resetGame() {
            score = 0;
            running = true;

            player.x = 80;
            player.y = HEIGHT / 2 - PLAYER_SIZE / 2;

            enemy.x = WIDTH - 120;
            enemy.y = 80;
            enemySpeed = ENEMY_MIN_SPEED + random.nextInt(ENEMY_MAX_SPEED - ENEMY_MIN_SPEED + 1);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(new Color(40, 60, 94));
            for (int x = 0; x < WIDTH; x += 40) {
                g.drawLine(x, 0, x, HEIGHT);
            }
            for (int y = 0; y < HEIGHT; y += 40) {
                g.drawLine(0, y, WIDTH, y);
            }

            g.setColor(new Color(80, 220, 130));
            g.fillRect(player.x, player.y, player.width, player.height);

            g.setColor(new Color(255, 95, 86));
            g.fillRect(enemy.x, enemy.y, enemy.width, enemy.height);

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 22));
            g.drawString("Puntos: " + score, 20, 35);

            g.setFont(new Font("SansSerif", Font.PLAIN, 16));
            g.drawString("Mover: WASD o flechas", 20, HEIGHT - 20);

            if (!running) {
                g.setFont(new Font("SansSerif", Font.BOLD, 42));
                g.setColor(new Color(255, 225, 95));
                String gameOverText = "GAME OVER";
                int textWidth = g.getFontMetrics().stringWidth(gameOverText);
                g.drawString(gameOverText, (WIDTH - textWidth) / 2, HEIGHT / 2 - 12);

                g.setFont(new Font("SansSerif", Font.PLAIN, 20));
                g.setColor(Color.WHITE);
                String restartText = "Pulsa R para reiniciar";
                int restartWidth = g.getFontMetrics().stringWidth(restartText);
                g.drawString(restartText, (WIDTH - restartWidth) / 2, HEIGHT / 2 + 28);
            }
        }
    }
}
