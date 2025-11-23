package com.gameengine.characters.enemies;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.scene.Scene;
import com.gameengine.math.Vector2;

import java.util.Random;

/**
 * 敌人抽象基类
 */
public abstract class Enemy extends GameObject {
    protected static final float RANGED_ATTACK_COOLDOWN = 2.0f;
    protected static final float MELEE_ATTACK_COOLDOWN = 1.5f;
    protected static final float MELEE_RANGE = 40.0f;
    protected static final int MELEE_DAMAGE = 15;
    protected static final float FRICTION = 0.95f;
    
    protected float rangedAttackCooldown;
    protected float meleeAttackCooldown;
    protected Random random;
    
    public Enemy(Vector2 position, String name, String tag) {
        super(name, tag);
        
        // 添加基础组件
        addComponent(new TransformComponent(position));
        addComponent(new SpriteComponent(getSpritePath(), getSpriteWidth(), getSpriteHeight()));
        PhysicsComponent physics = addComponent(new PhysicsComponent(getMass()));
        physics.setFriction(FRICTION);
        addComponent(new HealthComponent(getHealth()));
        
        // 初始化攻击冷却
        this.rangedAttackCooldown = RANGED_ATTACK_COOLDOWN;
        this.meleeAttackCooldown = MELEE_ATTACK_COOLDOWN;
        this.random = new Random();
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        if (!isActive()) return;
        if (hasComponent(HealthComponent.class) && !getComponent(HealthComponent.class).isAlive()) return;
        
        Scene currentScene = getScene();
        if (currentScene == null) return;
        
        GameObject player = currentScene.findGameObjectByTag("Player");
        if (player == null) return;

        TransformComponent myTransform = getComponent(TransformComponent.class);
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        PhysicsComponent myPhysics = getComponent(PhysicsComponent.class);

        if (myTransform != null && playerTransform != null && myPhysics != null) {
            Vector2 direction = playerTransform.getPosition().subtract(myTransform.getPosition());
            float distance = direction.magnitude();
            
            // 移动逻辑
            if (distance > 0) {
                myPhysics.addForce(direction.normalize().multiply(getMoveForce()));
            }
            
            // 更新攻击冷却时间
            rangedAttackCooldown -= deltaTime;
            meleeAttackCooldown -= deltaTime;
            
            // 攻击逻辑
            handleAttacks(distance);
        }
    }
    
    /**
     * 处理攻击逻辑，由子类实现
     */
    protected abstract void handleAttacks(float distanceToPlayer);
    
    /**
     * 检查近程攻击是否可用
     */
    public boolean canMeleeAttack() {
        return meleeAttackCooldown <= 0;
    }
    
    /**
     * 检查远程攻击是否可用
     */
    public boolean canRangeAttack() {
        return rangedAttackCooldown <= 0;
    }
    
    /**
     * 获取近程攻击范围
     */
    public float getMeleeRange() {
        return MELEE_RANGE;
    }
    
    /**
     * 获取近程攻击伤害
     */
    public int getMeleeDamage() {
        return MELEE_DAMAGE;
    }

    /**
     * 获取近程攻击范围的静态方法
     */
    public static float getMeleeRangeStatic() {
        return MELEE_RANGE;
    }
    
    public static int getMeleeDamageStatic() {
        return MELEE_DAMAGE;
    }

    // 抽象方法，由子类实现
    protected abstract String getSpritePath();
    protected abstract int getSpriteWidth();
    protected abstract int getSpriteHeight();
    protected abstract float getMass();
    protected abstract int getHealth();
    protected abstract float getMoveForce();
}
