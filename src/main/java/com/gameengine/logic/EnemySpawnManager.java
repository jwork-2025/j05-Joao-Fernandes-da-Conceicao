package com.gameengine.logic;

import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

/**
 * 敌人生成管理器
 * 负责管理敌人的生成逻辑
 */
public class EnemySpawnManager {
    private Scene scene;
    private float enemySpawnTimer = 0.0f;
    private boolean bossSpawned = false;
    
    private float enemySpawnInterval = 7.0f;
    private int enemySpawnNumber = 3;
    
    public EnemySpawnManager(Scene scene, float enemySpawnInterval, int enemySpawnNumber) {
        this.scene = scene;
        this.enemySpawnInterval = enemySpawnInterval;
        this.enemySpawnNumber = enemySpawnNumber;
    }
    
    /**
     * 更新敌人生成
     */
    public void update(float deltaTime, float gameTimer, float gameDuration) {
        enemySpawnTimer += deltaTime;
        
        // 处理敌人生成
        handleEnemySpawning(gameTimer, gameDuration);
    }
    
    /**
     * 处理敌人生成
     */
    private void handleEnemySpawning(float gameTimer, float gameDuration) {
        // 游戏时间到达后停止生成敌人
        if (gameTimer < gameDuration && enemySpawnTimer > enemySpawnInterval) {
            // 检查是否是最后一次生成（距离游戏结束还有7秒）
            if (!bossSpawned && gameTimer >= gameDuration - enemySpawnInterval) {
                // 最后一次生成：3个小兵 + 1个Boss
                createMinions(enemySpawnNumber);
                createBoss();
                bossSpawned = true;
                System.out.println("Boss已生成！");
            } else if (!bossSpawned) {
                // 普通生成：只生成小兵
                createMinions(enemySpawnNumber);
            }
            enemySpawnTimer = 0;
        }
    }
    
    /**
     * 创建小兵
     */
    private void createMinions(int count) {
        for (int i = 0; i < count; i++) {
            Vector2 spawnPos = getRandomSpawnPosition();
            GameObject minion = com.gameengine.characters.CharacterFactory.createMinion(spawnPos);
            scene.addGameObject(minion);
        }
    }
    
    /**
     * 创建Boss
     */
    private void createBoss() {
        Vector2 spawnPos = getRandomSpawnPosition();
        GameObject boss = com.gameengine.characters.CharacterFactory.createBoss(spawnPos);
        scene.addGameObject(boss);
    }
    
    /**
     * 获取随机生成位置
     */
    private Vector2 getRandomSpawnPosition() {
        java.util.Random random = new java.util.Random();
        int side = random.nextInt(4); // 0:上, 1:右, 2:下, 3:左
        
        switch (side) {
            case 0: // 上方
                return new Vector2(random.nextFloat() * 800, -50);
            case 1: // 右方
                return new Vector2(850, random.nextFloat() * 600);
            case 2: // 下方
                return new Vector2(random.nextFloat() * 800, 650);
            case 3: // 左方
                return new Vector2(-50, random.nextFloat() * 600);
        }
        return new Vector2(400, 300); // 默认中心位置
    }
    
    /**
     * 重置敌人生成状态
     */
    public void reset() {
        enemySpawnTimer = 0.0f;
        bossSpawned = false;
    }
    
    // Getter方法
    public boolean isBossSpawned() { return bossSpawned; }
}
