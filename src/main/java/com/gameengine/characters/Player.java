package com.gameengine.characters;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;
import com.gameengine.characters.projectiles.Cannonball;

/**
 * 玩家角色类
 */
public class Player extends GameObject {
    private static final float MELEE_COOLDOWN = 0.5f;
    private static final float RANGE_COOLDOWN = 0.8f;
    private static final float CANNON_COOLDOWN = 3.0f;

    // 渲染相关常量
    private static final int SPRITE_WIDTH = 40;
    private static final int SPRITE_HEIGHT = 40;
    
    // 攻击相关常量
    private static final float MELEE_RANGE = 60.0f;
    private static final int MELEE_DAMAGE = 30;
    private static final float MELEE_ATTACK_DURATION = 0.3f;
    
    // 血量
    private static final int HEALTH = 200;

    // 物理量
    private static final float MASS = 1.0f;
    private static final float FRICTION = 0.9f;

    private float meleeCooldownTimer = 0.0f;
    private float rangeCooldownTimer = 0.0f;
    private float cannonCooldownTimer = 0.0f;
    
    public Player(Vector2 position) {
        super("Player", "Player");
        
        // 添加组件
        addComponent(new TransformComponent(position));
        PhysicsComponent physics = addComponent(new PhysicsComponent(MASS));
        physics.setFriction(FRICTION); // 降低摩擦力
        addComponent(new HealthComponent(HEALTH));
        addComponent(new SpriteComponent("resources/player.png", SPRITE_WIDTH, SPRITE_HEIGHT));
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        // 更新攻击冷却时间
        if (meleeCooldownTimer > 0) {
            meleeCooldownTimer -= deltaTime;
        }
        if (rangeCooldownTimer > 0) {
            rangeCooldownTimer -= deltaTime;
        }
        if (cannonCooldownTimer > 0) {
            cannonCooldownTimer -= deltaTime;
        }
    }
    
    /**
     * 检查近程攻击是否可用
     */
    public boolean canMeleeAttack() {
        return meleeCooldownTimer <= 0;
    }
    
    /**
     * 检查远程攻击是否可用
     */
    public boolean canRangeAttack() {
        return rangeCooldownTimer <= 0;
    }
    
    /**
     * 执行近程攻击
     */
    public void performMeleeAttack() {
        if (canMeleeAttack()) {
            meleeCooldownTimer = MELEE_COOLDOWN;
        }
    }
    
    /**
     * 执行远程攻击
     */
    public void performRangeAttack() {
        if (canRangeAttack()) {
            rangeCooldownTimer = RANGE_COOLDOWN;
        }
    }
    
    /**
     * 获取近程攻击冷却进度 (0-1)
     */
    public float getMeleeCooldownProgress() {
        return meleeCooldownTimer > 0 ? 1.0f - (meleeCooldownTimer / MELEE_COOLDOWN) : 1.0f;
    }
    
    /**
     * 获取远程攻击冷却进度 (0-1)
     */
    public float getRangeCooldownProgress() {
        return rangeCooldownTimer > 0 ? 1.0f - (rangeCooldownTimer / RANGE_COOLDOWN) : 1.0f;
    }
    
    /**
     * 检查炮弹攻击是否可用
     */
    public boolean canCannonAttack() {
        return cannonCooldownTimer <= 0;
    }
    
    /**
     * 执行炮弹攻击
     */
    public void performCannonAttack() {
        if (canCannonAttack()) {
            cannonCooldownTimer = CANNON_COOLDOWN;
        }
    }
    
    /**
     * 获取炮弹攻击冷却进度 (0-1)
     */
    public float getCannonCooldownProgress() {
        return cannonCooldownTimer > 0 ? 1.0f - (cannonCooldownTimer / CANNON_COOLDOWN) : 1.0f;
    }
    
    // 攻击相关常量获取方法
    public static float getMeleeRange() { return MELEE_RANGE; }
    public static int getMeleeDamage() { return MELEE_DAMAGE; }
    public static float getMeleeAttackDuration() { return MELEE_ATTACK_DURATION; }
    
    // 冷却时间获取方法
    public float getMeleeCooldownTimer() { return meleeCooldownTimer; }
    public float getRangeCooldownTimer() { return rangeCooldownTimer; }
    public float getCannonCooldownTimer() { return cannonCooldownTimer; }
    
    // 冷却时间设置方法
    public void setMeleeCooldownTimer(float timer) { this.meleeCooldownTimer = timer; }
    public void setRangeCooldownTimer(float timer) { this.rangeCooldownTimer = timer; }
    public void setCannonCooldownTimer(float timer) { this.cannonCooldownTimer = timer; }
}
