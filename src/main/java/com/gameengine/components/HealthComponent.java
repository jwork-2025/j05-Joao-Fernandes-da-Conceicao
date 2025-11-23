package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;

public class HealthComponent extends Component<HealthComponent> {
    private int maxHealth;
    private int currentHealth;

    public HealthComponent(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    @Override
    public void initialize() {
        // 不需要初始化
    }

    @Override
    public void update(float deltaTime) {
        // HealthComponent不执行更新逻辑
    }

    @Override
    public void render() {
        // HealthComponent不执行渲染逻辑
    }

    public void takeDamage(int amount) {
        currentHealth -= amount;
        if (currentHealth < 0) {
            currentHealth = 0;
        }
        
        // 触发伤害粒子效果
        triggerDamageEffect();
    }
    
    private void triggerDamageEffect() {
        GameObject owner = getOwner();
        if (owner != null) {
            TransformComponent transform = owner.getComponent(TransformComponent.class);
            if (transform != null) {
                // 获取或创建粒子系统组件
                ParticleSystemComponent particleSystem = owner.getComponent(ParticleSystemComponent.class);
                if (particleSystem == null) {
                    particleSystem = new ParticleSystemComponent();
                    owner.addComponent(particleSystem);
                }
                
                // 创建伤害粒子效果
                Vector2 position = transform.getPosition();
                particleSystem.createDamageEffect(position, 8); // 8个粒子
            }
        }
    }

    public boolean isAlive() {
        return currentHealth > 0;
    }

    public int getHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }
    
    public void setHealth(int health) {
        this.currentHealth = health;
        if (this.currentHealth > this.maxHealth) {
            this.currentHealth = this.maxHealth;
        }
    }
}
