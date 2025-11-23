package com.gameengine.logic;

import com.gameengine.components.AttackRangeComponent;
import com.gameengine.components.HealthComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.core.GameObject;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;
import com.gameengine.characters.Player;
import com.gameengine.characters.enemies.Boss;
import com.gameengine.characters.enemies.Enemy;
import com.gameengine.characters.projectiles.Bomb;
import com.gameengine.characters.projectiles.Bullet;
import com.gameengine.characters.projectiles.Cannonball;

import java.util.ArrayList;
import java.util.List;

/**
 * 战斗系统
 * 负责处理攻击、碰撞检测和战斗逻辑
 */
public class CombatSystem {
    private Scene scene;
    private InputManager inputManager;
    
    public CombatSystem(Scene scene) {
        this.scene = scene;
        this.inputManager = InputManager.getInstance();
    }
    
    /**
     * 更新战斗系统
     */
    public void update() {
        handlePlayerAttacks();
        handleEnemyAttacks();
        checkCollisions();
    }
    
    /**
     * 处理玩家攻击
     */
    public void handlePlayerAttacks() {
        GameObject playerObj = scene.findGameObjectByTag("Player");
        if (playerObj == null || !(playerObj instanceof Player)) return;
        
        Player player = (Player) playerObj;
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);

        if (inputManager.isKeyPressed(74) && player.canMeleeAttack()) { // J - 近战攻击
            createMeleeAttack(playerTransform.getPosition());
            player.performMeleeAttack();
            playSound("melee_attack"); // 播放近战音效
        } else if (inputManager.isKeyPressed(85) && player.canRangeAttack()) { // U - 远程攻击
            GameObject nearestEnemy = findNearestEnemy(playerTransform.getPosition());
            if (nearestEnemy != null) {
                Vector2 target = nearestEnemy.getComponent(TransformComponent.class).getPosition();
                createBullet(playerTransform.getPosition(), target, "PlayerProjectile");
                player.performRangeAttack();
                playSound("bullet_fire"); // 播放子弹音效
            }
        } else if (inputManager.isKeyPressed(73) && player.canCannonAttack()) { // I - 炮弹攻击
            GameObject nearestEnemy = findNearestEnemy(playerTransform.getPosition());
            if (nearestEnemy != null) {
                Vector2 target = nearestEnemy.getComponent(TransformComponent.class).getPosition();
                createCannonball(playerTransform.getPosition(), target, "PlayerCannonball");
                player.performCannonAttack();
                playSound("cannon_fire"); // 播放炮弹音效
            }
        }
    }
    
    /**
     * 处理敌人攻击
     */
    public void handleEnemyAttacks() {
        // 处理敌人的攻击请求
        for (GameObject obj : scene.getGameObjects()) {
            if ("EnemyNeedsMeleeAttack".equals(obj.getTag())) {
                TransformComponent transform = obj.getComponent(TransformComponent.class);
                if (transform != null) {
                    createEnemyMeleeAttack(transform.getPosition());
                    playSound("melee_attack"); // 播放敌人近战音效
                    // 根据对象类型恢复原始标签
                    if (obj instanceof Boss) {
                        obj.setTag("Boss");
                    } else {
                        obj.setTag("Minion");
                    }
                }
            } else if ("EnemyNeedsRangeAttack".equals(obj.getTag())) {
                TransformComponent transform = obj.getComponent(TransformComponent.class);
                GameObject player = scene.findGameObjectByTag("Player");
                if (transform != null && player != null) {
                    TransformComponent playerTransform = player.getComponent(TransformComponent.class);
                    if (playerTransform != null) {
                        Vector2 target = playerTransform.getPosition();
                        createBullet(transform.getPosition(), target, "EnemyProjectile");
                        playSound("bullet_fire"); // 播放敌人子弹音效
                        // 根据对象类型恢复原始标签
                        if (obj instanceof Boss) {
                            obj.setTag("Boss");
                        } else {
                            obj.setTag("Minion");
                        }
                    }
                }
            } else if ("BossNeedsBombAttack".equals(obj.getTag())) {
                TransformComponent transform = obj.getComponent(TransformComponent.class);
                GameObject player = scene.findGameObjectByTag("Player");
                if (transform != null && player != null) {
                    TransformComponent playerTransform = player.getComponent(TransformComponent.class);
                    if (playerTransform != null) {
                        Vector2 target = playerTransform.getPosition();
                        createBomb(transform.getPosition(), target, "BossBomb");
                        playSound("bomb_throw"); // 播放炸弹投掷音效
                        obj.setTag("Boss");
                    }
                }
            }
        }
    }
    
    /**
     * 检查碰撞
     */
    public void checkCollisions() {
        GameObject player = scene.findGameObjectByTag("Player");
        if (player == null) return;

        List<GameObject> enemies = new ArrayList<>();
        enemies.addAll(scene.findGameObjectsByTag("Minion"));
        enemies.addAll(scene.findGameObjectsByTag("Boss"));
        List<GameObject> playerProjectiles = scene.findGameObjectsByTag("PlayerProjectile");
        List<GameObject> playerCannonballs = scene.findGameObjectsByTag("PlayerCannonball");
        List<GameObject> enemyProjectiles = scene.findGameObjectsByTag("EnemyProjectile");
        List<GameObject> meleeAttacks = scene.findGameObjectsByTag("MeleeAttack");
        List<GameObject> enemyMeleeAttacks = scene.findGameObjectsByTag("EnemyMeleeAttack");

        HealthComponent playerHealth = player.getComponent(HealthComponent.class);
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        if (playerHealth == null || playerTransform == null) return;

        // 1. 敌人近程攻击 vs 玩家
        for (GameObject enemyMelee : enemyMeleeAttacks) {
            TransformComponent meleeTransform = enemyMelee.getComponent(TransformComponent.class);
            if (meleeTransform != null) {
                float distance = playerTransform.getPosition().distance(meleeTransform.getPosition());
                if (distance < Enemy.getMeleeRangeStatic()) { // 敌人近程攻击范围
                    playerHealth.takeDamage(Enemy.getMeleeDamageStatic()); // 敌人近程攻击伤害
                    playSound("damage"); // 播放伤害音效
                    enemyMelee.destroy(); // 销毁攻击判定区域
                }
            }
        }

        // 2. 敌人子弹 vs 玩家
        for (GameObject projectile : enemyProjectiles) {
            TransformComponent projTransform = projectile.getComponent(TransformComponent.class);
            if (projTransform != null) {
                float distance = playerTransform.getPosition().distance(projTransform.getPosition());
                if (distance < Bullet.getBulletRange()) { // 子弹碰撞半径
                    playerHealth.takeDamage(Bullet.getBulletDamage()); // 敌人远程伤害
                    playSound("damage"); // 播放伤害音效
                    projectile.destroy(); // 销毁子弹
                }
            }
        }

        // 3. 玩家子弹 vs 敌人
        for (GameObject projectile : playerProjectiles) {
            TransformComponent projTransform = projectile.getComponent(TransformComponent.class);
            if (projTransform == null) continue;
            for (GameObject enemy : enemies) {
                if (!enemy.isActive()) continue;
                HealthComponent enemyHealth = enemy.getComponent(HealthComponent.class);
                if (enemyHealth == null || !enemyHealth.isAlive()) continue;
                TransformComponent enemyTransform = enemy.getComponent(TransformComponent.class);
                if (enemyHealth != null && enemyTransform != null) {
                    float distance = projTransform.getPosition().distance(enemyTransform.getPosition());
                    if (distance < Bullet.getBulletRange()) {
                        enemyHealth.takeDamage(Bullet.getBulletDamage()); // 子弹伤害
                        projectile.destroy(); // 销毁子弹
                        break;
                    }
                }
            }
        }
        
        // 4. 玩家炮弹 vs 敌人（多目标攻击）
        for (GameObject cannonball : playerCannonballs) {
            TransformComponent projTransform = cannonball.getComponent(TransformComponent.class);
            if (projTransform == null) continue;
            
            boolean hitAnyEnemy = false;
            for (GameObject enemy : enemies) {
                if (!enemy.isActive()) continue;
                HealthComponent enemyHealth = enemy.getComponent(HealthComponent.class);
                if (enemyHealth == null || !enemyHealth.isAlive()) continue;
                TransformComponent enemyTransform = enemy.getComponent(TransformComponent.class);
                if (enemyHealth != null && enemyTransform != null) {
                    float distance = projTransform.getPosition().distance(enemyTransform.getPosition());
                    if (distance < Cannonball.getCannonballScanRange()) { // 使用扫描距离进行碰撞检测
                        hitAnyEnemy = true;
                        // 不break，继续检查其他敌人
                    }
                }
            }
            
            // 如果击中了任何敌人，销毁炮弹并创建爆炸效果
            if (hitAnyEnemy) {
                // 在攻击范围内对所有敌人造成伤害
                for (GameObject enemy : enemies) {
                    if (!enemy.isActive()) continue;
                    HealthComponent enemyHealth = enemy.getComponent(HealthComponent.class);
                    if (enemyHealth == null || !enemyHealth.isAlive()) continue;
                    TransformComponent enemyTransform = enemy.getComponent(TransformComponent.class);
                    if (enemyHealth != null && enemyTransform != null) {
                        float distance = projTransform.getPosition().distance(enemyTransform.getPosition());
                        if (distance < Cannonball.getCannonballAttackRange()) { // 使用攻击距离
                            enemyHealth.takeDamage(Cannonball.getCannonballDamage());
                        }
                    }
                }
                
                cannonball.destroy();
                playSound("bomb_explode"); // 播放炮弹爆炸音效
                // 创建爆炸效果 - 直接在场景中添加对象
                GameObject explosionRange = new GameObject("CannonballExplosion", "CannonballExplosion");
                explosionRange.addComponent(new TransformComponent(new Vector2(projTransform.getPosition().x, projTransform.getPosition().y)));
                explosionRange.addComponent(new AttackRangeComponent(
                    Cannonball.getCannonballExplosionDuration(), 
                    Cannonball.getCannonballExplosionRadius()
                )); // 使用炮弹类的爆炸效果常量
                scene.addGameObject(explosionRange);
            }
        }
        
        // 5. Boss炸弹 vs 玩家
        List<GameObject> bossBombs = scene.findGameObjectsByTag("BossBomb");
        GameObject playerForBomb = scene.findGameObjectByTag("Player");
        if (playerForBomb != null) {
            TransformComponent playerTransformForBomb = playerForBomb.getComponent(TransformComponent.class);
            HealthComponent playerHealthForBomb = playerForBomb.getComponent(HealthComponent.class);
            
            for (GameObject bomb : bossBombs) {
                TransformComponent bombTransform = bomb.getComponent(TransformComponent.class);
                if (bombTransform != null && playerTransformForBomb != null && playerHealthForBomb != null) {
                    float distance = bombTransform.getPosition().distance(playerTransformForBomb.getPosition());
                    if (distance < Bomb.getBombScanRange()) { // 使用扫描距离进行碰撞检测
                        // 在攻击范围内对玩家造成伤害
                        if (distance < Bomb.getBombAttackRange()) {
                            playerHealthForBomb.takeDamage(Bomb.getBombDamage());
                            playSound("damage"); // 播放伤害音效
                        }
                        
                        bomb.destroy();
                        playSound("bomb_explode"); // 播放炸弹爆炸音效
                        
                        // 创建炸弹爆炸效果
                        GameObject explosionRange = new GameObject("BombExplosion", "BombExplosion");
                        explosionRange.addComponent(new TransformComponent(new Vector2(bombTransform.getPosition().x, bombTransform.getPosition().y)));
                        explosionRange.addComponent(new AttackRangeComponent(
                            Bomb.getBombExplosionDuration(), 
                            Bomb.getBombExplosionRadius()
                        ));
                        scene.addGameObject(explosionRange);
                        System.out.println("Boss炸弹击中玩家！");
                    }
                }
            }
        }
        
        // 6. 玩家近战 vs 敌人
        for (GameObject melee : meleeAttacks) {
            TransformComponent meleeTransform = melee.getComponent(TransformComponent.class);
            if (meleeTransform == null) continue;
            for (GameObject enemy : enemies) {
                HealthComponent enemyHealth = enemy.getComponent(HealthComponent.class);
                TransformComponent enemyTransform = enemy.getComponent(TransformComponent.class);
                if (enemyHealth != null && enemyTransform != null) {
                    float distance = meleeTransform.getPosition().distance(enemyTransform.getPosition());
                    if (distance < Player.getMeleeRange()) { // 使用玩家类的近战范围常量
                        enemyHealth.takeDamage(Player.getMeleeDamage()); // 使用玩家类的近战伤害常量
                    }
                }
            }
        }
    }
    
    /**
     * 查找最近的敌人
     */
    private GameObject findNearestEnemy(Vector2 playerPos) {
        GameObject nearest = null;
        float minDist = Float.MAX_VALUE;
        for (GameObject obj : scene.getGameObjects()) {
            if (("Minion".equals(obj.getTag()) || "Boss".equals(obj.getTag())) && 
                obj.isActive() && obj.hasComponent(com.gameengine.components.HealthComponent.class) && 
                obj.getComponent(com.gameengine.components.HealthComponent.class).isAlive() && 
                obj.hasComponent(TransformComponent.class)) {
                float dist = playerPos.distance(obj.getComponent(TransformComponent.class).getPosition());
                if (dist < minDist) {
                    minDist = dist;
                    nearest = obj;
                }
            }
        }
        return nearest;
    }
    
    /**
     * 播放音效
     */
    private void playSound(String soundName) {
        GameObject audioSystem = scene.findGameObjectByTag("AudioSystem");
        if (audioSystem != null && audioSystem.hasComponent(com.gameengine.components.AudioSystemComponent.class)) {
            com.gameengine.components.AudioSystemComponent audio = audioSystem.getComponent(com.gameengine.components.AudioSystemComponent.class);
            audio.playSound(soundName);
        }
    }
    
    /**
     * 创建近战攻击
     */
    private void createMeleeAttack(Vector2 position) {
        // 创建攻击范围效果
        GameObject attackRange = com.gameengine.characters.CharacterFactory.createPlayerMeleeAttack(position);
        scene.addGameObject(attackRange);
        
        // 创建攻击判定区域（用于碰撞检测）
        GameObject meleeArea = com.gameengine.characters.CharacterFactory.createPlayerMeleeArea(position);
        scene.addGameObject(meleeArea);
    }
    
    /**
     * 创建子弹
     */
    private void createBullet(Vector2 position, Vector2 target, String tag) {
        GameObject bullet = com.gameengine.characters.CharacterFactory.createBullet(position, target, tag);
        scene.addGameObject(bullet);
    }
    
    /**
     * 创建炮弹
     */
    private void createCannonball(Vector2 position, Vector2 target, String tag) {
        GameObject cannonball = com.gameengine.characters.CharacterFactory.createCannonball(position, target, tag);
        scene.addGameObject(cannonball);
    }
    
    /**
     * 创建炸弹
     */
    private void createBomb(Vector2 position, Vector2 target, String tag) {
        GameObject bomb = com.gameengine.characters.CharacterFactory.createBomb(position, target, tag);
        scene.addGameObject(bomb);
    }
    
    /**
     * 创建敌人近战攻击
     */
    private void createEnemyMeleeAttack(Vector2 position) {
        GameObject attackRange = com.gameengine.characters.CharacterFactory.createEnemyMeleeAttack(position);
        scene.addGameObject(attackRange);
    }
}
