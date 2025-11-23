package com.gameengine.characters.projectiles;

import com.gameengine.math.Vector2;

/**
 * 子弹类 - Projectile的具体实现
 * 快速、低伤害的投射物，玩家和敌人都可以使用
 */
public class Bullet extends Projectile {
    private static final float BULLET_SPEED = 250.0f;
    private static final float BULLET_LIFETIME = 3.0f;
    private static final float BULLET_MASS = 0.2f;
    private static final int BULLET_DAMAGE = 15;
    private static final float BULLET_RANGE = 20.0f;
    private static final float BULLET_COOLDOWN = 0.8f;
    private static final int SPRITE_WIDTH = 15;
    private static final int SPRITE_HEIGHT = 15;
    public Bullet(Vector2 start, Vector2 target, String tag) {
        super(start, target, tag, 
              BULLET_SPEED, BULLET_LIFETIME, BULLET_MASS, 
              BULLET_DAMAGE, BULLET_RANGE);
    }
    
    @Override
    protected String getSpritePath() {
        return "resources/projectile.png";
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
        return BULLET_COOLDOWN;
    }
    
    @Override
    public boolean canBeUsedBy(String characterType) {
        // 子弹可以被玩家和敌人使用
        return "Player".equals(characterType) || "Enemy".equals(characterType);
    }
    
    /**
     * 获取子弹的静态属性
     */
    public static float getBulletSpeed() {
        return BULLET_SPEED;
    }
    
    public static float getBulletLifetime() {
        return BULLET_LIFETIME;
    }
    
    public static float getBulletMass() {
        return BULLET_MASS;
    }
    
    public static int getBulletDamage() {
        return BULLET_DAMAGE;
    }
    
    public static float getBulletRange() {
        return BULLET_RANGE;
    }
    
    public static float getBulletCooldown() {
        return BULLET_COOLDOWN;
    }
}
