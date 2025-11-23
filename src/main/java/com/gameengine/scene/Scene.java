package com.gameengine.scene;

import com.gameengine.core.GameObject;
import com.gameengine.core.Component;
// 移除具体游戏逻辑的import
import java.util.*;
import java.util.stream.Collectors;

/**
 * 场景类，管理游戏对象和组件
 */
public class Scene {
    private String name;
    private List<GameObject> gameObjects;
    private List<GameObject> objectsToAdd;
    private List<GameObject> objectsToRemove;
    private boolean initialized;
    private boolean paused = false; // 场景暂停状态
    // 移除未使用的组件索引
    
    public Scene(String name) {
        this.name = name;
        this.gameObjects = new ArrayList<>();
        this.objectsToAdd = new ArrayList<>();
        this.objectsToRemove = new ArrayList<>();
        this.initialized = false;
        // 移除组件索引初始化
    }
    
    /**
     * 初始化场景
     */
    public void initialize() {
        for (GameObject obj : gameObjects) {
            obj.initialize();
        }
        initialized = true;
    }
    
    /**
     * 更新场景
     */
    public void update(float deltaTime) {
        // 添加新对象
        for (GameObject obj : objectsToAdd) {
            gameObjects.add(obj);
            if (initialized) {
                obj.initialize();
            }
        }
        objectsToAdd.clear();
        
        // 移除标记的对象
        for (GameObject obj : objectsToRemove) {
            gameObjects.remove(obj);
        }
        objectsToRemove.clear();
        
        // 如果场景暂停，不更新游戏对象
        if (paused) {
            return;
        }
        
        // 更新所有活跃的游戏对象
        Iterator<GameObject> iterator = gameObjects.iterator();
        while (iterator.hasNext()) {
            GameObject obj = iterator.next();
            if (obj.isActive()) {
                obj.update(deltaTime);
            } else {
                iterator.remove();
            }
        }
    }
    
    /**
     * 渲染场景
     */
    public void render() {
        for (GameObject obj : gameObjects) {
            if (obj.isActive()) {
                obj.render();
            }
        }
    }
    
    /**
     * 添加游戏对象到场景
     */
    public void addGameObject(GameObject gameObject) {
        objectsToAdd.add(gameObject);
        gameObject.setScene(this); // 设置GameObject的场景引用
    }
    
    /**
     * 根据组件类型查找游戏对象
     */
    public <T extends Component<T>> List<GameObject> findGameObjectsByComponent(Class<T> componentType) {
        return gameObjects.stream()
            .filter(obj -> obj.hasComponent(componentType))
            .collect(Collectors.toList());
    }

    /**
     * 根据tag查找第一个游戏对象
     */
    public GameObject findGameObjectByTag(String tag) {
        return gameObjects.stream()
            .filter(obj -> tag.equals(obj.getTag()))
            .findFirst()
            .orElse(null);
    }

    /**
     * 根据tag查找所有游戏对象
     */
    public List<GameObject> findGameObjectsByTag(String tag) {
        return gameObjects.stream()
            .filter(obj -> tag.equals(obj.getTag()))
            .collect(Collectors.toList());
    }
    
    /**
     * 获取所有具有指定组件的游戏对象
     */
    public <T extends Component<T>> List<T> getComponents(Class<T> componentType) {
        return findGameObjectsByComponent(componentType).stream()
            .map(obj -> obj.getComponent(componentType))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    /**
     * 清空场景
     */
    public void clear() {
        gameObjects.clear();
        objectsToAdd.clear();
        objectsToRemove.clear();
    }
    
    /**
     * 获取场景名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取所有游戏对象
     */
    public List<GameObject> getGameObjects() {
        return new ArrayList<>(gameObjects);
    }
    
    /**
     * 设置场景暂停状态
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    /**
     * 检查场景是否暂停
     */
    public boolean isPaused() {
        return paused;
    }
    
    // 移除具体游戏逻辑，让子类实现
}