package com.gameengine.logic;

import com.gameengine.core.GameLogic;
import com.gameengine.core.GameObject;
import com.gameengine.scene.Scene;

/**
 * 高级游戏逻辑 - 继承基础GameLogic并整合各个子系统
 */
public class AdvancedGameLogic extends GameLogic {
    // 各个子系统
    private GameStateManager gameStateManager;
    private EnemySpawnManager enemySpawnManager;
    private PhysicsManager physicsManager;
    private CombatSystem combatSystem;
    
    public AdvancedGameLogic(Scene scene, float enemySpawnInterval, int enemySpawnNumber, float gameDuration) {
        super(scene);
        
        // 初始化各个子系统
        this.gameStateManager = new GameStateManager(gameDuration);
        this.enemySpawnManager = new EnemySpawnManager(scene, enemySpawnInterval, enemySpawnNumber);
        this.physicsManager = new PhysicsManager(scene);
        this.combatSystem = new CombatSystem(scene);
    }
    
    /**
     * 重写主要的游戏逻辑更新方法
     */
    @Override
    public void update(float deltaTime) {
        // 检查主菜单状态
        GameObject mainMenu = scene.findGameObjectByTag("MainMenu");
        if (mainMenu != null && mainMenu.hasComponent(com.gameengine.components.MainMenuComponent.class)) {
            com.gameengine.components.MainMenuComponent menu = mainMenu.getComponent(com.gameengine.components.MainMenuComponent.class);
            if (menu.isActive()) {
                return; // 如果主菜单激活，不执行游戏逻辑
            }
        }
        
        // 检查是否有玩家对象，如果没有则不执行游戏逻辑
        GameObject player = scene.findGameObjectByTag("Player");
        if (player == null) {
            return; // 没有玩家对象，不执行游戏逻辑
        }
        
        // 更新游戏状态管理器
        gameStateManager.update(deltaTime);
        
        // 延迟恢复投射物（无论是否暂停都要执行）
        if (gameStateManager.shouldRestoreProjectiles()) {
            GameObject loadSystem = scene.findGameObjectByTag("LoadSystem");
            if (loadSystem != null && loadSystem.hasComponent(com.gameengine.components.LoadSystemComponent.class)) {
                com.gameengine.components.LoadSystemComponent load = loadSystem.getComponent(com.gameengine.components.LoadSystemComponent.class);
                load.restorePendingProjectiles(scene);
                gameStateManager.markProjectilesRestored();
                System.out.println("投射物延迟恢复完成");
            }
        }
        
        // 延迟恢复敌人（无论是否暂停都要执行）
        if (gameStateManager.shouldRestoreEnemies()) {
            GameObject loadSystem = scene.findGameObjectByTag("LoadSystem");
            if (loadSystem != null && loadSystem.hasComponent(com.gameengine.components.LoadSystemComponent.class)) {
                com.gameengine.components.LoadSystemComponent load = loadSystem.getComponent(com.gameengine.components.LoadSystemComponent.class);
                load.restorePendingEnemies(scene);
                gameStateManager.markEnemiesRestored();
                System.out.println("敌人延迟恢复完成");
                
                // 所有延迟恢复完成后，暂时不设置游戏加载状态为false
                // 因为对象是通过addGameObject添加的，需要等待下一帧才会真正添加到场景中
                // 所以我们保持gameLoading=true，直到下一帧再设置为false
                if (gameStateManager.areProjectilesRestored() && gameStateManager.areEnemiesRestored()) {
                    // 延迟恢复完成，等待下一帧设置gameLoading=false
                }
            }
        }
        
        // 如果所有延迟恢复都完成了，并且gameLoading仍然是true，说明上一帧刚完成恢复
        // 这一帧对象已经被添加到场景中了，可以设置gameLoading=false了
        if (gameStateManager.isLoading() && gameStateManager.areProjectilesRestored() && gameStateManager.areEnemiesRestored()) {
            // 检查场景中是否已经有恢复的对象，确保对象已经被添加
            boolean hasRestoredObjects = false;
            for (GameObject obj : scene.getGameObjects()) {
                if (("Minion".equals(obj.getTag()) || "Boss".equals(obj.getTag()) || 
                     "BossBomb".equals(obj.getTag()) || "EnemyProjectile".equals(obj.getTag()) || 
                     "PlayerProjectile".equals(obj.getTag())) && obj.isActive()) {
                    hasRestoredObjects = true;
                    break;
                }
            }
            
            // 只有当场景中确实有恢复的对象时，才设置gameLoading=false
            if (hasRestoredObjects) {
                gameStateManager.setGameLoading(false);
            }
        }
        
        // 如果游戏暂停或结束，不执行游戏逻辑
        if (gameStateManager.isGamePaused() || gameStateManager.isGameEnded()) {
            return;
        }
        
        // 更新各个子系统
        enemySpawnManager.update(deltaTime, gameStateManager.getGameTimer(), gameStateManager.getGameDuration());
        physicsManager.update();
        combatSystem.update();
        
        // 只有在非加载状态下才检查游戏结束条件
        // 这样可以避免在加载存档时，由于敌人还没有恢复而被错误地判定为胜利
        if (!gameStateManager.isLoading()) {
            checkGameEndConditions();
        }
    }
    
    /**
     * 检查游戏结束条件
     */
    private void checkGameEndConditions() {
        // 检查玩家是否死亡
        GameObject player = scene.findGameObjectByTag("Player");
        if (player != null && player.hasComponent(com.gameengine.components.HealthComponent.class)) {
            com.gameengine.components.HealthComponent health = player.getComponent(com.gameengine.components.HealthComponent.class);
            if (!health.isAlive()) {
                gameStateManager.setGameLost();
                return;
            }
        }
        
        // 检查是否通关：游戏时间到且没有敌人（包括Boss）
        if (gameStateManager.getGameTimer() >= gameStateManager.getGameDuration()) {
            boolean hasEnemies = false;
            boolean hasBosses = false;
            
            for (GameObject obj : scene.getGameObjects()) {
                if (obj.isActive() && obj.hasComponent(com.gameengine.components.HealthComponent.class) && 
                    obj.getComponent(com.gameengine.components.HealthComponent.class).isAlive()) {
                    
                    if ("Minion".equals(obj.getTag())) {
                        hasEnemies = true;
                    } else if ("Boss".equals(obj.getTag())) {
                        hasBosses = true;
                    }
                }
            }
            
            // 只有在没有小兵和Boss的情况下才算胜利
            if (!hasEnemies && !hasBosses && !gameStateManager.isGameEnded()) {
                gameStateManager.setGameWon();
            }
        }
    }
    
    /**
     * 重置游戏状态
     */
    public void resetGame() {
        gameStateManager.resetGame();
        enemySpawnManager.reset();
    }
    
    // Getter方法，用于向外暴露游戏状态
    public float getGameTimer() { return gameStateManager.getGameTimer(); }
    public float getGameDuration() { return gameStateManager.getGameDuration(); }
    public boolean isGameEnded() { return gameStateManager.isGameEnded(); }
    public boolean isGameWon() { return gameStateManager.isGameWon(); }
    public boolean isGamePaused() { return gameStateManager.isGamePaused(); }
    public boolean isLoading() { return gameStateManager.isLoading(); }
    public boolean isBossSpawned() { return enemySpawnManager.isBossSpawned(); }
    public boolean areProjectilesRestored() { return gameStateManager.areProjectilesRestored(); }
    public boolean areEnemiesRestored() { return gameStateManager.areEnemiesRestored(); }
    
    public void setGamePaused(boolean paused) { gameStateManager.setGamePaused(paused); }
    public void setGameLoading(boolean loading) { gameStateManager.setGameLoading(loading); }
}
