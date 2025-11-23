package com.gameengine.components;

import com.gameengine.core.Component;

/**
 * 攻击范围效果组件
 */
public class AttackRangeComponent extends Component<AttackRangeComponent> {
    private float duration;
    private float maxDuration;
    private float radius;
    private float maxRadius;
    private float alpha;
    
    public AttackRangeComponent(float duration, float radius) {
        this.maxDuration = duration;
        this.duration = duration;
        this.maxRadius = radius;
        this.radius = radius;
        this.alpha = 1.0f;
    }
    
    @Override
    public void initialize() {
        // 初始化攻击范围效果
    }
    
    @Override
    public void update(float deltaTime) {
        if (!enabled) return;
        
        duration -= deltaTime;
        
        if (duration <= 0) {
            // 效果结束，销毁对象
            if (getOwner() != null) {
                getOwner().destroy();
            }
            return;
        }
        
        // 计算当前进度 (1.0 到 0.0)
        float progress = duration / maxDuration;
        
        // 半径渐变：从最大半径逐渐缩小
        radius = maxRadius * progress;
        
        // 透明度渐变：从完全不透明到完全透明
        alpha = progress;
    }
    
    @Override
    public void render() {
        // 渲染逻辑在GameExample中处理
    }
    
    // Getters
    public float getRadius() {
        return radius;
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    public boolean isActive() {
        return duration > 0;
    }
}
