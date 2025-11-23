# 葫芦娃大战妖精 - 游戏架构文档

## 目录

1. [游戏简介](#游戏简介)
2. [项目结构](#项目结构)
3. [核心系统架构](#核心系统架构)
4. [管理器系统](#管理器系统)
5. [组件系统](#组件系统)
6. [游戏对象](#游戏对象)
7. [存档系统](#存档系统)
8. [战斗系统](#战斗系统)
9. [音效系统](#音效系统)
10. [渲染系统](#渲染系统)
11. [使用指南](#使用指南)

## 游戏简介

### 游戏概述

葫芦娃大战妖精是一个基于Java开发的2D动作游戏，采用组件-实体系统(ECS)架构。玩家控制葫芦娃与妖精战斗，游戏时长60秒，目标是消灭所有敌人获得胜利。

演示视频：[NJU-高级Java程序设计 作业j03 演示_哔哩哔哩_bilibili](https://www.bilibili.com/video/BV1Lf45zWEDk/?spm_id_from=333.1365.list.card_archive.click&vd_source=6589a49d106bd2d69b7c22bf0b6158db)

### 游戏特性

- **多种攻击方式**: 近战、远程、炮弹三种攻击模式
- **智能敌人AI**: 小兵和Boss具有不同的攻击策略
- **完整存档系统**: 支持游戏状态保存和加载
- **延迟加载机制**: 确保存档加载的稳定性
- **音效系统**: 背景音乐和战斗音效
- **粒子效果**: 视觉效果增强
- **健壮的游戏状态管理**: 避免加载存档时的状态冲突

### 操作控制

| 按键 | 功能          |
| ---- | ------------- |
| WASD | 玩家移动      |
| J    | 近战攻击      |
| U    | 远程攻击      |
| I    | 炮弹攻击      |
| P    | 暂停/恢复游戏 |
| F12  | 保存游戏      |
| M    | 音效开关      |
| N    | 背景音乐开关  |

## 项目结构

### 文件组织

```
src/main/java/com/gameengine/
├── core/                    # 核心引擎
│   ├── Component.java      # 组件基类
│   ├── GameObject.java     # 游戏对象基类
│   ├── GameEngine.java    # 游戏引擎主类
│   └── GameLogic.java     # 游戏逻辑基类
├── logic/                  # 逻辑管理系统
│   ├── AdvancedGameLogic.java    # 高级游戏逻辑
│   ├── GameStateManager.java     # 游戏状态管理器
│   ├── EnemySpawnManager.java    # 敌人生成管理器
│   ├── PhysicsManager.java       # 物理管理器
│   └── CombatSystem.java         # 战斗系统
├── components/             # 游戏组件
│   ├── TransformComponent.java      # 位置变换
│   ├── PhysicsComponent.java        # 物理运动
│   ├── HealthComponent.java         # 生命值管理
│   ├── SpriteComponent.java        # 精灵渲染
│   ├── AttackRangeComponent.java   # 攻击范围
│   ├── LifetimeComponent.java       # 生命周期
│   ├── AudioSystemComponent.java    # 音效系统
│   ├── BackgroundMusicComponent.java # 背景音乐
│   ├── ParticleSystemComponent.java # 粒子效果
│   ├── MainMenuComponent.java       # 主菜单
│   ├── SaveSystemComponent.java     # 存档系统
│   ├── LoadSystemComponent.java     # 加载系统
│   └── RenderComponent.java         # 渲染组件
├── characters/             # 角色相关
│   ├── Player.java         # 玩家角色
│   ├── CharacterFactory.java # 角色工厂
│   ├── enemies/            # 敌人类型
│   │   ├── Enemy.java      # 敌人基类
│   │   ├── Minion.java     # 小兵
│   │   └── Boss.java       # Boss
│   └── projectiles/        # 投射物
│       ├── Projectile.java # 投射物基类
│       ├── Bullet.java     # 子弹
│       ├── Cannonball.java # 炮弹
│       └── Bomb.java       # 炸弹
├── example/                # 游戏示例
│   ├── GameExample.java    # 游戏入口
│   └── scene/              # 场景
│       └── GameScene.java  # 主游戏场景
├── graphics/               # 图形渲染
│   └── Renderer.java       # 渲染器
├── input/                  # 输入处理
│   └── InputManager.java   # 输入管理器
├── math/                   # 数学工具
│   └── Vector2.java        # 2D向量
└── scene/                  # 场景管理
    └── Scene.java          # 场景基类
```

### 资源文件

```
resources/
├── sounds/                 # 音效文件
│   ├── bullet_fire.wav     # 子弹发射
│   ├── cannon_fire.wav     # 炮弹发射
│   ├── bomb_throw.wav      # 炸弹投掷
│   ├── bomb_explode.wav    # 炸弹爆炸
│   ├── melee_attack.wav    # 近战攻击
│   └── damage.wav          # 受伤音效
├── music/                  # 背景音乐
│   └── background.wav      # 背景音乐
├── saves/                  # 存档文件
└── 图片资源 (*.png)         # 角色和物品图片
```

## 核心系统架构

### 游戏启动流程

```
1. GameEngine.main() 启动
   ↓
2. 创建 GameEngine 实例
   ↓
3. 初始化窗口 (1100x600)
   ↓
4. 创建 GameScene 场景
   ↓
5. 初始化系统组件
   ├── AudioSystemComponent (音效系统)
   ├── BackgroundMusicComponent (背景音乐)
   ├── SaveSystemComponent (存档系统)
   ├── LoadSystemComponent (加载系统)
   └── MainMenuComponent (主菜单)
   ↓
6. 进入游戏循环
```

### 主游戏循环

```
每帧执行 (60 FPS):
1. AdvancedGameLogic.update() 统一调度
   ├── GameStateManager.update() - 状态管理
   ├── 延迟恢复检查 (投射物/敌人)
   ├── EnemySpawnManager.update() - 敌人生成
   ├── PhysicsManager.update() - 物理更新
   └── CombatSystem.update() - 战斗系统

2. 场景更新 Scene.update()
   ├── 处理输入 (InputManager)
   ├── 音频控制 (M/N键)
   ├── 暂停控制 (P键)
   └── 保存控制 (F12键)

3. 渲染 Scene.render()
   ├── 游戏对象渲染
   ├── UI界面渲染
   ├── 粒子效果渲染
   └── 主菜单渲染

4. 清理无效对象
```

### 游戏状态管理

```
游戏状态层次:
├── MainMenuComponent (主菜单状态)
│   ├── 新游戏
│   ├── 加载存档
│   └── 退出游戏
├── GameStateManager (游戏运行状态)
│   ├── 正常游戏 (gamePaused = false)
│   ├── 暂停状态 (gamePaused = true)
│   ├── 加载状态 (gameLoading = true)
│   ├── 延迟恢复状态 (投射物/敌人)
│   └── 游戏结束状态
│       ├── 胜利 (gameWon = true)
│       └── 失败 (gameEnded = true)
└── 各子系统状态
    ├── EnemySpawnManager (敌人生成状态)
    ├── CombatSystem (战斗状态)
    └── PhysicsManager (物理状态)
```

## 管理器系统

### 1. GameStateManager - 游戏状态管理器

**文件**: `src/main/java/com/gameengine/logic/GameStateManager.java`

#### 核心功能

- 游戏计时器管理
- 暂停/恢复控制
- 胜利/失败条件判断
- 延迟恢复机制管理
- 游戏加载状态管理

#### 主要方法

```java
public void update(float deltaTime);           // 更新游戏状态
public boolean shouldRestoreProjectiles();    // 检查是否恢复投射物
public boolean shouldRestoreEnemies();        // 检查是否恢复敌人
public void setGamePaused(boolean paused);    // 设置暂停状态
public void setGameWon();                     // 设置游戏胜利
public void setGameLost();                    // 设置游戏失败
public boolean isLoading();                   // 检查是否正在加载
public void setGameLoading(boolean loading);  // 设置加载状态
```

### 2. EnemySpawnManager - 敌人生成管理器

**文件**: `src/main/java/com/gameengine/logic/EnemySpawnManager.java`

#### 核心功能

- 敌人生成时机控制
- 敌人数量管理
- Boss生成逻辑

#### 生成规则

```
敌人生成时间轴:
0-50秒: 每10秒生成5个小兵
50-60秒: 生成5个小兵 + 1个Boss
60秒后: 停止生成
```

### 3. PhysicsManager - 物理管理器

**文件**: `src/main/java/com/gameengine/logic/PhysicsManager.java`

#### 核心功能

- 玩家移动输入处理
- 物理组件更新
- 边界碰撞检测
- 物理约束应用

### 4. CombatSystem - 战斗系统

**文件**: `src/main/java/com/gameengine/logic/CombatSystem.java`

#### 核心功能

- 玩家攻击处理
- 敌人AI攻击
- 碰撞检测
- 伤害计算
- 音效触发

### 5. AdvancedGameLogic - 高级游戏逻辑

**文件**: `src/main/java/com/gameengine/logic/AdvancedGameLogic.java`

#### 核心功能

作为调度中心，统一管理所有子系统的更新顺序和协作关系。特别处理游戏加载状态和对象恢复的时序同步问题。

## 组件系统

### 核心组件基类

**文件**: `src/main/java/com/gameengine/core/Component.java`

```java
public abstract class Component<T extends Component<T>> {
    protected GameObject gameObject;
  
    // 核心方法
    public abstract void initialize();
    public abstract void update(float deltaTime);
    public abstract void render();
}
```

### 主要组件

#### 1. TransformComponent - 位置变换

- 管理游戏对象的位置、旋转、缩放
- 提供位置计算和变换功能

#### 2. PhysicsComponent - 物理运动

- 处理物体的物理运动、速度、加速度
- 边界检测和碰撞响应

#### 3. HealthComponent - 生命值管理

- 管理对象的生命值、伤害、死亡状态
- 提供伤害计算和生存状态检查

#### 4. SpriteComponent - 精灵渲染

- 管理对象的图像渲染
- 图片加载和显示

#### 5. AttackRangeComponent - 攻击范围

- 管理攻击范围效果和持续时间
- 攻击区域检测

#### 6. LifetimeComponent - 生命周期

- 管理对象的生命周期，自动销毁
- 时间倒计时和对象清理

#### 7. AudioSystemComponent - 音效系统

- 音效文件管理和播放
- 音效开关控制

#### 8. BackgroundMusicComponent - 背景音乐

- 背景音乐播放和循环
- 音乐开关控制

#### 9. SaveSystemComponent - 存档系统

- 游戏状态保存
- JSON序列化

#### 10. LoadSystemComponent - 加载系统

- 游戏状态加载
- 延迟恢复机制

## 游戏对象

### GameObject 基类

**文件**: `src/main/java/com/gameengine/core/GameObject.java`

```java
public class GameObject {
    private String name;
    private String tag;
    private boolean active;
    private Map<Class<? extends Component>, Component> components;
  
    // 组件管理
    public <T extends Component> T getComponent(Class<T> componentClass);
    public <T extends Component> void addComponent(T component);
    public boolean hasComponent(Class<? extends Component> componentClass);
}
```

### 角色系统

#### 1. Player - 玩家角色

**文件**: `src/main/java/com/gameengine/characters/Player.java`

- 三种攻击模式：近战、远程、炮弹
- 攻击冷却时间管理
- 移动控制

#### 2. Enemy - 敌人基类

**文件**: `src/main/java/com/gameengine/characters/enemies/Enemy.java`

- 敌人AI决策框架
- 攻击冷却管理
- 玩家距离检测

#### 3. Minion - 小兵

**文件**: `src/main/java/com/gameengine/characters/enemies/Minion.java`

- AI策略：近战优先，远程备选
- 生命值：30HP
- 攻击冷却：近战1.5秒，远程2秒

#### 4. Boss - Boss敌人

**文件**: `src/main/java/com/gameengine/characters/enemies/Boss.java`

- AI策略：炸弹优先，近战次之，远程最后
- 生命值：150HP
- 特殊攻击：炸弹攻击

### 投射物系统

#### 1. Bullet - 子弹

- 速度：250像素/秒
- 生命时间：3秒
- 伤害：15点

#### 2. Cannonball - 炮弹

- 速度：200像素/秒
- 生命时间：4秒
- 伤害：50点

#### 3. Bomb - 炸弹

- 速度：150像素/秒
- 生命时间：4秒
- 伤害：80点
- 特殊效果：爆炸范围伤害

### CharacterFactory - 角色工厂

**文件**: `src/main/java/com/gameengine/characters/CharacterFactory.java`

统一的角色创建接口，负责：

- 创建各种角色对象
- 配置组件组合
- 设置初始属性

## 存档系统

### 存档系统架构

```
存档系统分离:
├── SaveSystemComponent - 负责保存
│   ├── 数据提取
│   ├── JSON序列化
│   └── 文件写入
└── LoadSystemComponent - 负责加载
    ├── 文件读取
    ├── JSON解析
    ├── 数据恢复
    └── 延迟加载管理
```

### 保存流程

```
1. 暂停游戏 (如果正在运行)
2. 打开文件选择器
3. 提取游戏数据:
   ├── 玩家状态 (位置、生命值、冷却时间)
   ├── 敌人状态 (位置、生命值、标签)
   ├── 投射物状态 (位置、速度、剩余时间)
   └── 游戏状态 (时间、Boss生成状态)
4. 序列化为JSON
5. 保存到文件
6. 延迟恢复游戏状态 (500ms后)
```

### 加载流程

```
1. 解析JSON存档文件
2. 立即恢复:
   ├── 玩家状态
   ├── 游戏状态字段
   └── 玩家冷却时间
3. 设置游戏加载状态 (gameLoading = true)
4. 延迟存储:
   ├── 敌人数据 → pendingEnemies
   └── 投射物数据 → pendingProjectiles
5. 等待0.5秒
6. 延迟恢复:
   ├── 恢复敌人对象
   └── 恢复投射物对象
7. 检查场景中恢复的对象
8. 设置游戏加载状态 (gameLoading = false)
```

### 延迟加载机制

为了解决存档加载时对象初始化冲突问题，系统采用延迟加载机制：

#### 设计原理

- 立即恢复不依赖其他系统的对象（玩家、游戏状态）
- 延迟恢复需要复杂初始化的对象（敌人、投射物）
- 确保所有系统组件完全初始化后再恢复对象
- 通过 `gameLoading` 状态避免游戏结束条件检查的时序问题

#### 实现细节

- 使用GameStateManager管理延迟计时器
- 0.5秒后自动触发延迟恢复
- 确保精灵组件和物理组件正确初始化
- 通过检查场景中实际存在的对象来确认恢复完成

### 存档数据结构

```java
public static class GameSaveData {
    public float gameTimer;              // 游戏时间
    public boolean gameEnded;            // 游戏结束状态
    public boolean gameWon;              // 胜利状态
    public boolean bossSpawned;          // Boss生成状态
    public PlayerData playerData;        // 玩家数据
    public List<EnemyData> enemies;      // 敌人列表
    public List<ProjectileData> projectiles; // 投射物列表
    public Map<String, Float> playerCooldowns; // 玩家冷却时间
}
```

## 战斗系统

### 攻击类型

#### 1. 近战攻击

- 攻击范围：60像素半径
- 持续时间：0.3秒
- 冷却时间：0.5秒
- 伤害：30点

#### 2. 远程攻击

- 发射子弹投射物
- 射程：750像素
- 冷却时间：0.8秒
- 伤害：15点

#### 3. 炮弹攻击

- 发射炮弹投射物
- 射程：800像素
- 冷却时间：3秒
- 伤害：50点

### 敌人AI系统

#### Minion AI策略

```
距离检测 → 攻击决策:
1. 距离 ≤ 40像素 且 近战冷却完毕 → 近战攻击
2. 否则，远程冷却完毕 → 远程攻击
3. 随机化冷却时间，避免同步攻击
```

#### Boss AI策略

```
攻击优先级:
1. 炸弹攻击 (冷却3秒)
2. 近战攻击 (距离≤40像素，冷却1.5秒)
3. 远程攻击 (冷却2秒)
```

### 碰撞检测系统

- 投射物 vs 角色碰撞
- 攻击范围 vs 角色碰撞
- 边界碰撞检测
- 伤害计算和应用

## 音效系统

### 音效文件管理

```
音效触发机制:
├── 玩家攻击音效
│   ├── bullet_fire.wav (远程攻击)
│   ├── cannon_fire.wav (炮弹攻击)
│   └── melee_attack.wav (近战攻击)
├── 敌人攻击音效
│   ├── bullet_fire.wav (远程攻击)
│   ├── bomb_throw.wav (炸弹投掷)
│   └── bomb_explode.wav (炸弹爆炸)
├── 战斗音效
│   └── damage.wav (受伤音效)
└── 背景音乐
    └── background.wav (循环播放)
```

### 音效控制

- M键：切换音效开关
- N键：切换背景音乐开关
- 音效与背景音乐独立控制
- 支持音量调节和静音功能

## 渲染系统

### Renderer 主渲染器

**文件**: `src/main/java/com/gameengine/graphics/Renderer.java`

#### 绘制功能

```java
// 基础绘制
public void drawText(String text, float x, float y, Color color);
public void drawRect(float x, float y, float width, float height, Color color);
public void drawCircle(float x, float y, float radius, Color color);
public void drawImage(BufferedImage image, float x, float y, float width, float height);

// 特殊绘制
public void drawProgressBar(float x, float y, float width, float height, float progress, Color color);
public void drawGradientCircle(float x, float y, float radius, Color color);
public void drawParticle(float x, float y, float size, Color color);
```

### RenderComponent 渲染组件

**文件**: `src/main/java/com/gameengine/components/RenderComponent.java`

#### 渲染类型

- RECT：矩形渲染
- CIRCLE：圆形渲染
- IMAGE：图像渲染
- TEXT：文本渲染
- PROGRESS_BAR：进度条渲染
- GRADIENT_CIRCLE：渐变圆形
- PARTICLE：粒子渲染

### 渲染流程

```
渲染顺序:
1. 背景渲染
2. 游戏对象渲染 (玩家、敌人、投射物)
3. 攻击效果渲染
4. 粒子效果渲染
5. UI界面渲染 (血条、冷却条、时间进度)
6. 主菜单渲染 (如果激活)
7. 暂停界面渲染 (如果暂停)
```

## 使用指南

### 编译和运行

```bash
# 编译
./compile.bat    # Windows
./compile.sh     # Linux/Mac

# 运行
./run.bat        # Windows
./run.sh         # Linux/Mac
```

### 游戏玩法

1. **开始游戏**：运行后选择"新游戏"或"加载存档"
2. **移动**：使用WASD键控制葫芦娃移动
3. **攻击**：
   - J键：近战攻击（高伤害，短距离）
   - U键：远程攻击（中等伤害，中等距离）
   - I键：炮弹攻击（高伤害，远距离，长冷却）
4. **存档**：按P键暂停，然后按F12键保存游戏
5. **音效控制**：M键切换音效，N键切换背景音乐

### 胜利条件

- 在60秒消灭所有敌人（包括Boss）
- 玩家生命值不为零

### 失败条件

- 玩家生命值归零

### 开发扩展

如需扩展游戏功能，可以：

1. 在 `logic/`目录添加新的管理器
2. 在 `components/`目录添加新的组件
3. 在 `characters/`目录添加新的角色类型
4. 在 `CharacterFactory`中添加新的创建方法
5. 更新存档系统以支持新的数据结构

项目采用模块化设计，各系统职责明确，便于维护和扩展。
