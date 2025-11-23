package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 粒子系统组件
 * 用于创建和管理粒子效果
 */
public class ParticleSystemComponent extends Component<ParticleSystemComponent> {
    private List<Particle> particles;
    private Random random;
    private boolean isActive;
    
    public ParticleSystemComponent() {
        this.particles = new ArrayList<>();
        this.random = new Random();
        this.isActive = false;
    }
    
    @Override
    public void initialize() {}
    
    @Override
    public void update(float deltaTime) {
        if (!isActive) return;
        
        // 更新所有粒子
        particles.removeIf(particle -> {
            particle.update(deltaTime);
            return particle.isDead();
        });
        
        // 如果没有粒子了，停用系统
        if (particles.isEmpty()) {
            isActive = false;
        }
    }
    
    @Override
    public void render() {}
    
    /**
     * 创建伤害粒子效果
     */
    public void createDamageEffect(Vector2 position, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            Particle particle = new Particle();
            particle.position = new Vector2(position.x, position.y);
            
            // 随机方向
            float angle = random.nextFloat() * 2 * (float) Math.PI;
            float speed = 50 + random.nextFloat() * 100; // 50-150 像素/秒
            particle.velocity = new Vector2(
                (float) Math.cos(angle) * speed,
                (float) Math.sin(angle) * speed
            );
            
            // 粒子属性
            particle.lifetime = 0.3f + random.nextFloat() * 0.4f; // 0.3-0.7秒
            particle.maxLifetime = particle.lifetime;
            particle.size = 2 + random.nextFloat() * 3; // 2-5像素
            particle.color = new float[]{1.0f, 0.0f, 0.0f, 1.0f}; // 红色
            
            particles.add(particle);
        }
        
        isActive = true;
    }
    
    /**
     * 获取所有粒子（用于渲染）
     */
    public List<Particle> getParticles() {
        return particles;
    }
    
    /**
     * 检查系统是否活跃
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * 粒子类
     */
    public static class Particle {
        public Vector2 position;
        public Vector2 velocity;
        public float lifetime;
        public float maxLifetime;
        public float size;
        public float[] color; // RGBA
        
        public void update(float deltaTime) {
            lifetime -= deltaTime;
            position = position.add(velocity.multiply(deltaTime));
            
            // 更新透明度（淡出效果）
            float alpha = lifetime / maxLifetime;
            color[3] = alpha;
        }
        
        public boolean isDead() {
            return lifetime <= 0;
        }
        
        public float getAlpha() {
            return color[3];
        }
        
        public float getSize() {
            return size * (lifetime / maxLifetime); // 粒子逐渐缩小
        }
    }
}
