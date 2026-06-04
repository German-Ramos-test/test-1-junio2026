package com.example;

import java.awt.Rectangle;
import java.util.Objects;
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
        validateConstructorArguments(
                width,
                height,
                playerSize,
                enemySize,
                playerSpeed,
                enemyMinSpeed,
                enemyMaxSpeed,
                enemyHorizontalSpeed,
                random);

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
        enemySpeed = GameLogic.randomSignedSpeed(random, enemyMinSpeed, enemyMaxSpeed);
    }

    private void resetEnemyPosition() {
        enemy.x = width + random.nextInt(220);
        enemy.y = random.nextInt(height - enemySize);
        enemySpeed = GameLogic.randomSignedSpeed(random, enemyMinSpeed, enemyMaxSpeed);
    }

    Rectangle getPlayer() {
        return new Rectangle(player);
    }

    Rectangle getEnemy() {
        return new Rectangle(enemy);
    }

    void setEnemyPosition(int x, int y) {
        enemy.x = x;
        enemy.y = y;
    }

    int getScore() {
        return score;
    }

    boolean isRunning() {
        return running;
    }

    private static void validateConstructorArguments(
            int width,
            int height,
            int playerSize,
            int enemySize,
            int playerSpeed,
            int enemyMinSpeed,
            int enemyMaxSpeed,
            int enemyHorizontalSpeed,
            Random random) {
        Objects.requireNonNull(random, "random no puede ser null");

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width y height deben ser mayores que cero");
        }
        if (playerSize <= 0 || enemySize <= 0) {
            throw new IllegalArgumentException("playerSize y enemySize deben ser mayores que cero");
        }
        if (playerSize > width || playerSize > height) {
            throw new IllegalArgumentException("playerSize no puede superar el tamano del panel");
        }
        if (enemySize > height) {
            throw new IllegalArgumentException("enemySize no puede superar la altura del panel");
        }
        if (playerSpeed <= 0 || enemyHorizontalSpeed <= 0) {
            throw new IllegalArgumentException("playerSpeed y enemyHorizontalSpeed deben ser mayores que cero");
        }
        if (enemyMinSpeed <= 0 || enemyMaxSpeed < enemyMinSpeed) {
            throw new IllegalArgumentException("enemyMinSpeed y enemyMaxSpeed no son validos");
        }
        if (height <= enemySize) {
            throw new IllegalArgumentException("height debe ser mayor que enemySize");
        }
    }
}
