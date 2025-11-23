package com.gameengine.logic;

import com.gameengine.components.PhysicsComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.core.GameObject;
import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

import java.util.List;

/**
 * 物理管理器
 * 负责处理物理计算、移动和边界检查
 */
public class PhysicsManager {
    private Scene scene;
    private InputManager inputManager;
    
    public PhysicsManager(Scene scene) {
        this.scene = scene;
        this.inputManager = InputManager.getInstance();
    }
    
    /**
     * 更新物理系统
     */
    public void update() {
        handlePlayerInput();
        updatePhysics();
    }
    
    /**
     * 处理玩家移动输入
     */
    public void handlePlayerInput() {
        GameObject player = scene.findGameObjectByTag("Player");
        if (player == null) return;
        
        TransformComponent transform = player.getComponent(TransformComponent.class);
        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
        
        if (transform == null || physics == null) return;
        
        Vector2 movement = new Vector2();
        
        if (inputManager.isKeyPressed(87) || inputManager.isKeyPressed(38)) { // W或上箭头
            movement.y -= 1;
        }
        if (inputManager.isKeyPressed(83) || inputManager.isKeyPressed(40)) { // S或下箭头
            movement.y += 1;
        }
        if (inputManager.isKeyPressed(65) || inputManager.isKeyPressed(37)) { // A或左箭头
            movement.x -= 1;
        }
        if (inputManager.isKeyPressed(68) || inputManager.isKeyPressed(39)) { // D或右箭头
            movement.x += 1;
        }
        
        if (movement.magnitude() > 0) {
            movement = movement.normalize().multiply(500); // 力量足够大
            physics.addForce(movement);
        }
    }
    
    /**
     * 更新物理系统
     */
    public void updatePhysics() {
        List<PhysicsComponent> physicsComponents = scene.getComponents(PhysicsComponent.class);
        for (PhysicsComponent physics : physicsComponents) {
            TransformComponent transform = physics.getOwner().getComponent(TransformComponent.class);
            if (transform != null) {
                Vector2 pos = transform.getPosition();
                
                // 边界检查 (让物体停在边界)
                if (pos.x < 0) { pos.x = 0; physics.getVelocity().x = 0; }
                if (pos.y < 0) { pos.y = 0; physics.getVelocity().y = 0; }
                if (pos.x > 800 - 20) { pos.x = 800 - 20; physics.getVelocity().x = 0; }
                if (pos.y > 600 - 20) { pos.y = 600 - 20; physics.getVelocity().y = 0; }
                transform.setPosition(pos);
            }
        }
    }
}
