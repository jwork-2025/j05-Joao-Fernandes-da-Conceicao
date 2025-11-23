package com.gameengine.characters.enemies;

import com.gameengine.math.Vector2;

/**
 * Boss类 - Enemy的具体实现
 * 强大的敌人单位，具有更高的血量，会发射炸弹
 */
public class Boss extends Enemy {
    private static final float BOSS_MASS = 1.5f;
    private static final int BOSS_HEALTH = 150;
    private static final float BOSS_MOVE_FORCE = 30.0f;
    private static final float BOSS_BOMB_COOLDOWN = 3.0f;
    private static final int SPRITE_WIDTH = 60;
    private static final int SPRITE_HEIGHT = 60;
    private float bombCooldown = BOSS_BOMB_COOLDOWN;
    
    public Boss(Vector2 position) {
        super(position, "Boss", "Boss");
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        // 更新炸弹攻击冷却
        if (bombCooldown > 0) {
            bombCooldown -= deltaTime;
        }
    }
    
    @Override
    protected void handleAttacks(float distanceToPlayer) {
        // Boss优先使用炸弹攻击
        if (bombCooldown <= 0) {
            setTag("BossNeedsBombAttack");
            bombCooldown = BOSS_BOMB_COOLDOWN;
        }
        // 近程攻击判定（优先级第二）
        else if (distanceToPlayer <= MELEE_RANGE && meleeAttackCooldown <= 0) {
            setTag("EnemyNeedsMeleeAttack");
            meleeAttackCooldown = MELEE_ATTACK_COOLDOWN;
        }
        // 远程攻击判定（优先级最低）
        else if (rangedAttackCooldown <= 0) {
            setTag("EnemyNeedsRangeAttack");
            rangedAttackCooldown = RANGED_ATTACK_COOLDOWN + random.nextFloat() * 2;
        }
    }
    
    @Override
    protected String getSpritePath() {
        return "resources/boss.png"; // 假设有boss图片
    }
    
    @Override
    protected int getSpriteWidth() {
        return SPRITE_WIDTH; // Boss更大
    }
    
    @Override
    protected int getSpriteHeight() {
        return SPRITE_HEIGHT; // Boss更大
    }
    
    @Override
    protected float getMass() {
        return BOSS_MASS;
    }
    
    @Override
    protected int getHealth() {
        return BOSS_HEALTH;
    }
    
    @Override
    protected float getMoveForce() {
        return BOSS_MOVE_FORCE;
    }
    
    /**
     * 检查炸弹攻击是否可用
     */
    public boolean canBombAttack() {
        return bombCooldown <= 0;
    }
    
    /**
     * 获取炸弹攻击冷却进度
     */
    public float getBombCooldownProgress() {
        return bombCooldown > 0 ? 1.0f - (bombCooldown / BOSS_BOMB_COOLDOWN) : 1.0f;
    }
    
    // 静态方法获取属性
    public static float getBossMass() { return BOSS_MASS; }
    public static int getBossHealth() { return BOSS_HEALTH; }
    public static float getBossMoveForce() { return BOSS_MOVE_FORCE; }
    public static float getBossBombCooldown() { return BOSS_BOMB_COOLDOWN; }
}
