package com.gameengine.characters.projectiles;

import com.gameengine.math.Vector2;

/**
 * 炮弹类 - Projectile的具体实现
 * 高伤害、大范围的投射物，仅玩家可以使用
 */
public class Cannonball extends Projectile {
    private static final float CANNONBALL_SPEED = 200.0f;
    private static final float CANNONBALL_LIFETIME = 4.0f;
    private static final float CANNONBALL_MASS = 0.5f;
    private static final int CANNONBALL_DAMAGE = 50;
    private static final float CANNONBALL_SCAN_RANGE = 30.0f; // 扫描距离（碰撞检测范围）
    private static final float CANNONBALL_ATTACK_RANGE = 100.0f; // 攻击距离（爆炸效果范围）
    private static final float CANNONBALL_COOLDOWN = 3.0f;
    private static final int SPRITE_WIDTH = 25;
    private static final int SPRITE_HEIGHT = 25;
    // 爆炸效果相关常量
    private static final float CANNONBALL_EXPLOSION_DURATION = 0.5f;
    private static final float CANNONBALL_EXPLOSION_RADIUS = CANNONBALL_ATTACK_RANGE; // 爆炸半径与攻击距离一致
    
    public Cannonball(Vector2 start, Vector2 target, String tag) {
        super(start, target, tag, 
              CANNONBALL_SPEED, CANNONBALL_LIFETIME, CANNONBALL_MASS, 
              CANNONBALL_DAMAGE, CANNONBALL_SCAN_RANGE); // 使用扫描距离作为碰撞检测范围
    }
    
    @Override
    protected String getSpritePath() {
        return "resources/cannonball.png"; // 假设有炮弹图片
    }
    
    @Override
    protected int getSpriteWidth() {
        return SPRITE_WIDTH; // 比子弹大
    }
    
    @Override
    protected int getSpriteHeight() {
        return SPRITE_HEIGHT; // 比子弹大
    }
    
    @Override
    public float getCooldownTime() {
        return CANNONBALL_COOLDOWN;
    }
    
    @Override
    public boolean canBeUsedBy(String characterType) {
        // 炮弹仅玩家可以使用
        return "Player".equals(characterType);
    }
    
    /**
     * 获取炮弹的静态属性
     */
    public static float getCannonballSpeed() {
        return CANNONBALL_SPEED;
    }
    
    public static float getCannonballLifetime() {
        return CANNONBALL_LIFETIME;
    }
    
    public static float getCannonballMass() {
        return CANNONBALL_MASS;
    }
    
    public static int getCannonballDamage() {
        return CANNONBALL_DAMAGE;
    }
    
    public static float getCannonballRange() {
        return CANNONBALL_SCAN_RANGE; // 保持向后兼容，返回扫描距离
    }
    
    public static float getCannonballScanRange() {
        return CANNONBALL_SCAN_RANGE; // 扫描距离（碰撞检测）
    }
    
    public static float getCannonballAttackRange() {
        return CANNONBALL_ATTACK_RANGE; // 攻击距离（爆炸效果）
    }
    
    public static float getCannonballCooldown() {
        return CANNONBALL_COOLDOWN;
    }
    
    // 爆炸效果相关方法
    public static float getCannonballExplosionDuration() {
        return CANNONBALL_EXPLOSION_DURATION;
    }
    
    public static float getCannonballExplosionRadius() {
        return CANNONBALL_EXPLOSION_RADIUS;
    }
}
