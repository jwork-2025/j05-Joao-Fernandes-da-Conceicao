package com.gameengine.characters.projectiles;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;

/**
 * 投射物抽象类
 * 定义了所有投射物的通用属性和行为
 */
public abstract class Projectile extends GameObject {
    protected float speed;
    protected float lifetime;
    protected float mass;
    protected int damage;
    protected float range;
    
    /**
     * 构造函数
     * @param start 起始位置
     * @param target 目标位置
     * @param tag 标签
     * @param speed 速度
     * @param lifetime 生命周期
     * @param mass 质量
     * @param damage 伤害
     * @param range 范围
     */
    public Projectile(Vector2 start, Vector2 target, String tag, 
                     float speed, float lifetime, float mass, 
                     int damage, float range) {
        super("Projectile", tag);
        
        this.speed = speed;
        this.lifetime = lifetime;
        this.mass = mass;
        this.damage = damage;
        this.range = range;
        
        // 添加通用组件
        addComponent(new TransformComponent(new Vector2(start.x, start.y)));
        addComponent(new SpriteComponent(getSpritePath(), getSpriteWidth(), getSpriteHeight()));
        
        // 设置物理属性
        PhysicsComponent physics = addComponent(new PhysicsComponent(mass));
        Vector2 direction = target.subtract(start).normalize();
        physics.setVelocity(direction.multiply(speed));
        physics.setFriction(1.0f); // 无摩擦力，保持匀速
        
        // 设置生命周期
        addComponent(new LifetimeComponent(lifetime));
    }
    
    /**
     * 获取伤害值
     */
    public int getDamage() {
        return damage;
    }
    
    /**
     * 获取范围
     */
    public float getRange() {
        return range;
    }
    
    /**
     * 获取速度
     */
    public float getSpeed() {
        return speed;
    }
    
    /**
     * 获取生命周期
     */
    public float getLifetime() {
        return lifetime;
    }
    
    /**
     * 获取质量
     */
    public float getMass() {
        return mass;
    }
    
    /**
     * 抽象方法：获取精灵图片路径
     */
    protected abstract String getSpritePath();
    
    /**
     * 抽象方法：获取精灵宽度
     */
    protected abstract int getSpriteWidth();
    
    /**
     * 抽象方法：获取精灵高度
     */
    protected abstract int getSpriteHeight();
    
    /**
     * 抽象方法：获取冷却时间
     */
    public abstract float getCooldownTime();
    
    /**
     * 抽象方法：检查是否可以被指定角色使用
     */
    public abstract boolean canBeUsedBy(String characterType);
}
