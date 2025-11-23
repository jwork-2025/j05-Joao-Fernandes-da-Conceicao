package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;
import com.gameengine.characters.Player;
import com.gameengine.characters.enemies.Minion;
import com.gameengine.characters.enemies.Boss;
import com.gameengine.characters.CharacterFactory;
import com.gameengine.components.SpriteComponent;
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
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 存档系统组件
 * 负责游戏状态的保存和加载
 */
public class SaveSystemComponent extends Component<SaveSystemComponent> {
    private static final String SAVE_DIR = "resources/saves";
    private static final String SAVE_FILE = "save.jsonl";
    
    // 延迟恢复的投射物数据
    private List<ProjectileData> pendingProjectiles = null;
    
    @Override
    public void initialize() {
        // 确保存档目录存在
        try {
            Files.createDirectories(Paths.get(SAVE_DIR));
        } catch (IOException e) {
            System.out.println("创建存档目录失败: " + e.getMessage());
        }
    }
    
    @Override
    public void update(float deltaTime) {
        // 存档系统不需要更新
    }
    
    @Override
    public void render() {
        // 存档系统不需要渲染
    }
    
    /**
     * 保存游戏状态
     */
    public boolean saveGame(Scene scene) {
        try {
            // 打开文件选择器
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(SAVE_DIR));
            fileChooser.setSelectedFile(new File(SAVE_FILE));
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSONL存档文件", "jsonl"));
            fileChooser.setDialogTitle("保存游戏存档");
            
            int result = fileChooser.showSaveDialog(null);
            if (result != JFileChooser.APPROVE_OPTION) {
                System.out.println("用户取消了保存操作");
                return false;
            }
            
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            // 确保文件扩展名为.jsonl
            if (!filePath.toLowerCase().endsWith(".jsonl")) {
                filePath += ".jsonl";
            }
            
            // 准备保存数据
            GameSaveData saveData = new GameSaveData();
            
            // 保存游戏时间
            saveData.gameTimer = getGameTimer(scene);
            saveData.gameEnded = getGameEnded(scene);
            saveData.gameWon = getGameWon(scene);
            saveData.bossSpawned = getBossSpawned(scene);
            
            // 保存玩家信息
            GameObject player = scene.findGameObjectByTag("Player");
            if (player != null) {
                saveData.playerData = extractPlayerData(player);
            }
            
            // 保存敌人信息
            saveData.enemies = extractEnemiesData(scene);
            
            // 保存投射物信息
            saveData.projectiles = extractProjectilesData(scene);
            
            // 保存攻击冷却
            saveData.playerCooldowns = extractPlayerCooldowns(player);
            
            // 转换为JSONL并保存
            List<String> jsonlLines = saveDataToJsonl(saveData);
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (String line : jsonlLines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            
            System.out.println("游戏存档成功: " + filePath);
            return true;
            
        } catch (Exception e) {
            System.out.println("保存游戏失败: " + e.getMessage());
            return false;
        }
    }
    
    
    /**
     * 检查存档是否存在
     */
    public boolean hasSaveFile() {
        String filePath = SAVE_DIR + "/" + SAVE_FILE;
        return new File(filePath).exists();
    }
    
    /**
     * 删除存档文件
     */
    public boolean deleteSave() {
        try {
            String filePath = SAVE_DIR + "/" + SAVE_FILE;
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("存档删除成功");
                } else {
                    System.out.println("存档删除失败");
                }
                return deleted;
            }
            return true; // 文件不存在也算成功
        } catch (Exception e) {
            System.out.println("删除存档失败: " + e.getMessage());
            return false;
        }
    }
    
    // 私有辅助方法
    private float getGameTimer(Scene scene) {
        // 通过AdvancedGameLogic获取游戏时间
        try {
            // 获取GameScene的gameLogic字段
            java.lang.reflect.Field gameLogicField = scene.getClass().getDeclaredField("gameLogic");
            gameLogicField.setAccessible(true);
            Object gameLogic = gameLogicField.get(scene);
            
            if (gameLogic != null) {
                // 调用AdvancedGameLogic的getGameTimer方法
                java.lang.reflect.Method getGameTimerMethod = gameLogic.getClass().getMethod("getGameTimer");
                return (Float) getGameTimerMethod.invoke(gameLogic);
            }
        } catch (Exception e) {
            System.out.println("通过AdvancedGameLogic获取游戏时间失败: " + e.getMessage());
        }
        
        // 备用方案：直接通过反射获取GameStateManager
        try {
            // 获取AdvancedGameLogic的gameStateManager字段
            java.lang.reflect.Field gameLogicField = scene.getClass().getDeclaredField("gameLogic");
            gameLogicField.setAccessible(true);
            Object gameLogic = gameLogicField.get(scene);
            
            if (gameLogic != null) {
                java.lang.reflect.Field gameStateManagerField = gameLogic.getClass().getDeclaredField("gameStateManager");
                gameStateManagerField.setAccessible(true);
                Object gameStateManager = gameStateManagerField.get(gameLogic);
                
                if (gameStateManager != null) {
                    java.lang.reflect.Method getGameTimerMethod = gameStateManager.getClass().getMethod("getGameTimer");
                    return (Float) getGameTimerMethod.invoke(gameStateManager);
                }
            }
        } catch (Exception e) {
            System.out.println("通过GameStateManager获取游戏时间失败: " + e.getMessage());
        }
        
        return 0.0f;
    }
    
    private boolean getGameEnded(Scene scene) {
        // 通过AdvancedGameLogic获取游戏结束状态
        try {
            java.lang.reflect.Field gameLogicField = scene.getClass().getDeclaredField("gameLogic");
            gameLogicField.setAccessible(true);
            Object gameLogic = gameLogicField.get(scene);
            
            if (gameLogic != null) {
                java.lang.reflect.Method getGameEndedMethod = gameLogic.getClass().getMethod("isGameEnded");
                return (Boolean) getGameEndedMethod.invoke(gameLogic);
            }
        } catch (Exception e) {
            System.out.println("通过AdvancedGameLogic获取游戏结束状态失败: " + e.getMessage());
        }
        return false;
    }
    
    private boolean getGameWon(Scene scene) {
        // 通过AdvancedGameLogic获取游戏胜利状态
        try {
            java.lang.reflect.Field gameLogicField = scene.getClass().getDeclaredField("gameLogic");
            gameLogicField.setAccessible(true);
            Object gameLogic = gameLogicField.get(scene);
            
            if (gameLogic != null) {
                java.lang.reflect.Method getGameWonMethod = gameLogic.getClass().getMethod("isGameWon");
                return (Boolean) getGameWonMethod.invoke(gameLogic);
            }
        } catch (Exception e) {
            System.out.println("通过AdvancedGameLogic获取游戏胜利状态失败: " + e.getMessage());
        }
        return false;
    }
    
    private boolean getBossSpawned(Scene scene) {
        // 通过AdvancedGameLogic获取Boss生成状态
        try {
            java.lang.reflect.Field gameLogicField = scene.getClass().getDeclaredField("gameLogic");
            gameLogicField.setAccessible(true);
            Object gameLogic = gameLogicField.get(scene);
            
            if (gameLogic != null) {
                java.lang.reflect.Method getBossSpawnedMethod = gameLogic.getClass().getMethod("isBossSpawned");
                return (Boolean) getBossSpawnedMethod.invoke(gameLogic);
            }
        } catch (Exception e) {
            System.out.println("通过AdvancedGameLogic获取Boss生成状态失败: " + e.getMessage());
        }
        return false;
    }
    
    private PlayerData extractPlayerData(GameObject player) {
        PlayerData data = new PlayerData();
        
        TransformComponent transform = player.getComponent(TransformComponent.class);
        if (transform != null) {
            data.position = transform.getPosition();
        }
        
        HealthComponent health = player.getComponent(HealthComponent.class);
        if (health != null) {
            data.health = health.getHealth();
            data.maxHealth = health.getMaxHealth();
        }
        
        return data;
    }
    
    private List<EnemyData> extractEnemiesData(Scene scene) {
        List<EnemyData> enemies = new ArrayList<>();
        
        for (GameObject obj : scene.getGameObjects()) {
            if (obj.isActive() && obj.hasComponent(HealthComponent.class)) {
                // 排除玩家对象
                if ("Player".equals(obj.getTag())) {
                    continue;
                }
                
                HealthComponent health = obj.getComponent(HealthComponent.class);
                if (health.isAlive()) {
                    EnemyData data = new EnemyData();
                    // 恢复原始标签，避免保存攻击状态标签
                    String originalTag = getOriginalEnemyTag(obj);
                    data.tag = originalTag;
                    data.type = obj.getClass().getSimpleName();
                    
                    TransformComponent transform = obj.getComponent(TransformComponent.class);
                    if (transform != null) {
                        data.position = transform.getPosition();
                    }
                    
                    if (health != null) {
                        data.health = health.getHealth();
                        data.maxHealth = health.getMaxHealth();
                    }
                    
                    enemies.add(data);
                }
            }
        }
        
        return enemies;
    }
    
    private List<ProjectileData> extractProjectilesData(Scene scene) {
        List<ProjectileData> projectiles = new ArrayList<>();
        
        System.out.println("开始提取投射物数据...");
        for (GameObject obj : scene.getGameObjects()) {
            System.out.println("检查对象: " + obj.getName() + " (Tag: " + obj.getTag() + ", Active: " + obj.isActive() + ")");
            
            if (obj.isActive() && "Projectile".equals(obj.getName())) {
                System.out.println("找到投射物: " + obj.getTag());
                ProjectileData data = new ProjectileData();
                data.tag = obj.getTag();
                
                // 根据tag确定投射物类型
                String type = "";
                if (obj.getTag().contains("Cannonball")) {
                    type = "Cannonball";
                } else if (obj.getTag().contains("Bomb")) {
                    type = "Bomb";
                } else if (obj.getTag().contains("Projectile")) {
                    // 对于EnemyProjectile和PlayerProjectile，默认为Bullet类型
                    type = "Bullet";
                }
                data.type = type;
                System.out.println("  类型: " + type);
                
                TransformComponent transform = obj.getComponent(TransformComponent.class);
                if (transform != null) {
                    data.position = transform.getPosition();
                    System.out.println("  位置: " + data.position);
                }
                
                PhysicsComponent physics = obj.getComponent(PhysicsComponent.class);
                if (physics != null) {
                    data.velocity = physics.getVelocity();
                    System.out.println("  速度: " + data.velocity);
                }
                
                LifetimeComponent lifetime = obj.getComponent(LifetimeComponent.class);
                if (lifetime != null) {
                    data.lifetime = lifetime.getLifetime();
                    data.remainingLifetime = lifetime.getRemainingTime();
                    System.out.println("  剩余生命: " + data.remainingLifetime);
                }
                
                projectiles.add(data);
            }
        }
        
        System.out.println("提取完成，共找到 " + projectiles.size() + " 个投射物");
        return projectiles;
    }
    
    private Map<String, Float> extractPlayerCooldowns(GameObject player) {
        Map<String, Float> cooldowns = new HashMap<>();
        
        if (player instanceof Player) {
            Player p = (Player) player;
            cooldowns.put("meleeCooldown", p.getMeleeCooldownTimer());
            cooldowns.put("rangeCooldown", p.getRangeCooldownTimer());
            cooldowns.put("cannonCooldown", p.getCannonCooldownTimer());
        }
        
        return cooldowns;
    }
    
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
        
        // 恢复敌人
        System.out.println("开始恢复敌人，数量: " + saveData.enemies.size());
        for (EnemyData enemyData : saveData.enemies) {
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
                    System.out.println("设置敌人血量: " + enemyData.health);
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
                
                scene.addGameObject(enemy);
                System.out.println("敌人已添加到场景");
            } else {
                System.out.println("敌人创建失败");
            }
        }
        System.out.println("敌人恢复完成，场景中对象数量: " + scene.getGameObjects().size());
        
        // 恢复游戏状态
        restoreGameStateFields(scene, saveData);
        
        // 存储投射物数据，稍后恢复
        this.pendingProjectiles = saveData.projectiles;
    }
    
    private void restoreGameStateFields(Scene scene, GameSaveData saveData) {
        try {
            java.lang.reflect.Field gameTimerField = scene.getClass().getDeclaredField("gameTimer");
            gameTimerField.setAccessible(true);
            gameTimerField.set(scene, saveData.gameTimer);
            
            java.lang.reflect.Field gameEndedField = scene.getClass().getDeclaredField("gameEnded");
            gameEndedField.setAccessible(true);
            gameEndedField.set(scene, saveData.gameEnded);
            
            java.lang.reflect.Field gameWonField = scene.getClass().getDeclaredField("gameWon");
            gameWonField.setAccessible(true);
            gameWonField.set(scene, saveData.gameWon);
            
            java.lang.reflect.Field bossSpawnedField = scene.getClass().getDeclaredField("bossSpawned");
            bossSpawnedField.setAccessible(true);
            bossSpawnedField.set(scene, saveData.bossSpawned);
            
        } catch (Exception e) {
            System.out.println("恢复游戏状态字段失败: " + e.getMessage());
        }
    }
    
    // JSONL序列化方法（每行一个JSON对象）
    private List<String> saveDataToJsonl(GameSaveData data) {
        List<String> lines = new ArrayList<>();
        
        // 第一行：游戏状态
        StringBuilder gameState = new StringBuilder();
        gameState.append("{\"type\":\"gameState\",");
        gameState.append("\"gameTimer\":").append(data.gameTimer).append(",");
        gameState.append("\"gameEnded\":").append(data.gameEnded).append(",");
        gameState.append("\"gameWon\":").append(data.gameWon).append(",");
        gameState.append("\"bossSpawned\":").append(data.bossSpawned).append("}");
        lines.add(gameState.toString());
        
        // 第二行：玩家数据
        if (data.playerData != null) {
            StringBuilder player = new StringBuilder();
            player.append("{\"type\":\"player\",");
            player.append("\"position\":{\"x\":").append(data.playerData.position.x).append(",\"y\":").append(data.playerData.position.y).append("},");
            player.append("\"health\":").append(data.playerData.health).append(",");
            player.append("\"maxHealth\":").append(data.playerData.maxHealth).append("}");
            lines.add(player.toString());
        }
        
        // 第三行：玩家冷却时间
        StringBuilder cooldowns = new StringBuilder();
        cooldowns.append("{\"type\":\"cooldowns\",");
        cooldowns.append("\"meleeCooldown\":").append(data.playerCooldowns.getOrDefault("meleeCooldown", 0.0f)).append(",");
        cooldowns.append("\"rangeCooldown\":").append(data.playerCooldowns.getOrDefault("rangeCooldown", 0.0f)).append(",");
        cooldowns.append("\"cannonCooldown\":").append(data.playerCooldowns.getOrDefault("cannonCooldown", 0.0f)).append("}");
        lines.add(cooldowns.toString());
        
        // 每个敌人一行
        for (EnemyData enemy : data.enemies) {
            StringBuilder enemyLine = new StringBuilder();
            enemyLine.append("{\"type\":\"enemy\",");
            enemyLine.append("\"tag\":\"").append(enemy.tag).append("\",");
            enemyLine.append("\"enemyType\":\"").append(enemy.type).append("\",");
            enemyLine.append("\"position\":{\"x\":").append(enemy.position.x).append(",\"y\":").append(enemy.position.y).append("},");
            enemyLine.append("\"health\":").append(enemy.health).append(",");
            enemyLine.append("\"maxHealth\":").append(enemy.maxHealth).append("}");
            lines.add(enemyLine.toString());
        }
        
        // 每个投射物一行
        for (ProjectileData projectile : data.projectiles) {
            StringBuilder projectileLine = new StringBuilder();
            projectileLine.append("{\"type\":\"projectile\",");
            projectileLine.append("\"tag\":\"").append(projectile.tag).append("\",");
            projectileLine.append("\"projectileType\":\"").append(projectile.type).append("\",");
            projectileLine.append("\"position\":{\"x\":").append(projectile.position.x).append(",\"y\":").append(projectile.position.y).append("},");
            projectileLine.append("\"velocity\":{\"x\":").append(projectile.velocity.x).append(",\"y\":").append(projectile.velocity.y).append("},");
            projectileLine.append("\"lifetime\":").append(projectile.lifetime).append(",");
            projectileLine.append("\"remainingLifetime\":").append(projectile.remainingLifetime).append("}");
            lines.add(projectileLine.toString());
        }
        
        return lines;
    }
    
    private GameSaveData jsonToSaveData(String json) {
        GameSaveData data = new GameSaveData();
        
        try {
            System.out.println("开始解析JSON，长度: " + json.length());
            
            // 使用更简单的字符串解析方法
            data.gameTimer = extractFloatValue(json, "gameTimer");
            data.gameEnded = extractBooleanValue(json, "gameEnded");
            data.gameWon = extractBooleanValue(json, "gameWon");
            data.bossSpawned = extractBooleanValue(json, "bossSpawned");

            // 解析玩家数据
            data.playerData = parsePlayerDataSimple(json);

            // 解析敌人数据
            data.enemies = parseEnemiesDataSimple(json);

            // 解析投射物数据
            data.projectiles = parseProjectilesDataSimple(json);

            // 解析冷却时间
            data.playerCooldowns = parseCooldownsSimple(json);

        } catch (Exception e) {
            System.out.println("JSON解析失败: " + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }
    
    private float parseFloat(String json, String key) {
        try {
            int start = json.indexOf(key) + key.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end == -1) end = json.indexOf("\n", start);
            if (end == -1) end = json.length();
            
            String value = json.substring(start, end).trim();
            // 移除可能的换行符和空格
            value = value.replaceAll("\\s+", "");
            return Float.parseFloat(value);
        } catch (Exception e) {
            System.out.println("解析float失败: " + key + " - " + e.getMessage());
            return 0.0f;
        }
    }
    
    private boolean parseBoolean(String json, String key) {
        try {
            int start = json.indexOf(key) + key.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end == -1) end = json.indexOf("\n", start);
            if (end == -1) end = json.length();
            
            String value = json.substring(start, end).trim();
            value = value.replaceAll("\\s+", "");
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            System.out.println("解析boolean失败: " + key + " - " + e.getMessage());
            return false;
        }
    }
    
    private PlayerData parsePlayerData(String json) {
        PlayerData data = new PlayerData();
        
        // 解析位置
        int posStart = json.indexOf("\"position\":") + 11;
        int posEnd = json.indexOf("}", posStart) + 1;
        String posJson = json.substring(posStart, posEnd);
        data.position = new Vector2(
            parseFloat(posJson, "\"x\":"),
            parseFloat(posJson, "\"y\":")
        );
        
        // 解析血量
        data.health = (int)parseFloat(json, "\"health\":");
        data.maxHealth = (int)parseFloat(json, "\"maxHealth\":");
        
        return data;
    }
    
    private List<EnemyData> parseEnemiesData(String json) {
        List<EnemyData> enemies = new ArrayList<>();
        
        try {
            int enemiesStart = json.indexOf("\"enemies\":[") + 11;
            int enemiesEnd = json.lastIndexOf("]");
            
            if (enemiesStart == 10 || enemiesEnd == -1) {
                System.out.println("未找到enemies部分");
                return enemies;
            }
            
            String enemiesJson = json.substring(enemiesStart, enemiesEnd);
            System.out.println("解析敌人JSON: " + enemiesJson);
            
            String[] enemyBlocks = enemiesJson.split("\\},\\s*\\{");
            for (String block : enemyBlocks) {
                if (block.trim().isEmpty()) continue;
                
                // 确保每个块都以 { 开头和 } 结尾
                if (!block.startsWith("{")) block = "{" + block;
                if (!block.endsWith("}")) block = block + "}";
                
                EnemyData data = new EnemyData();
                data.tag = parseString(block, "\"tag\":");
                data.type = parseString(block, "\"type\":");
                
                // 解析位置
                int posStart = block.indexOf("\"position\":") + 11;
                int posEnd = block.indexOf("}", posStart) + 1;
                String posJson = block.substring(posStart, posEnd);
                data.position = new Vector2(
                    parseFloat(posJson, "\"x\":"),
                    parseFloat(posJson, "\"y\":")
                );
                
                data.health = (int)parseFloat(block, "\"health\":");
                data.maxHealth = (int)parseFloat(block, "\"maxHealth\":");
                
                enemies.add(data);
            }
        } catch (Exception e) {
            System.out.println("解析敌人数据失败: " + e.getMessage());
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
    
    private Map<String, Float> parseCooldowns(String json) {
        Map<String, Float> cooldowns = new HashMap<>();
        
        try {
            // 找到playerCooldowns的开始和结束位置
            int cooldownsStart = json.indexOf("\"playerCooldowns\":{") + 19;
            int cooldownsEnd = json.indexOf("}", cooldownsStart);
            
            // 确保找到了正确的结束位置
            if (cooldownsStart == 18 || cooldownsEnd == -1) {
                System.out.println("未找到playerCooldowns部分");
                cooldowns.put("meleeCooldown", 0.0f);
                cooldowns.put("rangeCooldown", 0.0f);
                cooldowns.put("cannonCooldown", 0.0f);
                return cooldowns;
            }
            
            String cooldownsJson = json.substring(cooldownsStart, cooldownsEnd);
            System.out.println("解析冷却时间JSON: " + cooldownsJson);
            
            cooldowns.put("meleeCooldown", parseFloat(cooldownsJson, "\"meleeCooldown\":"));
            cooldowns.put("rangeCooldown", parseFloat(cooldownsJson, "\"rangeCooldown\":"));
            cooldowns.put("cannonCooldown", parseFloat(cooldownsJson, "\"cannonCooldown\":"));
        } catch (Exception e) {
            System.out.println("解析冷却时间失败: " + e.getMessage());
            // 设置默认值
            cooldowns.put("meleeCooldown", 0.0f);
            cooldowns.put("rangeCooldown", 0.0f);
            cooldowns.put("cannonCooldown", 0.0f);
        }
        
        return cooldowns;
    }
    
    private String parseString(String json, String key) {
        int start = json.indexOf(key) + key.length();
        int end = json.indexOf("\"", start + 1);
        return json.substring(start + 1, end);
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
    
    /**
     * 获取敌人的原始标签（避免保存攻击状态标签）
     */
    private String getOriginalEnemyTag(GameObject obj) {
        String currentTag = obj.getTag();
        
        // 如果是攻击状态标签，根据对象类型返回原始标签
        if ("EnemyNeedsMeleeAttack".equals(currentTag) || "EnemyNeedsRangeAttack".equals(currentTag)) {
            if (obj instanceof Boss) {
                return "Boss";
            } else {
                return "Minion";
            }
        } else if ("BossNeedsBombAttack".equals(currentTag)) {
            return "Boss";
        }
        
        // 如果不是攻击状态标签，直接返回当前标签
        return currentTag;
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

    // 新的简化JSON解析方法
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
}
