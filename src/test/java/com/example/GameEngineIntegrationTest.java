package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Rectangle;
import java.util.Random;

import org.junit.jupiter.api.Test;

class GameEngineIntegrationTest {
    private static GameEngine createEngine() {
        return new GameEngine(800, 500, 36, 32, 6, 3, 7, 4, new Random(123));
    }

    @Test
    void tick_movesEnemyLeftOnEachFrame() {
        GameEngine engine = createEngine();
        int initialEnemyX = engine.getEnemy().x;

        engine.tick(false, false, false, false);

        assertEquals(initialEnemyX - 4, engine.getEnemy().x);
    }

    @Test
    void tick_whenEnemyLeavesScreen_addsPointAndRespawns() {
        GameEngine engine = createEngine();
        Rectangle enemy = engine.getEnemy();
        enemy.x = -enemy.width;
        enemy.y = 120;

        engine.tick(false, false, false, false);

        assertEquals(1, engine.getScore());
        assertTrue(engine.getEnemy().x >= 800);
        assertTrue(engine.isRunning());
    }

    @Test
    void tick_whenCollisionHappens_stopsGameAndNextTicksDoNotAdvance() {
        GameEngine engine = createEngine();
        Rectangle player = engine.getPlayer();
        Rectangle enemy = engine.getEnemy();

        enemy.x = player.x;
        enemy.y = player.y;

        engine.tick(false, false, false, false);

        assertFalse(engine.isRunning());

        int frozenX = player.x;
        int frozenScore = engine.getScore();

        engine.tick(true, false, false, false);

        assertEquals(frozenX, player.x);
        assertEquals(frozenScore, engine.getScore());
    }
}
