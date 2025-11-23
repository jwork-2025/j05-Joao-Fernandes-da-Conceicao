@echo off
echo Compiling game engine...

if not exist build mkdir build
if not exist build\classes mkdir build\classes

REM Compile all Java files
javac -encoding UTF-8 -d build\classes -cp . ^
    src\main\java\com\gameengine\math\Vector2.java ^
    src\main\java\com\gameengine\input\InputManager.java ^
    src\main\java\com\gameengine\core\Component.java ^
    src\main\java\com\gameengine\core\GameObject.java ^
    src\main\java\com\gameengine\components\TransformComponent.java ^
    src\main\java\com\gameengine\components\PhysicsComponent.java ^
    src\main\java\com\gameengine\components\RenderComponent.java ^
    src\main\java\com\gameengine\components\HealthComponent.java ^
    src\main\java\com\gameengine\components\SpriteComponent.java ^
    src\main\java\com\gameengine\components\LifetimeComponent.java ^
    src\main\java\com\gameengine\components\AttackRangeComponent.java ^
    src\main\java\com\gameengine\components\ParticleSystemComponent.java ^
    src\main\java\com\gameengine\components\AudioSystemComponent.java ^
    src\main\java\com\gameengine\components\BackgroundMusicComponent.java ^
    src\main\java\com\gameengine\components\SaveSystemComponent.java ^
    src\main\java\com\gameengine\components\LoadSystemComponent.java ^
    src\main\java\com\gameengine\components\MainMenuComponent.java ^
    src\main\java\com\gameengine\graphics\Renderer.java ^
    src\main\java\com\gameengine\core\GameEngine.java ^
    src\main\java\com\gameengine\core\GameLogic.java ^
    src\main\java\com\gameengine\logic\GameStateManager.java ^
    src\main\java\com\gameengine\logic\EnemySpawnManager.java ^
    src\main\java\com\gameengine\logic\CombatSystem.java ^
    src\main\java\com\gameengine\logic\PhysicsManager.java ^
    src\main\java\com\gameengine\logic\AdvancedGameLogic.java ^
    src\main\java\com\gameengine\scene\Scene.java ^
    src\main\java\com\gameengine\characters\Player.java ^
    src\main\java\com\gameengine\characters\CharacterFactory.java ^
    src\main\java\com\gameengine\characters\enemies\Enemy.java ^
    src\main\java\com\gameengine\characters\enemies\Minion.java ^
    src\main\java\com\gameengine\characters\enemies\Boss.java ^
    src\main\java\com\gameengine\characters\projectiles\Projectile.java ^
    src\main\java\com\gameengine\characters\projectiles\Bullet.java ^
    src\main\java\com\gameengine\characters\projectiles\Cannonball.java ^
    src\main\java\com\gameengine\characters\projectiles\Bomb.java ^
    src\main\java\com\gameengine\example\scene\GameScene.java ^
    src\main\java\com\gameengine\example\GameExample.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo Run game: java -cp build\classes com.gameengine.example.GameExample
) else (
    echo Compilation failed!
    exit /b 1
)