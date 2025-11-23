[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/aDiFgvK1)
# j05

本仓库提供了一个简单的游戏引擎，请完善该引擎或重新实现自己的引擎，并开发一个葫芦娃与妖精的对战游戏。游戏录屏发小破站。

---

# 简单Java游戏引擎

一个基于Java Swing的简单游戏引擎，提供了基本的游戏开发功能。

## 功能特性

- **核心引擎**: 游戏循环、场景管理、对象生命周期管理
- **渲染系统**: 基于Swing的2D渲染，支持矩形、圆形、线条绘制
- **输入处理**: 键盘和鼠标输入管理
- **数学工具**: 2D向量运算
- **场景管理**: 游戏对象的添加、移除、查找
- **碰撞检测**: 基本的距离检测
- **组件系统**: 基于泛型的组件-实体系统(ECS)

## 项目结构

```
src/main/java/com/gameengine/
├── core/           # 核心引擎类
│   ├── GameEngine.java    # 游戏引擎主类
│   ├── GameObject.java    # 游戏对象基类
│   ├── Component.java    # 组件基类
│   └── GameLogic.java    # 游戏规则处理
├── components/     # 组件系统
│   ├── TransformComponent.java  # 变换组件
│   ├── PhysicsComponent.java    # 物理组件
│   └── RenderComponent.java     # 渲染组件
├── graphics/       # 渲染系统
│   └── Renderer.java      # 渲染器
├── input/          # 输入处理
│   └── InputManager.java  # 输入管理器
├── math/           # 数学工具
│   └── Vector2.java       # 2D向量类
├── scene/          # 场景管理
│   └── Scene.java         # 场景类
└── example/        # 示例代码
    └── GameExample.java   # 主程序入口
```

## 快速开始

### 1. 环境要求

- Java 11 或更高版本

### 2. 运行示例

```bash
# 编译并运行游戏
./run.sh

# 或者分步执行
./compile.sh
java -cp build/classes com.gameengine.example.GameExample
```

### 3. 游戏控制

- **WASD** 或 **方向键**: 移动玩家（绿色方块）
- 玩家需要避免与橙色敌人碰撞
- 碰撞后玩家会重置到中心位置

## 使用指南

### 组件系统(ECS)

这个引擎使用组件-实体系统(ECS)设计模式：

```java
// 创建游戏对象
GameObject player = new GameObject("Player");

// 添加变换组件
TransformComponent transform = player.addComponent(
    new TransformComponent(new Vector2(100, 100))
);

// 添加渲染组件
RenderComponent render = player.addComponent(
    new RenderComponent(
        RenderComponent.RenderType.RECTANGLE,
        new Vector2(20, 20),
        new RenderComponent.Color(0.0f, 1.0f, 0.0f, 1.0f)
    )
);

// 添加物理组件
PhysicsComponent physics = player.addComponent(
    new PhysicsComponent(1.0f)
);
```

### 创建自定义场景

```java
Scene gameScene = new Scene("MyGameScene") {
    private GameLogic gameLogic;
    
    @Override
    public void initialize() {
        super.initialize();
        this.gameLogic = new GameLogic(this);
        
        // 创建游戏对象
        createPlayer();
        createEnemies();
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        
        // 使用游戏逻辑类处理游戏规则
        gameLogic.handlePlayerInput();
        gameLogic.updatePhysics();
        gameLogic.checkCollisions();
    }
    
    private void createPlayer() {
        // 创建玩家逻辑
    }
    
    private void createEnemies() {
        // 创建敌人逻辑
    }
};
```

### 使用游戏逻辑类

```java
// 创建游戏逻辑实例
GameLogic gameLogic = new GameLogic(scene);

// 在场景更新中调用游戏逻辑
@Override
public void update(float deltaTime) {
    super.update(deltaTime);
    
    // 处理玩家输入
    gameLogic.handlePlayerInput();
    
    // 更新物理系统
    gameLogic.updatePhysics();
    
    // 检查碰撞
    gameLogic.checkCollisions();
}
```

### 处理输入

```java
InputManager input = InputManager.getInstance();

// 检查按键是否被按下
if (input.isKeyPressed(87)) { // W键
    // 处理W键按下
}

// 检查方向键
if (input.isKeyPressed(38)) { // 上箭头
    // 处理上箭头按下
}

// 获取鼠标位置
Vector2 mousePos = input.getMousePosition();
```

### 渲染图形

```java
// 绘制矩形
renderer.drawRect(x, y, width, height, r, g, b, a);

// 绘制圆形
renderer.drawCircle(x, y, radius, r, g, b, a);

// 绘制线条
renderer.drawLine(x1, y1, x2, y2, r, g, b, a);
```

## 示例游戏说明

示例游戏是一个简单的躲避游戏：

- 玩家（绿色方块）可以通过WASD或方向键移动
- 敌人（橙色方块）会随机移动
- 玩家需要避免与敌人碰撞
- 碰撞后玩家会重置到中心位置
- 蓝色小圆点是装饰元素

## 架构设计

### 职责分离

```
GameExample (游戏设定)
    ↓ 使用
GameLogic (游戏规则)
    ↓ 操作
Scene (场景管理)
    ↓ 管理
GameObject + Components (游戏对象)
```

### 核心组件

- **GameEngine**: 游戏引擎主类，管理游戏循环
- **Scene**: 场景管理，负责游戏对象的生命周期
- **GameLogic**: 游戏规则处理，包含输入、物理、碰撞逻辑
- **GameObject**: 游戏对象基类，使用组件系统
- **Component**: 组件基类，实现ECS架构

### 设计优势

- **单一职责**: 每个类职责明确
- **易于扩展**: 可以轻松添加新的游戏规则
- **代码复用**: GameLogic可以在不同场景中重用
- **维护性**: 游戏逻辑集中管理
- **测试性**: 可以独立测试游戏逻辑

## JSONL存档系统

本项目实现了基于JSONL（JSON Lines）格式的游戏存档系统，相比传统的JSON格式具有更好的扩展性和容错性。

### JSONL格式说明

JSONL（JSON Lines）是一种文本格式，每行都是一个独立的JSON对象。相比传统JSON格式的优势：

- **流式处理**: 可以逐行读取，内存占用更小
- **易于追加**: 新数据可以追加到文件末尾
- **容错性强**: 单行损坏不影响其他行
- **工具友好**: 可以使用 `grep`、`awk` 等命令行工具处理

### 存档操作方法

**重要提示：只有按 `P` 键暂停游戏后，才能按 `F12` 键保存游戏。**

存档操作步骤：
1. 在游戏进行中，按 `P` 键暂停游戏
2. 暂停后，按 `F12` 键打开文件保存对话框
3. 选择保存位置和文件名（默认扩展名为 `.jsonl`）
4. 保存完成后，可以再次按 `P` 键恢复游戏

**注意**：如果游戏未暂停，`F12` 键不会触发保存功能。这是为了避免在游戏进行中意外触发保存操作，影响游戏体验。

### 存档文件格式

存档文件使用 `.jsonl` 扩展名，每行一个JSON对象，通过 `type` 字段标识数据类型：

```jsonl
{"type":"gameState","gameTimer":64.17,"gameEnded":false,"gameWon":false,"bossSpawned":true}
{"type":"player","position":{"x":472.0,"y":432.7},"health":80,"maxHealth":200}
{"type":"cooldowns","meleeCooldown":0.0,"rangeCooldown":-0.02,"cannonCooldown":-0.01}
{"type":"enemy","tag":"Boss","enemyType":"Boss","position":{"x":139.2,"y":379.5},"health":120,"maxHealth":150}
{"type":"projectile","tag":"EnemyProjectile","projectileType":"Bullet","position":{"x":665.1,"y":6.1},"velocity":{"x":205.3,"y":-142.7},"lifetime":0.3,"remainingLifetime":0.3}
```

### 关键代码实现

#### 1. 保存功能（SaveSystemComponent）

保存时将数据转换为JSONL格式，每行一个JSON对象：

```java
// JSONL序列化方法（每行一个JSON对象）
private List<String> saveDataToJsonl(GameSaveData data) {
    List<String> lines = new ArrayList<>();
    
    // 第一行：游戏状态
    lines.add("{\"type\":\"gameState\",\"gameTimer\":...}");
    
    // 第二行：玩家数据
    lines.add("{\"type\":\"player\",\"position\":{...}}");
    
    // 每个敌人一行
    for (EnemyData enemy : data.enemies) {
        lines.add("{\"type\":\"enemy\",...}");
    }
    
    // 每个投射物一行
    for (ProjectileData projectile : data.projectiles) {
        lines.add("{\"type\":\"projectile\",...}");
    }
    
    return lines;
}
```

#### 2. 加载功能（LoadSystemComponent）

加载时逐行读取并解析JSONL格式：

```java
// JSONL解析方法（逐行解析）
private GameSaveData jsonlToSaveData(List<String> lines) {
    GameSaveData data = new GameSaveData();
    
    for (String line : lines) {
        String type = extractStringValue(line, "type");
        
        if ("gameState".equals(type)) {
            // 解析游戏状态
        } else if ("player".equals(type)) {
            // 解析玩家数据
        } else if ("enemy".equals(type)) {
            // 解析敌人数据
        } else if ("projectile".equals(type)) {
            // 解析投射物数据
        }
    }
    
    return data;
}
```

### 延迟加载机制

为了确保游戏对象正确初始化，系统采用了延迟加载机制。延迟加载的原因如下：

#### 1. **组件初始化依赖问题**

敌人和投射物对象需要多个组件的协同工作：
- **SpriteComponent**: 需要加载图片资源，初始化渲染状态
- **PhysicsComponent**: 需要设置速度、加速度等物理属性
- **HealthComponent**: 需要设置生命值
- **LifetimeComponent**: 需要设置生命周期（投射物）

如果立即恢复这些对象，可能会出现：
- 图片资源尚未加载完成
- 物理系统尚未完全初始化
- 组件之间的依赖关系尚未建立

#### 2. **游戏状态检查的时序问题（关键原因）**

**这是延迟加载机制最关键的原因。** 游戏逻辑中会检查游戏结束条件，特别是在游戏时间达到60秒后，会检查是否所有敌人都被清除：

```java
// 检查是否通关：游戏时间到且没有敌人（包括Boss）
if (gameStateManager.getGameTimer() >= gameStateManager.getGameDuration()) {
    boolean hasEnemies = false;
    boolean hasBosses = false;
    
    for (GameObject obj : scene.getGameObjects()) {
        // 检查场景中的敌人
    }
    
    // 只有在没有小兵和Boss的情况下才算胜利
    if (!hasEnemies && !hasBosses) {
        gameStateManager.setGameWon();
    }
}
```

**问题场景：**

如果存档的游戏时间已经超过60秒（`gameTimer >= gameDuration`），在加载存档时：

1. **没有延迟加载机制的情况**：
   - 敌人数据被解析并存储到 `pendingEnemies`，但还没有恢复到场景中
   - 游戏状态（包括 `gameTimer`）立即恢复，此时 `gameTimer >= 60`
   - `checkGameEndConditions()` 被调用（如果没有 `gameLoading` 保护）
   - 检查场景中的敌人：**发现没有敌人**（因为敌人还在 `pendingEnemies` 中，尚未恢复）
   - **错误判定为游戏胜利**（`gameWon = true`）
   - 存档机制完全失效，游戏无法正常继续

2. **有延迟加载机制的情况**：
   - 设置 `gameLoading = true`
   - 在加载期间，`checkGameEndConditions()` 被跳过
   - 等待0.5秒后，敌人完全恢复到场景中
   - 设置 `gameLoading = false`
   - 此时再检查游戏结束条件，才能正确判断游戏状态

**因此，延迟加载机制是确保存档系统正常运行的关键保障，特别是在游戏时间超过60秒后的存档加载场景中。**

#### 3. **场景对象添加的异步性**

`scene.addGameObject()` 方法添加的对象需要等待下一帧才会真正加入到场景的游戏对象列表中。立即检查对象是否存在可能会失败。

#### 延迟加载实现

延迟加载通过 `GameStateManager` 管理，延迟时间为 0.5 秒：

```java
// 延迟恢复投射物管理
private float projectileRestoreTimer = 0.0f;
private final float PROJECTILE_RESTORE_DELAY = 0.5f;

// 延迟恢复敌人管理
private float enemyRestoreTimer = 0.0f;
private final float ENEMY_RESTORE_DELAY = 0.5f;
```

加载流程：

1. **立即恢复**: 玩家状态、游戏状态字段、玩家冷却时间（这些不依赖复杂初始化）
2. **延迟存储**: 将敌人和投射物数据存储到 `pendingEnemies` 和 `pendingProjectiles`
3. **设置加载状态**: `gameLoading = true`，避免游戏逻辑检查
4. **等待0.5秒**: 确保所有系统组件完全初始化
5. **延迟恢复**: 恢复敌人和投射物对象，重新初始化所有组件
6. **清除加载状态**: `gameLoading = false`，恢复正常游戏逻辑

这种设计确保了：
- 所有组件正确初始化
- 避免时序相关的bug
- 游戏状态检查的准确性
- 对象正确添加到场景中

## 扩展功能

这个游戏引擎提供了基础功能，你可以在此基础上扩展：

- 添加纹理和精灵渲染
- 实现更复杂的物理系统
- 添加音频支持
- 实现粒子系统
- 添加UI系统
- 实现资源管理
- 添加更多组件类型

## 技术特点

- **无外部依赖**: 只使用Java标准库
- **简单构建**: 使用shell脚本编译，无需Maven
- **组件化设计**: 基于ECS架构，易于扩展
- **职责分离**: Scene负责场景管理，GameLogic负责游戏规则
- **跨平台**: 基于Swing，支持所有Java平台
- **易于维护**: 代码结构清晰，职责明确

## 许可证

本项目仅供学习和参考使用。

