package com.gameengine.logic;


/**
 * 游戏状态管理器
 * 负责管理游戏的整体状态，如计时器、暂停、结束条件等
 */
public class GameStateManager {
    // 游戏状态管理
    private float gameTimer = 0.0f;
    private float gameDuration; // 游戏总时长
    private boolean gameEnded = false;
    private boolean gameWon = false;
    private boolean gamePaused = false;
    private boolean gameLoading = false; // 游戏加载状态
    
    // 延迟恢复投射物管理
    private float projectileRestoreTimer = 0.0f;
    private final float PROJECTILE_RESTORE_DELAY = 0.5f;
    private boolean projectilesRestored = false;
    
    // 延迟恢复敌人管理
    private float enemyRestoreTimer = 0.0f;
    private final float ENEMY_RESTORE_DELAY = 0.5f;
    private boolean enemiesRestored = false;
    
    // 游戏结束延迟处理
    private float victoryDelayTimer = 0.0f;
    private float defeatDelayTimer = 0.0f;
    private final float VICTORY_DELAY = 2.0f;
    private final float DEFEAT_DELAY = 2.0f;
    
    public GameStateManager(float gameDuration) {
        this.gameDuration = gameDuration;
    }
    
    /**
     * 更新游戏状态
     */
    public void update(float deltaTime) {
        // 更新游戏计时器（无论是否暂停都要执行）
        if (!gameEnded && !gamePaused) {
            gameTimer += deltaTime;
        }
        
        // 延迟恢复投射物（无论是否暂停都要执行）
        if (!projectilesRestored) {
            projectileRestoreTimer += deltaTime;
        }
        
        // 延迟恢复敌人（无论是否暂停都要执行）
        if (!enemiesRestored) {
            enemyRestoreTimer += deltaTime;
        }
        
        // 游戏结束后的延迟处理
        if (gameEnded) {
            handleGameEndDelay(deltaTime);
        }
    }
    
    /**
     * 检查是否需要恢复投射物
     */
    public boolean shouldRestoreProjectiles() {
        return !projectilesRestored && projectileRestoreTimer >= PROJECTILE_RESTORE_DELAY;
    }
    
    /**
     * 标记投射物已恢复
     */
    public void markProjectilesRestored() {
        projectilesRestored = true;
    }
    
    /**
     * 检查是否需要恢复敌人
     */
    public boolean shouldRestoreEnemies() {
        return !enemiesRestored && enemyRestoreTimer >= ENEMY_RESTORE_DELAY;
    }
    
    /**
     * 标记敌人已恢复
     */
    public void markEnemiesRestored() {
        enemiesRestored = true;
    }
    
    /**
     * 设置游戏胜利
     */
    public void setGameWon() {
        gameEnded = true;
        gameWon = true;
        victoryDelayTimer = VICTORY_DELAY;
        System.out.println("胜利！所有敌人和Boss都被消灭！");
    }
    
    /**
     * 设置游戏失败
     */
    public void setGameLost() {
        gameEnded = true;
        gameWon = false;
        defeatDelayTimer = DEFEAT_DELAY;
        System.out.println("游戏失败！");
    }
    
    /**
     * 处理游戏结束延迟
     */
    private void handleGameEndDelay(float deltaTime) {
        if (gameWon && victoryDelayTimer > 0) {
            // 胜利延迟处理
            victoryDelayTimer -= deltaTime;
            if (victoryDelayTimer <= 0) {
                javax.swing.JOptionPane.showMessageDialog(null, "恭喜通关！葫芦娃胜利！");
                System.exit(0);
            }
        } else if (!gameWon && defeatDelayTimer > 0) {
            // 失败延迟处理
            defeatDelayTimer -= deltaTime;
            if (defeatDelayTimer <= 0) {
                javax.swing.JOptionPane.showMessageDialog(null, "游戏失败！葫芦娃被击败了！");
                System.exit(0);
            }
        }
    }
    
    /**
     * 重置游戏状态
     */
    public void resetGame() {
        gameTimer = 0.0f;
        gameEnded = false;
        gameWon = false;
        gamePaused = false;
        gameLoading = false; // 重置加载状态
        projectileRestoreTimer = 0.0f;
        projectilesRestored = false;
        enemyRestoreTimer = 0.0f;
        enemiesRestored = false;
        victoryDelayTimer = 0.0f;
        defeatDelayTimer = 0.0f;
    }
    
    // Getter方法
    public float getGameTimer() { return gameTimer; }
    public float getGameDuration() { return gameDuration; }
    public boolean isGameEnded() { return gameEnded; }
    public boolean isGameWon() { return gameWon; }
    public boolean isGamePaused() { return gamePaused; }
    public boolean isLoading() { return gameLoading; }
    public boolean areProjectilesRestored() { return projectilesRestored; }
    public boolean areEnemiesRestored() { return enemiesRestored; }
    
    // Setter方法
    public void setGamePaused(boolean paused) { this.gamePaused = paused; }
    public void setGameLoading(boolean loading) { this.gameLoading = loading; }
}
