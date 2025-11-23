package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;
import com.gameengine.characters.Player;
import com.gameengine.characters.CharacterFactory;
import com.gameengine.characters.projectiles.Bullet;
import com.gameengine.characters.projectiles.Cannonball;
import com.gameengine.characters.projectiles.Bomb;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 加载系统组件
 * 负责游戏状态的加载功能
 */
public class LoadSystemComponent extends Component<LoadSystemComponent> {
    private static final String SAVE_DIR = "resources/saves";
    private static final String SAVE_FILE = "save.jsonl";
    
    // 延迟恢复的投射物数据
    private List<ProjectileData> pendingProjectiles = null;
    
    // 延迟恢复的敌人数据
    private List<EnemyData> pendingEnemies = null;
    
    @Override
    public void initialize() {
        // 加载系统不需要特殊初始化
    }
    
    @Override
    public void update(float deltaTime) {
        // 加载系统不需要更新
    }
    
    @Override
    public void render() {
        // 加载系统不需要渲染
    }
    
    /**
     * 加载游戏状态
     */
    public boolean loadGame(Scene scene) {
        try {
            String filePath = SAVE_DIR + "/" + SAVE_FILE;
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.out.println("存档文件不存在: " + filePath);
                return false;
            }
            
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            GameSaveData saveData = jsonlToSaveData(lines);
            
            // 恢复游戏状态
            restoreGameState(scene, saveData);
            
            System.out.println("游戏加载成功: " + filePath);
            return true;
            
        } catch (Exception e) {
            System.out.println("加载游戏失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 从指定文件加载游戏状态
     */
    public boolean loadGameFromFile(Scene scene, String filePath) {
        try {
            File file = new File(filePath);
            
            if (!file.exists()) {
                System.out.println("存档文件不存在: " + filePath);
                return false;
            }
            
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            GameSaveData saveData = jsonlToSaveData(lines);
            
            // 恢复游戏状态
            restoreGameState(scene, saveData);
            
            System.out.println("游戏加载成功: " + filePath);
            return true;
            
        } catch (Exception e) {
            System.out.println("加载游戏失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 延迟恢复投射物（在游戏开始后0.5秒调用）
     */
    public void restorePendingProjectiles(Scene scene) {
        if (pendingProjectiles == null || pendingProjectiles.isEmpty()) {
            System.out.println("没有待恢复的投射物");
            return;
        }
        
        System.out.println("开始延迟恢复投射物，数量: " + pendingProjectiles.size());
        restoreProjectiles(scene, pendingProjectiles);
        pendingProjectiles = null; // 清空待恢复数据
        System.out.println("延迟恢复投射物完成");
    }
    
    /**
     * 延迟恢复敌人（在游戏开始后0.5秒调用）
     */
    public void restorePendingEnemies(Scene scene) {
        if (pendingEnemies == null || pendingEnemies.isEmpty()) {
            System.out.println("没有待恢复的敌人");
            return;
        }
        
        System.out.println("开始延迟恢复敌人，数量: " + pendingEnemies.size());
        restoreEnemies(scene, pendingEnemies);
        pendingEnemies = null; // 清空待恢复数据
        System.out.println("延迟恢复敌人完成");
    }
    
    // 私有方法 - 游戏状态恢复
    private void restoreGameState(Scene scene, GameSaveData saveData) {
        // 清除现有对象
        scene.getGameObjects().clear();
        
        // 恢复玩家
        if (saveData.playerData != null) {
            GameObject player = CharacterFactory.createPlayer(saveData.playerData.position);
            HealthComponent health = player.getComponent(HealthComponent.class);
            if (health != null) {
                health.setHealth(saveData.playerData.health);
            }
            
            // 恢复玩家冷却时间
            if (player instanceof Player && saveData.playerCooldowns != null) {
                Player p = (Player) player;
                p.setMeleeCooldownTimer(saveData.playerCooldowns.getOrDefault("meleeCooldown", 0.0f));
                p.setRangeCooldownTimer(saveData.playerCooldowns.getOrDefault("rangeCooldown", 0.0f));
                p.setCannonCooldownTimer(saveData.playerCooldowns.getOrDefault("cannonCooldown", 0.0f));
                System.out.println("恢复玩家冷却时间: " + saveData.playerCooldowns);
            }
            
            scene.addGameObject(player);
        }
        
        // 存储敌人数据，稍后恢复（与投射物相同的延迟机制）
        this.pendingEnemies = saveData.enemies;
        System.out.println("敌人数据已存储，等待延迟恢复，数量: " + saveData.enemies.size());
        
        // 恢复游戏状态
        restoreGameStateFields(scene, saveData);
        
        // 存储投射物数据，稍后恢复
        this.pendingProjectiles = saveData.projectiles;
    }
    
    private void restoreGameStateFields(Scene scene, GameSaveData saveData) {
        // 通过AdvancedGameLogic恢复游戏状态
        try {
            // 获取GameScene的gameLogic字段
            java.lang.reflect.Field gameLogicField = scene.getClass().getDeclaredField("gameLogic");
            gameLogicField.setAccessible(true);
            Object gameLogic = gameLogicField.get(scene);
            
            if (gameLogic != null) {
                // 获取AdvancedGameLogic的gameStateManager字段
                java.lang.reflect.Field gameStateManagerField = gameLogic.getClass().getDeclaredField("gameStateManager");
                gameStateManagerField.setAccessible(true);
                Object gameStateManager = gameStateManagerField.get(gameLogic);
                
                if (gameStateManager != null) {
                    // 恢复游戏时间
                    java.lang.reflect.Field gameTimerField = gameStateManager.getClass().getDeclaredField("gameTimer");
                    gameTimerField.setAccessible(true);
                    gameTimerField.set(gameStateManager, saveData.gameTimer);
                    
                    // 恢复游戏结束状态
                    java.lang.reflect.Field gameEndedField = gameStateManager.getClass().getDeclaredField("gameEnded");
                    gameEndedField.setAccessible(true);
                    gameEndedField.set(gameStateManager, saveData.gameEnded);
                    
                    // 恢复游戏胜利状态
                    java.lang.reflect.Field gameWonField = gameStateManager.getClass().getDeclaredField("gameWon");
                    gameWonField.setAccessible(true);
                    gameWonField.set(gameStateManager, saveData.gameWon);
                    
                    // 设置游戏加载状态为true
                    java.lang.reflect.Field gameLoadingField = gameStateManager.getClass().getDeclaredField("gameLoading");
                    gameLoadingField.setAccessible(true);
                    gameLoadingField.set(gameStateManager, true);
                }
            }
        } catch (Exception e) {
            System.out.println("恢复游戏状态字段失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void restoreProjectiles(Scene scene, List<ProjectileData> projectiles) {
        if (projectiles == null) {
            System.out.println("没有投射物需要恢复");
            return;
        }
        
        System.out.println("开始恢复投射物，数量: " + projectiles.size());
        
        for (ProjectileData projectileData : projectiles) {
            try {
                GameObject projectile = null;
                
                // 使用CharacterFactory创建投射物，就像游戏中正常创建一样
                if ("Bullet".equals(projectileData.type)) {
                    // 检查速度是否为零，避免normalize()异常
                    if (projectileData.velocity.magnitude() > 0.01f) {
                        Vector2 direction = projectileData.velocity.normalize();
                        Vector2 target = projectileData.position.add(direction.multiply(Bullet.getBulletRange()));
                        projectile = CharacterFactory.createBullet(projectileData.position, target, projectileData.tag);
                    } else {
                        // 如果速度为零，使用默认方向
                        Vector2 target = projectileData.position.add(new Vector2(0, -Bullet.getBulletRange()));
                        projectile = CharacterFactory.createBullet(projectileData.position, target, projectileData.tag);
                    }
                } else if ("Cannonball".equals(projectileData.type)) {
                    if (projectileData.velocity.magnitude() > 0.01f) {
                        Vector2 direction = projectileData.velocity.normalize();
                        Vector2 target = projectileData.position.add(direction.multiply(Cannonball.getCannonballRange()));
                        projectile = CharacterFactory.createCannonball(projectileData.position, target, projectileData.tag);
                    } else {
                        Vector2 target = projectileData.position.add(new Vector2(0, -Cannonball.getCannonballRange()));
                        projectile = CharacterFactory.createCannonball(projectileData.position, target, projectileData.tag);
                    }
                } else if ("Bomb".equals(projectileData.type)) {
                    if (projectileData.velocity.magnitude() > 0.01f) {
                        Vector2 direction = projectileData.velocity.normalize();
                        Vector2 target = projectileData.position.add(direction.multiply(Bomb.getBombRange()));
                        projectile = CharacterFactory.createBomb(projectileData.position, target, projectileData.tag);
                    } else {
                        Vector2 target = projectileData.position.add(new Vector2(0, -Bomb.getBombRange()));
                        projectile = CharacterFactory.createBomb(projectileData.position, target, projectileData.tag);
                    }
                }
                
                if (projectile != null) {
                    // 重新设置为存储的状态
                    
                    // 设置正确的位置
                    TransformComponent transform = projectile.getComponent(TransformComponent.class);
                    if (transform != null) {
                        transform.setPosition(projectileData.position);
                    }
                    
                    // 设置正确的速度
                    PhysicsComponent physics = projectile.getComponent(PhysicsComponent.class);
                    if (physics != null) {
                        System.out.println("存储的速度: " + projectileData.velocity + " (模长: " + projectileData.velocity.magnitude() + ")");
                        System.out.println("CharacterFactory设置的速度: " + physics.getVelocity() + " (模长: " + physics.getVelocity().magnitude() + ")");
                        
                        // 检查速度是否合理
                        float storedSpeed = projectileData.velocity.magnitude();
                        float expectedSpeed = getExpectedSpeed(projectileData.type);
                        
                        if (Math.abs(storedSpeed - expectedSpeed) > 10.0f) {
                            System.out.println("警告: 存储的速度(" + storedSpeed + ")与预期速度(" + expectedSpeed + ")差异较大");
                            // 使用正确的速度模长，但保持存储的方向
                            Vector2 direction = projectileData.velocity.normalize();
                            Vector2 correctedVelocity = direction.multiply(expectedSpeed);
                            physics.setVelocity(correctedVelocity);
                            System.out.println("修正后的速度: " + correctedVelocity);
                        } else {
                            physics.setVelocity(projectileData.velocity);
                        }
                    }
                    
                    // 设置正确的生命周期
                    LifetimeComponent lifetime = projectile.getComponent(LifetimeComponent.class);
                    if (lifetime != null) {
                        lifetime.setRemainingTime(Math.max(projectileData.remainingLifetime, 0.1f));
                    }
                    
                    // 初始化精灵组件（确保图片加载）
                    SpriteComponent sprite = projectile.getComponent(SpriteComponent.class);
                    if (sprite != null) {
                        sprite.initialize();
                        System.out.println("初始化投射物精灵: " + projectileData.type);
                    }
                    
                    // 添加到场景
                    scene.addGameObject(projectile);
                    System.out.println("投射物已添加到场景: " + projectileData.type + " 位置: " + projectileData.position + " 速度: " + projectileData.velocity);
                } else {
                    System.out.println("投射物创建失败: " + projectileData.type);
                }
                
            } catch (Exception e) {
                System.out.println("恢复投射物失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("投射物恢复完成，场景中对象数量: " + scene.getGameObjects().size());
    }
    
    private void restoreEnemies(Scene scene, List<EnemyData> enemies) {
        if (enemies == null) {
            System.out.println("没有敌人需要恢复");
            return;
        }
        
        System.out.println("开始恢复敌人，数量: " + enemies.size());
        
        for (EnemyData enemyData : enemies) {
            try {
                System.out.println("恢复敌人: " + enemyData.tag + " 位置: " + enemyData.position + " 血量: " + enemyData.health);
                GameObject enemy = null;
                
                if ("Minion".equals(enemyData.tag)) {
                    enemy = CharacterFactory.createMinion(enemyData.position);
                    System.out.println("创建Minion成功");
                } else if ("Boss".equals(enemyData.tag)) {
                    enemy = CharacterFactory.createBoss(enemyData.position);
                    System.out.println("创建Boss成功");
                }
                
                if (enemy != null) {
                    HealthComponent health = enemy.getComponent(HealthComponent.class);
                    if (health != null) {
                        health.setHealth(enemyData.health);
                    }
                    
                    // 确保SpriteComponent正确初始化
                    SpriteComponent sprite = enemy.getComponent(SpriteComponent.class);
                    if (sprite != null) {
                        System.out.println("敌人SpriteComponent存在");
                        // 重新初始化SpriteComponent以确保图片加载
                        sprite.initialize();
                        System.out.println("重新初始化SpriteComponent完成");
                    } else {
                        System.out.println("敌人SpriteComponent不存在！");
                    }
                    
                    // 确保敌人对象是活跃的
                    enemy.setActive(true);
                    
                    scene.addGameObject(enemy);
                } else {
                    System.out.println("敌人创建失败");
                }
                
            } catch (Exception e) {
                System.out.println("恢复敌人失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("敌人恢复完成，场景中对象数量: " + scene.getGameObjects().size());
    }
    
    /**
     * 获取投射物的预期速度
     */
    private float getExpectedSpeed(String type) {
        switch (type) {
            case "Bullet":
                return Bullet.getBulletSpeed();
            case "Cannonball":
                return Cannonball.getCannonballSpeed();
            case "Bomb":
                return Bomb.getBombSpeed();
            default:
                return Cannonball.getCannonballSpeed(); // 默认使用炮弹速度
        }
    }
    
    // JSONL解析方法（逐行解析）
    private GameSaveData jsonlToSaveData(List<String> lines) {
        GameSaveData data = new GameSaveData();
        
        try {
            System.out.println("开始解析JSONL，共 " + lines.size() + " 行");
            
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) {
                    continue;
                }
                
                line = line.trim();
                String type = extractStringValue(line, "type");
                
                if ("gameState".equals(type)) {
                    // 解析游戏状态
                    data.gameTimer = extractFloatValue(line, "gameTimer");
                    data.gameEnded = extractBooleanValue(line, "gameEnded");
                    data.gameWon = extractBooleanValue(line, "gameWon");
                    data.bossSpawned = extractBooleanValue(line, "bossSpawned");
                    System.out.println("解析游戏状态: timer=" + data.gameTimer + ", ended=" + data.gameEnded);
                } else if ("player".equals(type)) {
                    // 解析玩家数据
                    data.playerData = new PlayerData();
                    int posStart = line.indexOf("\"position\":{");
                    if (posStart != -1) {
                        int posEnd = line.indexOf("}", posStart + 11);
                        if (posEnd != -1) {
                            String posJson = line.substring(posStart + 11, posEnd);
                            data.playerData.position = new Vector2(
                                extractFloatValue(posJson, "x"),
                                extractFloatValue(posJson, "y")
                            );
                        }
                    }
                    data.playerData.health = (int)extractFloatValue(line, "health");
                    data.playerData.maxHealth = (int)extractFloatValue(line, "maxHealth");
                    System.out.println("解析玩家数据: pos=" + data.playerData.position + ", health=" + data.playerData.health);
                } else if ("cooldowns".equals(type)) {
                    // 解析冷却时间
                    data.playerCooldowns = new HashMap<>();
                    data.playerCooldowns.put("meleeCooldown", extractFloatValue(line, "meleeCooldown"));
                    data.playerCooldowns.put("rangeCooldown", extractFloatValue(line, "rangeCooldown"));
                    data.playerCooldowns.put("cannonCooldown", extractFloatValue(line, "cannonCooldown"));
                    System.out.println("解析冷却时间: " + data.playerCooldowns);
                } else if ("enemy".equals(type)) {
                    // 解析敌人数据
                    EnemyData enemy = new EnemyData();
                    enemy.tag = extractStringValue(line, "tag");
                    enemy.type = extractStringValue(line, "enemyType");
                    int posStart = line.indexOf("\"position\":{");
                    if (posStart != -1) {
                        int posEnd = line.indexOf("}", posStart + 11);
                        if (posEnd != -1) {
                            String posJson = line.substring(posStart + 11, posEnd);
                            enemy.position = new Vector2(
                                extractFloatValue(posJson, "x"),
                                extractFloatValue(posJson, "y")
                            );
                        }
                    }
                    enemy.health = (int)extractFloatValue(line, "health");
                    enemy.maxHealth = (int)extractFloatValue(line, "maxHealth");
                    data.enemies.add(enemy);
                    System.out.println("解析敌人: " + enemy.tag + " at " + enemy.position);
                } else if ("projectile".equals(type)) {
                    // 解析投射物数据
                    ProjectileData projectile = new ProjectileData();
                    projectile.tag = extractStringValue(line, "tag");
                    projectile.type = extractStringValue(line, "projectileType");
                    int posStart = line.indexOf("\"position\":{");
                    if (posStart != -1) {
                        int posEnd = line.indexOf("}", posStart + 11);
                        if (posEnd != -1) {
                            String posJson = line.substring(posStart + 11, posEnd);
                            projectile.position = new Vector2(
                                extractFloatValue(posJson, "x"),
                                extractFloatValue(posJson, "y")
                            );
                        }
                    }
                    int velStart = line.indexOf("\"velocity\":{");
                    if (velStart != -1) {
                        int velEnd = line.indexOf("}", velStart + 12);
                        if (velEnd != -1) {
                            String velJson = line.substring(velStart + 12, velEnd);
                            projectile.velocity = new Vector2(
                                extractFloatValue(velJson, "x"),
                                extractFloatValue(velJson, "y")
                            );
                        }
                    }
                    projectile.lifetime = extractFloatValue(line, "lifetime");
                    projectile.remainingLifetime = extractFloatValue(line, "remainingLifetime");
                    data.projectiles.add(projectile);
                    System.out.println("解析投射物: " + projectile.type + " at " + projectile.position);
                }
            }

        } catch (Exception e) {
            System.out.println("JSONL解析失败: " + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }
    
    private float extractFloatValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);
            if (start == -1) {
                System.out.println("未找到键: " + key);
                return 0.0f;
            }
            
            start += pattern.length();
            int end = start;
            
            // 找到下一个逗号或右括号
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
                end++;
            }
            
            String value = json.substring(start, end).trim();
            System.out.println("解析 " + key + ": " + value);
            return Float.parseFloat(value);
        } catch (Exception e) {
            System.out.println("解析float失败: " + key + " - " + e.getMessage());
            return 0.0f;
        }
    }

    private boolean extractBooleanValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern) + pattern.length();
            int end = start;
            
            // 找到下一个逗号或右括号
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') {
                end++;
            }
            
            String value = json.substring(start, end).trim();
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            System.out.println("解析boolean失败: " + key + " - " + e.getMessage());
            return false;
        }
    }

    private String extractStringValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern);
            if (start == -1) {
                System.out.println("未找到字符串键: " + key);
                return "";
            }
            start += pattern.length();
            
            // 跳过可能的空白字符
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }
            
            // 检查是否有引号
            if (start >= json.length() || json.charAt(start) != '"') {
                System.out.println("字符串值缺少开始引号: " + key);
                return "";
            }
            start++; // 跳过开始引号
            
            int end = json.indexOf("\"", start);
            if (end == -1) {
                System.out.println("字符串值缺少结束引号: " + key);
                return "";
            }
            String value = json.substring(start, end);
            System.out.println("解析 " + key + " (String): " + value);
            return value;
        } catch (Exception e) {
            System.out.println("解析字符串失败: " + key + " - " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
    
    private PlayerData parsePlayerDataSimple(String json) {
        PlayerData data = new PlayerData();
        
        try {
            System.out.println("查找playerData部分...");
            
            // 找到playerData的开始位置
            int start = json.indexOf("\"playerData\":");
            if (start == -1) {
                System.out.println("未找到playerData部分");
                return data;
            }
            
            // 找到playerData对象开始的大括号
            int braceStart = json.indexOf("{", start);
            if (braceStart == -1) {
                System.out.println("未找到playerData对象开始");
                return data;
            }
            
            start = braceStart + 1; // 跳过开始的大括号
            
            // 找到对应的右括号
            int braceCount = 1;
            int end = start;
            while (end < json.length() && braceCount > 0) {
                if (json.charAt(end) == '{') braceCount++;
                else if (json.charAt(end) == '}') braceCount--;
                end++;
            }
            
            if (braceCount != 0) {
                System.out.println("playerData部分括号不匹配");
                return data;
            }
            
            String playerJson = json.substring(start, end - 1);
            System.out.println("解析玩家JSON: " + playerJson);
            
            // 解析位置
            int posStart = playerJson.indexOf("\"position\":");
            if (posStart == -1) {
                System.out.println("未找到position部分");
                return data;
            }
            
            // 找到position对象开始的大括号
            int posBraceStart = playerJson.indexOf("{", posStart);
            if (posBraceStart == -1) {
                System.out.println("未找到position对象开始");
                return data;
            }
            
            posStart = posBraceStart + 1; // 跳过开始的大括号
            
            // 找到position的右括号
            int posEnd = playerJson.indexOf("}", posStart);
            if (posEnd == -1) {
                System.out.println("position部分括号不匹配");
                return data;
            }
            
            String posJson = playerJson.substring(posStart, posEnd);
            System.out.println("解析位置JSON: " + posJson);
            
            data.position = new Vector2(
                extractFloatValue(posJson, "x"),
                extractFloatValue(posJson, "y")
            );
            
            // 解析血量
            data.health = (int)extractFloatValue(playerJson, "health");
            data.maxHealth = (int)extractFloatValue(playerJson, "maxHealth");
            
        } catch (Exception e) {
            System.out.println("解析玩家数据失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return data;
    }
    
    private List<EnemyData> parseEnemiesDataSimple(String json) {
        List<EnemyData> enemies = new ArrayList<>();
        
        try {
            System.out.println("查找enemies部分...");
            
            // 找到enemies数组的开始位置
            int start = json.indexOf("\"enemies\":");
            if (start == -1) {
                System.out.println("未找到enemies部分");
                return enemies;
            }
            
            // 找到enemies数组开始的中括号
            int bracketStart = json.indexOf("[", start);
            if (bracketStart == -1) {
                System.out.println("未找到enemies数组开始");
                return enemies;
            }
            
            start = bracketStart + 1; // 跳过开始的中括号
            
            // 找到对应的右中括号
            int bracketCount = 1;
            int end = start;
            while (end < json.length() && bracketCount > 0) {
                if (json.charAt(end) == '[') bracketCount++;
                else if (json.charAt(end) == ']') bracketCount--;
                end++;
            }
            
            if (bracketCount != 0) {
                System.out.println("enemies部分括号不匹配");
                return enemies;
            }
            
            String enemiesJson = json.substring(start, end - 1);
            System.out.println("解析敌人JSON: " + enemiesJson);
            
            // 逐个解析敌人对象，使用括号计数
            int pos = 0;
            int enemyIndex = 0;
            while (pos < enemiesJson.length()) {
                // 跳过空白字符
                while (pos < enemiesJson.length() && Character.isWhitespace(enemiesJson.charAt(pos))) {
                    pos++;
                }
                
                if (pos >= enemiesJson.length()) break;
                
                // 找到下一个 { 开始
                int blockStart = enemiesJson.indexOf("{", pos);
                if (blockStart == -1) break;
                
                // 使用括号计数找到对应的 }
                int braceCount = 1;
                int blockEnd = blockStart + 1;
                while (blockEnd < enemiesJson.length() && braceCount > 0) {
                    if (enemiesJson.charAt(blockEnd) == '{') braceCount++;
                    else if (enemiesJson.charAt(blockEnd) == '}') braceCount--;
                    blockEnd++;
                }
                
                if (braceCount != 0) break;
                
                String block = enemiesJson.substring(blockStart, blockEnd);
                System.out.println("敌人块 " + enemyIndex + ": " + block);
                
                EnemyData data = new EnemyData();
                data.tag = extractStringValue(block, "tag");
                data.type = extractStringValue(block, "type");
                
                // 解析位置
                int posStart = block.indexOf("\"position\":");
                if (posStart != -1) {
                    int posBraceStart = block.indexOf("{", posStart);
                    if (posBraceStart != -1) {
                        int posEnd = block.indexOf("}", posBraceStart);
                        if (posEnd != -1) {
                            String posJson = block.substring(posBraceStart + 1, posEnd);
                            data.position = new Vector2(
                                extractFloatValue(posJson, "x"),
                                extractFloatValue(posJson, "y")
                            );
                        }
                    }
                }
                
                data.health = (int)extractFloatValue(block, "health");
                data.maxHealth = (int)extractFloatValue(block, "maxHealth");
                
                enemies.add(data);
                enemyIndex++;
                
                // 移动到下一个位置
                pos = blockEnd;
            }
        } catch (Exception e) {
            System.out.println("解析敌人数据失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return enemies;
    }
    
    private List<ProjectileData> parseProjectilesDataSimple(String json) {
        List<ProjectileData> projectiles = new ArrayList<>();
        
        try {
            System.out.println("查找projectiles部分...");
            
            // 找到projectiles数组的开始位置
            int start = json.indexOf("\"projectiles\":");
            if (start == -1) {
                System.out.println("未找到projectiles部分");
                return projectiles;
            }
            
            // 找到projectiles数组开始的中括号
            int bracketStart = json.indexOf("[", start);
            if (bracketStart == -1) {
                System.out.println("未找到projectiles数组开始");
                return projectiles;
            }
            
            start = bracketStart + 1; // 跳过开始的中括号
            
            // 找到对应的右中括号
            int bracketCount = 1;
            int end = start;
            while (end < json.length() && bracketCount > 0) {
                if (json.charAt(end) == '[') bracketCount++;
                else if (json.charAt(end) == ']') bracketCount--;
                end++;
            }
            
            if (bracketCount != 0) {
                System.out.println("projectiles部分括号不匹配");
                return projectiles;
            }
            
            String projectilesJson = json.substring(start, end - 1);
            System.out.println("解析投射物JSON: " + projectilesJson);
            
            // 逐个解析投射物对象，使用括号计数
            int pos = 0;
            int projectileIndex = 0;
            while (pos < projectilesJson.length()) {
                // 跳过空白字符
                while (pos < projectilesJson.length() && Character.isWhitespace(projectilesJson.charAt(pos))) {
                    pos++;
                }
                
                if (pos >= projectilesJson.length()) break;
                
                // 找到下一个 { 开始
                int blockStart = projectilesJson.indexOf("{", pos);
                if (blockStart == -1) break;
                
                // 使用括号计数找到对应的 }
                int braceCount = 1;
                int blockEnd = blockStart + 1;
                while (blockEnd < projectilesJson.length() && braceCount > 0) {
                    if (projectilesJson.charAt(blockEnd) == '{') braceCount++;
                    else if (projectilesJson.charAt(blockEnd) == '}') braceCount--;
                    blockEnd++;
                }
                
                if (braceCount != 0) break;
                
                String block = projectilesJson.substring(blockStart, blockEnd);
                System.out.println("投射物块 " + projectileIndex + ": " + block);
                
                ProjectileData data = new ProjectileData();
                data.tag = extractStringValue(block, "tag");
                data.type = extractStringValue(block, "type");
                
                // 解析位置
                int posStart = block.indexOf("\"position\":");
                if (posStart != -1) {
                    int posBraceStart = block.indexOf("{", posStart);
                    if (posBraceStart != -1) {
                        int posEnd = block.indexOf("}", posBraceStart);
                        if (posEnd != -1) {
                            String posJson = block.substring(posBraceStart + 1, posEnd);
                            data.position = new Vector2(
                                extractFloatValue(posJson, "x"),
                                extractFloatValue(posJson, "y")
                            );
                        }
                    }
                }
                
                // 解析速度
                int velStart = block.indexOf("\"velocity\":");
                if (velStart != -1) {
                    int velBraceStart = block.indexOf("{", velStart);
                    if (velBraceStart != -1) {
                        int velEnd = block.indexOf("}", velBraceStart);
                        if (velEnd != -1) {
                            String velJson = block.substring(velBraceStart + 1, velEnd);
                            data.velocity = new Vector2(
                                extractFloatValue(velJson, "x"),
                                extractFloatValue(velJson, "y")
                            );
                        }
                    }
                }
                
                data.lifetime = extractFloatValue(block, "lifetime");
                data.remainingLifetime = extractFloatValue(block, "remainingLifetime");
                
                projectiles.add(data);
                projectileIndex++;
                
                // 移动到下一个位置
                pos = blockEnd;
            }
        } catch (Exception e) {
            System.out.println("解析投射物数据失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return projectiles;
    }
    
    private Map<String, Float> parseCooldownsSimple(String json) {
        Map<String, Float> cooldowns = new HashMap<>();
        
        try {
            System.out.println("查找playerCooldowns部分...");
            
            // 找到playerCooldowns的开始位置
            int start = json.indexOf("\"playerCooldowns\":");
            if (start == -1) {
                System.out.println("未找到playerCooldowns部分");
                cooldowns.put("meleeCooldown", 0.0f);
                cooldowns.put("rangeCooldown", 0.0f);
                cooldowns.put("cannonCooldown", 0.0f);
                return cooldowns;
            }
            
            // 找到playerCooldowns对象开始的大括号
            int braceStart = json.indexOf("{", start);
            if (braceStart == -1) {
                System.out.println("未找到playerCooldowns对象开始");
                cooldowns.put("meleeCooldown", 0.0f);
                cooldowns.put("rangeCooldown", 0.0f);
                cooldowns.put("cannonCooldown", 0.0f);
                return cooldowns;
            }
            
            start = braceStart + 1; // 跳过开始的大括号
            
            // 找到对应的右括号
            int braceCount = 1;
            int end = start;
            while (end < json.length() && braceCount > 0) {
                if (json.charAt(end) == '{') braceCount++;
                else if (json.charAt(end) == '}') braceCount--;
                end++;
            }
            
            if (braceCount != 0) {
                System.out.println("playerCooldowns部分括号不匹配");
                cooldowns.put("meleeCooldown", 0.0f);
                cooldowns.put("rangeCooldown", 0.0f);
                cooldowns.put("cannonCooldown", 0.0f);
                return cooldowns;
            }
            
            String cooldownsJson = json.substring(start, end - 1);
            System.out.println("解析冷却时间JSON: " + cooldownsJson);
            
            cooldowns.put("meleeCooldown", extractFloatValue(cooldownsJson, "meleeCooldown"));
            cooldowns.put("rangeCooldown", extractFloatValue(cooldownsJson, "rangeCooldown"));
            cooldowns.put("cannonCooldown", extractFloatValue(cooldownsJson, "cannonCooldown"));
            
        } catch (Exception e) {
            System.out.println("解析冷却时间失败: " + e.getMessage());
            e.printStackTrace();
            cooldowns.put("meleeCooldown", 0.0f);
            cooldowns.put("rangeCooldown", 0.0f);
            cooldowns.put("cannonCooldown", 0.0f);
        }
        
        return cooldowns;
    }
    
    // 数据类
    public static class GameSaveData {
        public float gameTimer;
        public boolean gameEnded;
        public boolean gameWon;
        public boolean bossSpawned;
        public PlayerData playerData;
        public List<EnemyData> enemies = new ArrayList<>();
        public List<ProjectileData> projectiles = new ArrayList<>();
        public Map<String, Float> playerCooldowns = new HashMap<>();
    }
    
    public static class PlayerData {
        public Vector2 position;
        public int health;
        public int maxHealth;
    }
    
    public static class EnemyData {
        public String tag;
        public String type;
        public Vector2 position;
        public int health;
        public int maxHealth;
    }
    
    public static class ProjectileData {
        public String tag;
        public String type;
        public Vector2 position;
        public Vector2 velocity;
        public float lifetime;
        public float remainingLifetime;
    }
}
