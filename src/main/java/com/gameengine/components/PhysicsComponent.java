package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;

/**
 * 物理组件，处理物理运动
 */
public class PhysicsComponent extends Component<PhysicsComponent> {
    private float mass;
    private Vector2 velocity;
    private Vector2 acceleration;
    private float friction;
    private Vector2 force = new Vector2(); // 新增: 用于累加力
    
    public PhysicsComponent() {
        this.velocity = new Vector2();
        this.acceleration = new Vector2();
        this.mass = 1.0f;
        this.friction = 0.9f;
    }
    
    public PhysicsComponent(float mass) {
        this.mass = mass > 0 ? mass : 1.0f;
        this.velocity = new Vector2();
        this.acceleration = new Vector2();
        this.friction = 0.98f;
    }
    
    @Override
    public void initialize() {
        // 初始化物理组件
    }
    
    @Override
    public void update(float deltaTime) {
        if (!enabled) return;
        
        // 应用累加的力
        if (mass > 0) {
            acceleration = force.divide(mass);
        }
        
        // 更新速度和位置
        velocity = velocity.add(acceleration.multiply(deltaTime));
        velocity = velocity.multiply(friction); // 应用摩擦力

        TransformComponent transform = owner.getComponent(TransformComponent.class);
        if (transform != null) {
            Vector2 delta = velocity.multiply(deltaTime);
            // System.out.println("[DEBUG] velocity=" + velocity + ", delta=" + delta);
            transform.translate(delta);
        }

        // 重置力
        force.x = 0;
        force.y = 0;
    }
    
    @Override
    public void render() {
        // 物理组件不直接渲染
    }
    
    /**
     * 施加一个力
     */
    public void addForce(Vector2 force) {
        this.force = this.force.add(force);
    }
    
    /**
     * 应用力
     */
    public void applyForce(Vector2 force) {
        if (mass > 0) {
            acceleration = acceleration.add(force.multiply(1.0f / mass));
        }
    }
    
    /**
     * 应用冲量
     */
    public void applyImpulse(Vector2 impulse) {
        if (mass > 0) {
            velocity = velocity.add(impulse.multiply(1.0f / mass));
        }
    }
    
    /**
     * 设置速度
     */
    public void setVelocity(Vector2 velocity) {
        this.velocity = new Vector2(velocity);
    }
    
    /**
     * 设置速度
     */
    public void setVelocity(float x, float y) {
        this.velocity = new Vector2(x, y);
    }
    
    /**
     * 添加速度
     */
    public void addVelocity(Vector2 delta) {
        this.velocity = velocity.add(delta);
    }
    
    /**
     * 设置重力
     */
    public void setGravity(Vector2 gravity) {
        // This method is no longer used for gravity, but kept for consistency
        // The gravity vector is now managed by the force vector
    }
    
    /**
     * 启用/禁用重力
     */
    public void setUseGravity(boolean useGravity) {
        // This method is no longer used for gravity, but kept for consistency
        // The gravity vector is now managed by the force vector
    }
    
    /**
     * 设置摩擦力
     */
    public void setFriction(float friction) {
        this.friction = Math.max(0, Math.min(1, friction));
    }
    
    /**
     * 设置质量
     */
    public void setMass(float mass) {
        this.mass = Math.max(0.1f, mass);
    }
    
    // Getters
    public Vector2 getVelocity() {
        return new Vector2(velocity);
    }
    
    public Vector2 getAcceleration() {
        return new Vector2(acceleration);
    }
    
    public float getMass() {
        return mass;
    }
    
    public float getFriction() {
        return friction;
    }
    
    public boolean isUseGravity() {
        // This method is no longer used for gravity, but kept for consistency
        return false;
    }
    
    public Vector2 getGravity() {
        // This method is no longer used for gravity, but kept for consistency
        return new Vector2();
    }
}
