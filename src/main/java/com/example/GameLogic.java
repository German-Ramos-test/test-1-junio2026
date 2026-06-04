package com.example;

import java.awt.Rectangle;
import java.util.Random;

final class GameLogic {
    private GameLogic() {
    }

    static void clampPlayerToBounds(Rectangle player, int width, int height) {
        if (player.x < 0) {
            player.x = 0;
        }
        if (player.y < 0) {
            player.y = 0;
        }
        if (player.x + player.width > width) {
            player.x = width - player.width;
        }
        if (player.y + player.height > height) {
            player.y = height - player.height;
        }
    }

    static int updateVerticalEnemySpeed(int enemyY, int enemyHeight, int panelHeight, int currentSpeed) {
        if (enemyY <= 0 || enemyY + enemyHeight >= panelHeight) {
            return -currentSpeed;
        }
        return currentSpeed;
    }

    static int randomSignedSpeed(Random random, int minSpeed, int maxSpeed) {
        int baseSpeed = minSpeed + random.nextInt(maxSpeed - minSpeed + 1);
        return random.nextBoolean() ? baseSpeed : -baseSpeed;
    }

    static boolean hasCollision(Rectangle player, Rectangle enemy) {
        return player.intersects(enemy);
    }
}
