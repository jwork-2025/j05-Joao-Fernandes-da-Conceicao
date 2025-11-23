package com.gameengine.characters.projectiles;

import com.gameengine.math.Vector2;

/**
 * 炸弹类 - Projectile的具体实现
 * Boss发射的高伤害炸弹，具有爆炸效果
 */
public class Bomb extends Projectile {
    private static final float BOMB_SPEED = 150.0f;
    private static final float BOMB_LIFETIME = 4.0f;
    private static final float BOMB_MASS = 1.0f;
    private static final int BOMB_DAMAGE = 80;
    private static final float BOMB_SCAN_RANGE = 30.0f; // 扫描距离（碰撞检测范围）
    private static final float BOMB_ATTACK_RANGE = 100.0f; // 攻击距离（爆炸效果范围）
    private static final float BOMB_COOLDOWN = 3.0f;
    private static final int SPRITE_WIDTH = 20;
    private static final int SPRITE_HEIGHT = 20;
    
    // 爆炸效果相关常量
    private static final float BOMB_EXPLOSION_DURATION = 0.8f;
    private static final float BOMB_EXPLOSION_RADIUS = BOMB_ATTACK_RANGE; // 爆炸半径与攻击范围一致
    
    public Bomb(Vector2 start, Vector2 target, String tag) {
        super(start, target, tag, 
              BOMB_SPEED, BOMB_LIFETIME, BOMB_MASS, 
              BOMB_DAMAGE, BOMB_SCAN_RANGE); // 使用扫描距离作为碰撞检测范围
    }
    
    @Override
    protected String getSpritePath() {
        return "resources/bomb.png"; // 假设有炸弹图片
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
    public float getCooldownTime() {
        return BOMB_COOLDOWN;
    }
    
    @Override
    public boolean canBeUsedBy(String characterType) {
        return "Boss".equals(characterType); // 只有Boss可以使用炸弹
    }
    
    // 爆炸效果相关方法
    public static float getBombExplosionDuration() {
        return BOMB_EXPLOSION_DURATION;
    }
    
    public static float getBombExplosionRadius() {
        return BOMB_EXPLOSION_RADIUS;
    }
    
    // 静态方法获取属性
    public static float getBombSpeed() { return BOMB_SPEED; }
    public static float getBombLifetime() { return BOMB_LIFETIME; }
    public static float getBombMass() { return BOMB_MASS; }
    public static int getBombDamage() { return BOMB_DAMAGE; }
    public static float getBombRange() { return BOMB_SCAN_RANGE; } // 保持向后兼容，返回扫描距离
    public static float getBombScanRange() { return BOMB_SCAN_RANGE; } // 扫描距离（碰撞检测）
    public static float getBombAttackRange() { return BOMB_ATTACK_RANGE; } // 攻击距离（爆炸效果）
    public static float getBombCooldown() { return BOMB_COOLDOWN; }
}
