package com.gameengine.example.scene;

import com.gameengine.components.*;
import com.gameengine.components.LoadSystemComponent;
import com.gameengine.core.Component;
import com.gameengine.core.GameObject;
import com.gameengine.core.GameEngine;
import com.gameengine.logic.AdvancedGameLogic;
import java.util.Iterator;
import com.gameengine.graphics.Renderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;
import com.gameengine.input.InputManager;
import com.gameengine.components.LifetimeComponent;
import com.gameengine.components.AttackRangeComponent;
import com.gameengine.characters.*;
import com.gameengine.characters.projectiles.*;
import com.gameengine.characters.enemies.*;

import java.util.Random;
import javax.swing.JOptionPane;

/**
 * 游戏场景类，负责游戏的具体实现
 */
public class GameScene extends Scene implements MainMenuComponent.GameScene {
    private GameEngine engine;
    private Renderer renderer;
    private Random random;
    private AdvancedGameLogic gameLogic;
    private boolean mKeyPressed = false; // M键按下状态跟踪
    private boolean nKeyPressed = false; // N键按下状态跟踪
    private boolean f12KeyPressed = false; // F12键按下状态跟踪
    private boolean pKeyPressed = false; // P键按下状态跟踪

    // 游戏参数 - 通过构造函数传入
    private float enemySpawnInterval;
    private int enemySpawnNumber;
    private float gameDuration;

    /**
     * 构造函数
     * @param engine 游戏引擎
     * @param enemySpawnInterval 敌人生成间隔（秒）
     * @param enemySpawnNumber 每次生成的敌人数量
     * @param gameDuration 游戏时长（秒）
     */
    public GameScene(GameEngine engine, float enemySpawnInterval, int enemySpawnNumber, float gameDuration) {
        super("GameScene");
        this.engine = engine;
        this.enemySpawnInterval = enemySpawnInterval;
        this.enemySpawnNumber = enemySpawnNumber;
        this.gameDuration = gameDuration;
    }
    
    @Override
    public void initialize() {
        super.initialize();
        this.renderer = engine.getRenderer();
        this.random = new Random();
        this.gameLogic = new AdvancedGameLogic(this, enemySpawnInterval, enemySpawnNumber, gameDuration);
        
        // 添加音效系统
        AudioSystemComponent audioSystem = new AudioSystemComponent();
        GameObject audioGameObject = new GameObject("AudioSystem", "AudioSystem");
        audioGameObject.addComponent(audioSystem);
        addGameObject(audioGameObject);
        
        // 添加背景音乐系统
        BackgroundMusicComponent bgmSystem = new BackgroundMusicComponent();
        GameObject bgmGameObject = new GameObject("BackgroundMusic", "BackgroundMusic");
        bgmGameObject.addComponent(bgmSystem);
        addGameObject(bgmGameObject);
        
        // 添加存档系统
        SaveSystemComponent saveSystem = new SaveSystemComponent();
        GameObject saveGameObject = new GameObject("SaveSystem", "SaveSystem");
        saveGameObject.addComponent(saveSystem);
        addGameObject(saveGameObject);
        
        // 添加加载系统
        LoadSystemComponent loadSystem = new LoadSystemComponent();
        GameObject loadGameObject = new GameObject("LoadSystem", "LoadSystem");
        loadGameObject.addComponent(loadSystem);
        addGameObject(loadGameObject);
        
        // 确保存档系统先被处理
        super.update(0.0f);
        
        // 添加主菜单
        MainMenuComponent mainMenu = new MainMenuComponent(renderer, this);
        GameObject menuGameObject = new GameObject("MainMenu", "MainMenu");
        menuGameObject.addComponent(mainMenu);
        addGameObject(menuGameObject);
        
        // 开始播放背景音乐
        startBackgroundMusic();
        
        // 注意：不在这里创建玩家和敌人，等主菜单选择"新游戏"后再创建
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        // 处理音频控制按键（无论是否暂停都有效）
        handleAudioControls();
        
        // 处理P键暂停功能
        handlePauseInput();
        
        // 如果游戏暂停，只执行保存功能，不执行其他游戏逻辑
        if (gameLogic.isGamePaused()) {
            // 暂停时仍然可以保存游戏
            handleSaveInput();
            return; // 暂停时不执行其他游戏逻辑
        }
        
        // 调用GameLogic的主要更新方法
        gameLogic.update(deltaTime);
        
        // 统一移除所有 inactive 或 !isAlive 的对象
        getGameObjects().removeIf(obj -> !obj.isActive() || (obj.hasComponent(HealthComponent.class) && !obj.getComponent(HealthComponent.class).isAlive()));
    }

    /**
     * 处理保存输入（仅在暂停时）
     */
    private void handleSaveInput() {
        InputManager input = InputManager.getInstance();
        
        // 暂停时，F12键保存游戏 - 只在按下时触发一次
        if (input.isKeyPressed(123)) { // F12键 - 保存游戏
            if (!f12KeyPressed) {
                saveGame();
                f12KeyPressed = true;
            }
        } else {
            f12KeyPressed = false; // 按键抬起时重置状态
        }
    }
    
    /**
     * 处理音频控制按键
     */
    private void handleAudioControls() {
        InputManager input = InputManager.getInstance();
        
        // 处理N键 - 背景音乐开关
        if (input.isKeyPressed(78)) { // N键
            if (!nKeyPressed) {
                BackgroundMusicComponent.toggleBgm();
                System.out.println("背景音乐设置切换为: " + (BackgroundMusicComponent.isBgmEnabled() ? "开启" : "关闭"));
                
                // 立即应用BGM设置 - 使用暂停/恢复而不是停止/开始
                GameObject bgmSystem = findGameObjectByTag("BackgroundMusic");
                if (bgmSystem != null && bgmSystem.hasComponent(BackgroundMusicComponent.class)) {
                    BackgroundMusicComponent bgm = bgmSystem.getComponent(BackgroundMusicComponent.class);
                    if (BackgroundMusicComponent.isBgmEnabled()) {
                        bgm.playMusic(); // 恢复播放（如果之前暂停）或开始播放
                    } else {
                        bgm.pauseMusic(); // 暂停而不是停止
                    }
                }
            }
            nKeyPressed = true;
        } else {
            nKeyPressed = false;
        }
        
        // 处理M键 - 音效开关
        if (input.isKeyPressed(77)) { // M键
            if (!mKeyPressed) {
                AudioSystemComponent.toggleAudio();
                System.out.println("音效设置切换为: " + (AudioSystemComponent.isAudioEnabled() ? "开启" : "关闭"));
            }
            mKeyPressed = true;
        } else {
            mKeyPressed = false;
        }
    }
    
    @Override
    public void render() {
        // 先调用父类的render方法，这会渲染所有组件（包括主菜单）
        super.render();
        
        // 检查主菜单状态，如果主菜单激活则不绘制游戏背景
        GameObject mainMenu = findGameObjectByTag("MainMenu");
        boolean menuActive = false;
        if (mainMenu != null && mainMenu.hasComponent(MainMenuComponent.class)) {
            MainMenuComponent menu = mainMenu.getComponent(MainMenuComponent.class);
            menuActive = menu.isActive();
        }
        
        // 只有在主菜单未激活时才绘制游戏背景
        if (!menuActive) {
            renderer.drawRect(0, 0, 800, 600, 0.1f, 0.1f, 0.2f, 1.0f);
        }
        
        // 渲染游戏对象
        for (GameObject obj : getGameObjects()) {
            if (!obj.isActive()) continue;
            if (obj.hasComponent(HealthComponent.class) && !obj.getComponent(HealthComponent.class).isAlive()) continue;
            
            // 渲染攻击范围效果
            if (obj.hasComponent(AttackRangeComponent.class) && obj.hasComponent(TransformComponent.class)) {
                AttackRangeComponent attackRange = obj.getComponent(AttackRangeComponent.class);
                TransformComponent transform = obj.getComponent(TransformComponent.class);
                if (attackRange.isActive()) {
                    // 根据对象类型选择颜色
                    if ("EnemyAttackRange".equals(obj.getTag())) {
                        // 敌人攻击范围 - 深红色
                        renderer.drawGradientCircle(
                            transform.getPosition().x,
                            transform.getPosition().y,
                            attackRange.getRadius(),
                            attackRange.getAlpha(),
                            0.8f, 0.0f, 0.0f // 深红色
                        );
                    } else if ("CannonballExplosion".equals(obj.getTag())) {
                        // 炮弹爆炸效果 - 橙红色
                        renderer.drawGradientCircle(
                            transform.getPosition().x,
                            transform.getPosition().y,
                            attackRange.getRadius(),
                            attackRange.getAlpha(),
                            1.0f, 0.5f, 0.0f // 橙红色
                        );
                    } else if ("BombExplosion".equals(obj.getTag())) {
                        // Boss炸弹爆炸效果 - 深红色
                        renderer.drawGradientCircle(
                            transform.getPosition().x,
                            transform.getPosition().y,
                            attackRange.getRadius(),
                            attackRange.getAlpha(),
                            1.0f, 0.0f, 0.0f // 深红色
                        );
                    } else {
                        // 玩家攻击范围 - 亮红色
                        renderer.drawGradientCircle(
                            transform.getPosition().x,
                            transform.getPosition().y,
                            attackRange.getRadius(),
                            attackRange.getAlpha(),
                            1.0f, 0.0f, 0.0f // 亮红色
                        );
                    }
                }
            }
            
            // 渲染精灵
            if (obj.hasComponent(SpriteComponent.class) && obj.hasComponent(TransformComponent.class)) {
                SpriteComponent sprite = obj.getComponent(SpriteComponent.class);
                TransformComponent transform = obj.getComponent(TransformComponent.class);
                if (sprite.getImage() != null) {
                    renderer.drawImage(
                        sprite.getImage(), 
                        transform.getPosition().x - sprite.getWidth() / 2, 
                        transform.getPosition().y - sprite.getHeight() / 2,
                        sprite.getWidth(),
                        sprite.getHeight()
                    );
                }
            }
            
            // 渲染粒子系统
            if (obj.hasComponent(ParticleSystemComponent.class)) {
                ParticleSystemComponent particleSystem = obj.getComponent(ParticleSystemComponent.class);
                renderer.drawParticles(particleSystem);
            }
        }
        
        // 渲染UI元素
        renderHealthBars();
        renderUI();
    }
    
    private void renderUI() {
        // 检查是否有玩家对象，如果没有则不显示游戏UI
        GameObject playerObj = findGameObjectByTag("Player");
        if (playerObj == null) {
            return; // 没有玩家对象，不显示游戏UI
        }
        
        // UI区域定义 - 左侧独立UI栏
        int gameAreaWidth = 800; // 游戏画面区域宽度
        int uiStartX = gameAreaWidth + 10; // UI栏起始位置
        int uiWidth = 280; // UI栏宽度
        int lineHeight = 45; // 进一步增加行高，避免文字和进度条重叠
        int currentY = 20;
        
        // 绘制UI背景区域
        renderer.drawRect(uiStartX - 5, currentY - 10, uiWidth + 10, 550, 0.0f, 0.0f, 0.0f, 0.3f);
        
        // 游戏时间进度条
        float timeProgress = gameLogic.getGameTimer() / gameLogic.getGameDuration();
        renderer.drawText("时间: " + String.format("%.1f", gameLogic.getGameTimer()) + "s", uiStartX, currentY, 1.0f, 1.0f, 1.0f, 1.0f);
        renderer.drawProgressBar(uiStartX, currentY + 18, uiWidth - 20, 15, timeProgress, 0.0f, 1.0f, 0.0f, 1.0f);
        currentY += 50; // 进一步增加间距
        
        // 近战攻击冷却进度条
        if (playerObj instanceof Player) {
            Player player = (Player) playerObj;
            float meleeProgress = player.getMeleeCooldownProgress();
            renderer.drawText("近战攻击 (J)", uiStartX, currentY, 1.0f, 1.0f, 1.0f, 1.0f);
            renderer.drawProgressBar(uiStartX, currentY + 18, uiWidth - 20, 12, meleeProgress, 1.0f, 0.5f, 0.0f, 1.0f);
            currentY += lineHeight;
            
            // 远程攻击冷却进度条
            float rangeProgress = player.getRangeCooldownProgress();
            renderer.drawText("远程攻击 (U)", uiStartX, currentY, 1.0f, 1.0f, 1.0f, 1.0f);
            renderer.drawProgressBar(uiStartX, currentY + 18, uiWidth - 20, 12, rangeProgress, 0.0f, 0.5f, 1.0f, 1.0f);
            currentY += lineHeight;
            
            // 炮弹攻击冷却进度条
            float cannonProgress = player.getCannonCooldownProgress();
            renderer.drawText("炮弹攻击 (I)", uiStartX, currentY, 1.0f, 1.0f, 1.0f, 1.0f);
            renderer.drawProgressBar(uiStartX, currentY + 18, uiWidth - 20, 12, cannonProgress, 1.0f, 0.0f, 0.0f, 1.0f);
            currentY += lineHeight;
        }
        
        // 分隔线
        renderer.drawLine(uiStartX, currentY, uiStartX + uiWidth - 20, currentY, 0.5f, 0.5f, 0.5f, 1.0f);
        currentY += 15;
        
        // 音效状态显示
        String audioStatus = AudioSystemComponent.isAudioEnabled() ? "音效: 开启" : "音效: 关闭";
        float audioR = AudioSystemComponent.isAudioEnabled() ? 0.0f : 1.0f;
        float audioG = AudioSystemComponent.isAudioEnabled() ? 1.0f : 0.0f;
        renderer.drawText(audioStatus + " (M)", uiStartX, currentY, audioR, audioG, 0.0f, 1.0f);
        currentY += lineHeight;
        
        // 背景音乐状态显示
        String bgmStatus = BackgroundMusicComponent.isBgmEnabled() ? "背景音乐: 开启" : "背景音乐: 关闭";
        float bgmR = BackgroundMusicComponent.isBgmEnabled() ? 0.0f : 1.0f;
        float bgmG = BackgroundMusicComponent.isBgmEnabled() ? 1.0f : 0.0f;
        renderer.drawText(bgmStatus + " (N)", uiStartX, currentY, bgmR, bgmG, 0.0f, 1.0f);
        currentY += lineHeight;
        
        // 暂停游戏提示
        String pauseStatus = gameLogic.isGamePaused() ? "游戏已暂停 (P)" : "暂停游戏 (P)";
        float pauseR = gameLogic.isGamePaused() ? 1.0f : 0.0f;
        float pauseG = gameLogic.isGamePaused() ? 0.0f : 1.0f;
        renderer.drawText(pauseStatus, uiStartX, currentY, pauseR, pauseG, 0.0f, 1.0f);
        currentY += lineHeight;
        
        // 保存游戏提示（仅在暂停时显示）
        if (gameLogic.isGamePaused()) {
            renderer.drawText("保存游戏 (F12)", uiStartX, currentY, 1.0f, 1.0f, 0.0f, 1.0f);
            currentY += lineHeight;
        }
        
        // 移动控制提示
        renderer.drawText("移动: WASD", uiStartX, currentY, 0.8f, 0.8f, 0.8f, 1.0f);
        currentY += lineHeight;
        
        // 游戏状态显示（在游戏画面中央）
        if (gameLogic.isGameEnded()) {
            int gameCenterX = 400; // 游戏画面中心X坐标
            int gameCenterY = 300; // 游戏画面中心Y坐标
            
            if (gameLogic.isGameWon()) {
                renderer.drawText("胜利！", gameCenterX - 30, gameCenterY, 0.0f, 1.0f, 0.0f, 1.0f);
            } else {
                renderer.drawText("失败！", gameCenterX - 30, gameCenterY, 1.0f, 0.0f, 0.0f, 1.0f);
            }
        }
    }

    private void renderHealthBars() {
        for (GameObject obj : getGameObjects()) {
            if (!obj.isActive()) continue;
            if (obj.hasComponent(HealthComponent.class)) {
                HealthComponent health = obj.getComponent(HealthComponent.class);
                if (!health.isAlive()) continue;
                TransformComponent transform = obj.getComponent(TransformComponent.class);
                if (health != null && transform != null) {
                    float x = transform.getPosition().x - 15;
                    float y = transform.getPosition().y - 30; // Move it up a bit
                    float maxWidth = 30;
                    float currentWidth = maxWidth * ((float)health.getHealth() / health.getMaxHealth());
                    renderer.drawRect(x, y, maxWidth, 5, 0.5f, 0.0f, 0.0f, 1.0f);
                    renderer.drawRect(x, y, currentWidth, 5, 0.0f, 1.0f, 0.0f, 1.0f);
                }
            }
        }
    }
    
    private void createPlayer() {
        Player player = CharacterFactory.createPlayer(new Vector2(400, 300));
        addGameObject(player);
    }
    
    private void createMinions(int count) {
        for (int i = 0; i < count; i++) createMinion();
    }
    
    private void createMinion() {
        Vector2 position = getRandomSpawnPosition();
        Minion minion = CharacterFactory.createMinion(position);
        addGameObject(minion);
    }
    

    /**
     * 获取随机生成位置（在屏幕边缘）
     */
    private Vector2 getRandomSpawnPosition() {
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
     * 开始播放背景音乐
     */
    private void startBackgroundMusic() {
        // 延迟一小段时间确保音频系统完全初始化
        new Thread(() -> {
            try {
                Thread.sleep(100); // 延迟100毫秒
                GameObject bgmSystem = findGameObjectByTag("BackgroundMusic");
                if (bgmSystem != null && bgmSystem.hasComponent(BackgroundMusicComponent.class)) {
                    BackgroundMusicComponent bgm = bgmSystem.getComponent(BackgroundMusicComponent.class);
                    bgm.playMusic();
                }
            } catch (InterruptedException e) {
                System.out.println("背景音乐启动延迟被中断: " + e.getMessage());
            }
        }).start();
    }
    
    // MainMenuComponent.GameScene接口实现
    @Override
    public void startNewGame() {
        System.out.println("开始新游戏");
        
        // 重置游戏状态
        gameLogic.resetGame();
        
        // 清除现有游戏对象（除了系统对象）
        getGameObjects().removeIf(obj -> 
            !"AudioSystem".equals(obj.getTag()) && 
            !"BackgroundMusic".equals(obj.getTag()) && 
            !"SaveSystem".equals(obj.getTag()) && 
            !"MainMenu".equals(obj.getTag())
        );
        
        // 创建新游戏对象
        createPlayer();
        createMinions(enemySpawnNumber);
        
        // 隐藏主菜单
        GameObject mainMenu = findGameObjectByTag("MainMenu");
        if (mainMenu != null && mainMenu.hasComponent(MainMenuComponent.class)) {
            MainMenuComponent menu = mainMenu.getComponent(MainMenuComponent.class);
            menu.setActive(false);
        }
    }
    
    /**
     * 处理P键暂停输入
     */
    private void handlePauseInput() {
        InputManager inputManager = InputManager.getInstance();
        
        // 检查P键是否被按下（按下和弹出触发一次）
        if (inputManager.isKeyPressed(80)) { // P键
            if (!pKeyPressed) {
                pKeyPressed = true;
                // 切换暂停状态
                gameLogic.setGamePaused(!gameLogic.isGamePaused());
                setPaused(gameLogic.isGamePaused()); // 同步场景暂停状态
                if (gameLogic.isGamePaused()) {
                    System.out.println("游戏已暂停");
                } else {
                    System.out.println("游戏已恢复");
                }
            }
        } else {
            pKeyPressed = false;
        }
    }
    
    @Override
    public void loadGame() {
        System.out.println("加载游戏存档");
        
        // 重置游戏状态
        gameLogic.resetGame();
        
        GameObject loadSystem = findGameObjectByTag("LoadSystem");
        if (loadSystem != null && loadSystem.hasComponent(LoadSystemComponent.class)) {
            LoadSystemComponent load = loadSystem.getComponent(LoadSystemComponent.class);
            boolean success = load.loadGame(this);
            
            if (success) {
                // 隐藏主菜单
                GameObject mainMenu = findGameObjectByTag("MainMenu");
                if (mainMenu != null && mainMenu.hasComponent(MainMenuComponent.class)) {
                    MainMenuComponent menu = mainMenu.getComponent(MainMenuComponent.class);
                    menu.setActive(false);
                }
            } else {
                System.out.println("加载存档失败");
            }
        }
    }
    
    @Override
    public void loadGameFromFile(String filePath) {
        System.out.println("从文件加载游戏存档: " + filePath);
        
        // 重置游戏状态
        gameLogic.resetGame();
        
        GameObject loadSystem = findGameObjectByTag("LoadSystem");
        if (loadSystem != null && loadSystem.hasComponent(LoadSystemComponent.class)) {
            LoadSystemComponent load = loadSystem.getComponent(LoadSystemComponent.class);
            boolean success = load.loadGameFromFile(this, filePath);
            
            if (success) {
                // 隐藏主菜单
                GameObject mainMenu = findGameObjectByTag("MainMenu");
                if (mainMenu != null && mainMenu.hasComponent(MainMenuComponent.class)) {
                    MainMenuComponent menu = mainMenu.getComponent(MainMenuComponent.class);
                    menu.setActive(false);
                }
            } else {
                System.out.println("从文件加载存档失败");
            }
        }
    }
    
    /**
     * 保存游戏
     */
    public void saveGame() {
        // 记录当前暂停状态
        boolean wasPaused = gameLogic.isGamePaused();
        
        // 如果游戏正在运行，使用P键逻辑暂停
        if (!wasPaused) {
            gameLogic.setGamePaused(true);
            setPaused(true);
            System.out.println("保存前暂停游戏");
        }
        
        try {
            // 执行保存操作
            GameObject saveSystem = findGameObjectByTag("SaveSystem");
            if (saveSystem != null && saveSystem.hasComponent(SaveSystemComponent.class)) {
                SaveSystemComponent save = saveSystem.getComponent(SaveSystemComponent.class);
                save.saveGame(this);
                
                // 如果之前未暂停，等待一段时间后恢复
                if (!wasPaused) {
                    // 使用新线程避免阻塞UI
                    new Thread(() -> {
                        try {
                            Thread.sleep(500); // 等待500ms防止冲突
                            gameLogic.setGamePaused(false);
                            setPaused(false);
                            System.out.println("保存后恢复游戏");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            System.out.println("保存过程中发生错误: " + e.getMessage());
            // 如果发生错误且之前未暂停，也要恢复游戏
            if (!wasPaused) {
                gameLogic.setGamePaused(false);
                setPaused(false);
                System.out.println("保存失败，恢复游戏");
            }
        }
    }
}
