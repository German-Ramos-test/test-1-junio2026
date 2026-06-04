package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import java.util.Random;

import org.junit.jupiter.api.Test;

class AppTest {
    @Test
    void clampPlayerToBounds_keepsPlayerInsideBoard() {
        Rectangle player = new Rectangle(-10, 900, 36, 36);

        GameLogic.clampPlayerToBounds(player, 800, 500);

        assertEquals(0, player.x);
        assertEquals(464, player.y);
    }

    @Test
    void updateVerticalEnemySpeed_invertsOnTopAndBottomBorders() {
        int speedAtTop = GameLogic.updateVerticalEnemySpeed(0, 32, 500, 5);
        int speedAtBottom = GameLogic.updateVerticalEnemySpeed(468, 32, 500, -4);
        int speedInMiddle = GameLogic.updateVerticalEnemySpeed(120, 32, 500, 3);

        assertEquals(-5, speedAtTop);
        assertEquals(4, speedAtBottom);
        assertEquals(3, speedInMiddle);
    }

    @Test
    void hasCollision_returnsTrueOnlyWhenRectanglesOverlap() {
        Rectangle player = new Rectangle(100, 100, 36, 36);
        Rectangle touchingEnemy = new Rectangle(120, 120, 32, 32);
        Rectangle farEnemy = new Rectangle(400, 300, 32, 32);

        assertTrue(GameLogic.hasCollision(player, touchingEnemy));
        assertFalse(GameLogic.hasCollision(player, farEnemy));
    }

    @Test
    void randomSignedSpeed_staysInExpectedAbsoluteRange() {
        Random random = new Random(42);

        for (int i = 0; i < 200; i++) {
            int speed = GameLogic.randomSignedSpeed(random, 3, 7);
            int absolute = Math.abs(speed);
            assertTrue(absolute >= 3 && absolute <= 7);
        }
    }

    @Test
    void gameEngine_constructorRejectsInvalidSpeedRange() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameEngine(800, 500, 36, 32, 6, 8, 7, 4, new Random(1)));
    }

    @Test
    void gameEngine_constructorRejectsNullRandom() {
        assertThrows(
                NullPointerException.class,
                () -> new GameEngine(800, 500, 36, 32, 6, 3, 7, 4, null));
    }
}
