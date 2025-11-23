package com.gameengine.components;

import com.gameengine.core.Component;

public class LifetimeComponent extends Component<LifetimeComponent> {
    private float lifetime;

    public LifetimeComponent(float lifetime) {
        this.lifetime = lifetime;
    }

    @Override
    public void initialize() {}

    @Override
    public void render() {}

    @Override
    public void update(float deltaTime) {
        lifetime -= deltaTime;
        if (lifetime <= 0 && getOwner() != null) {
            getOwner().destroy();
        }
    }
    
    public float getLifetime() {
        return lifetime;
    }
    
    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }
    
    public float getRemainingTime() {
        return lifetime;
    }
    
    public void setRemainingTime(float remainingTime) {
        this.lifetime = remainingTime;
    }
}
