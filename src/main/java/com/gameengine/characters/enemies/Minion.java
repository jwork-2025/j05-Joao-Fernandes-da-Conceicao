package com.gameengine.characters.enemies;

import com.gameengine.math.Vector2;

/**
 * 小兵类 - Enemy的具体实现
 * 基础的敌人单位，具有近战和远程攻击能力
 */
public class Minion extends Enemy {
    private static final float MINION_MASS = 0.8f;
    private static final int MINION_HEALTH = 30;
    private static final float MINION_MOVE_FORCE = 50.0f;
    private static final int SPRITE_WIDTH = 40;
    private static final int SPRITE_HEIGHT = 40;
    
    public Minion(Vector2 position) {
        super(position, "Minion", "Minion");
    }
    
    @Override
    protected void handleAttacks(float distanceToPlayer) {
        // 近程攻击判定（优先级更高）
        if (distanceToPlayer <= MELEE_RANGE && meleeAttackCooldown <= 0) {
            // 标记需要近程攻击，由场景处理
            setTag("EnemyNeedsMeleeAttack");
            meleeAttackCooldown = MELEE_ATTACK_COOLDOWN; // 重置近程攻击冷却
        }
        // 远程攻击判定
        else if (rangedAttackCooldown <= 0) {
            // 标记需要远程攻击，由场景处理
            setTag("EnemyNeedsRangeAttack");
            rangedAttackCooldown = RANGED_ATTACK_COOLDOWN + random.nextFloat() * 2;
        }
    }
    
    @Override
    protected String getSpritePath() {
        return "resources/enemy.png";
    }
    
    @Override
    protected int getSpriteWidth() {
        return SPRITE_WIDTH;
    }
    
    @Override
    protected int getSpriteHeight() {
        return SPRITE_HEIGHT;
    }
    
    @Override
    protected float getMass() {
        return MINION_MASS;
    }
    
    @Override
    protected int getHealth() {
        return MINION_HEALTH;
    }
    
    @Override
    protected float getMoveForce() {
        return MINION_MOVE_FORCE;
    }
    
    // 静态方法获取属性
    public static float getMinionMass() { return MINION_MASS; }
    public static int getMinionHealth() { return MINION_HEALTH; }
    public static float getMinionMoveForce() { return MINION_MOVE_FORCE; }
}
