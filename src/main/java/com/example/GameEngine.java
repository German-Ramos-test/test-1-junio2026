package com.example;

import java.awt.Rectangle;
import java.util.Random;

final class GameEngine {
    private final int width;
    private final int height;
    private final int playerSpeed;
    private final int enemySize;
    private final int enemyMinSpeed;
    private final int enemyMaxSpeed;
    private final int enemyHorizontalSpeed;
    private final Random random;

    private final Rectangle player;
    private final Rectangle enemy;

    private int enemySpeed;
    private int score;
    private boolean running;

    GameEngine(
            int width,
            int height,
            int playerSize,
            int enemySize,
            int playerSpeed,
            int enemyMinSpeed,
            int enemyMaxSpeed,
            int enemyHorizontalSpeed,
            Random random) {
        this.width = width;
        this.height = height;
        this.playerSpeed = playerSpeed;
        this.enemySize = enemySize;
        this.enemyMinSpeed = enemyMinSpeed;
        this.enemyMaxSpeed = enemyMaxSpeed;
        this.enemyHorizontalSpeed = enemyHorizontalSpeed;
        this.random = random;

        this.player = new Rectangle(80, height / 2 - playerSize / 2, playerSize, playerSize);
        this.enemy = new Rectangle(width - 120, 80, enemySize, enemySize);
        this.enemySpeed = enemyMinSpeed + random.nextInt(enemyMaxSpeed - enemyMinSpeed + 1);
        this.score = 0;
        this.running = true;
    }

    void tick(boolean leftPressed, boolean rightPressed, boolean upPressed, boolean downPressed) {
        if (!running) {
            return;
        }

        if (leftPressed) {
            player.x -= playerSpeed;
        }
        if (rightPressed) {
            player.x += playerSpeed;
        }
        if (upPressed) {
            player.y -= playerSpeed;
        }
        if (downPressed) {
            player.y += playerSpeed;
        }

        GameLogic.clampPlayerToBounds(player, width, height);

        enemy.y += enemySpeed;
        enemySpeed = GameLogic.updateVerticalEnemySpeed(enemy.y, enemy.height, height, enemySpeed);

        enemy.x -= enemyHorizontalSpeed;
        if (enemy.x + enemy.width < 0) {
            score++;
            resetEnemyPosition();
        }

        if (GameLogic.hasCollision(player, enemy)) {
            running = false;
        }
    }

    void resetGame() {
        score = 0;
        running = true;

        player.x = 80;
        player.y = height / 2 - player.height / 2;

        enemy.x = width - 120;
        enemy.y = 80;
        enemySpeed = enemyMinSpeed + random.nextInt(enemyMaxSpeed - enemyMinSpeed + 1);
    }

    private void resetEnemyPosition() {
        enemy.x = width + random.nextInt(220);
        enemy.y = random.nextInt(height - enemySize);
        enemySpeed = GameLogic.randomSignedSpeed(random, enemyMinSpeed, enemyMaxSpeed);
    }

    Rectangle getPlayer() {
        return player;
    }

    Rectangle getEnemy() {
        return enemy;
    }

    int getScore() {
        return score;
    }

    boolean isRunning() {
        return running;
    }
}
