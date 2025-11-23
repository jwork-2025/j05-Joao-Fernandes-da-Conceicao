package com.gameengine.example;

import com.gameengine.core.GameEngine;
import com.gameengine.example.scene.GameScene;

public class GameExample {
    public static void main(String[] args) {
        System.out.println("启动游戏引擎...");
        
        try {
            GameEngine engine = new GameEngine(1100, 600, "葫芦娃大战妖精"); // 增加300px宽度用于UI栏
            
            // 游戏参数配置
            float enemySpawnInterval = 10.0f;  // 敌人生成间隔（秒）
            int enemySpawnNumber = 5;          // 每次生成的敌人数量
            float gameDuration = 60.0f;       // 游戏时长（秒）
            
            GameScene gameScene = new GameScene(engine, enemySpawnInterval, enemySpawnNumber, gameDuration);
            engine.setScene(gameScene);
            engine.run();
        } catch (Exception e) {
            System.err.println("游戏运行出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("游戏结束");
    }
}
