package com.gameengine.characters;

import com.gameengine.components.AttackRangeComponent;
import com.gameengine.components.LifetimeComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.core.GameObject;
import com.gameengine.math.Vector2;
import com.gameengine.characters.projectiles.Bullet;
import com.gameengine.characters.projectiles.Cannonball;
import com.gameengine.characters.projectiles.Bomb;
import com.gameengine.characters.Player;
import com.gameengine.characters.enemies.Enemy;
import com.gameengine.characters.enemies.Minion;
import com.gameengine.characters.enemies.Boss;

/**
 * 角色工厂类，用于创建各种游戏角色和效果
 */
public class CharacterFactory {
    
    /**
     * 创建玩家
     */
    public static Player createPlayer(Vector2 position) {
        return new Player(position);
    }
    
    /**
     * 创建小兵
     */
    public static Minion createMinion(Vector2 position) {
        return new Minion(position);
    }
    
    /**
     * 创建Boss
     */
    public static Boss createBoss(Vector2 position) {
        return new Boss(position);
    }
    
    /**
     * 创建子弹
     */
    public static Bullet createBullet(Vector2 start, Vector2 target, String tag) {
        return new Bullet(start, target, tag);
    }
    
    /**
     * 创建炮弹
     */
    public static Cannonball createCannonball(Vector2 start, Vector2 target, String tag) {
        return new Cannonball(start, target, tag);
    }
    
    /**
     * 创建炸弹
     */
    public static Bomb createBomb(Vector2 start, Vector2 target, String tag) {
        return new Bomb(start, target, tag);
    }
    
    /**
     * 创建玩家近程攻击效果
     */
    public static GameObject createPlayerMeleeAttack(Vector2 position) {
        // 创建攻击范围效果
        GameObject attackRange = new GameObject("AttackRange", "AttackRange");
        attackRange.addComponent(new TransformComponent(new Vector2(position.x, position.y)));
        attackRange.addComponent(new AttackRangeComponent(
            Player.getMeleeAttackDuration(), 
            Player.getMeleeRange()
        )); // 使用玩家类的常量
        
        return attackRange; // 只返回攻击范围效果
    }
    
    /**
     * 创建玩家近战攻击判定区域（用于碰撞检测）
     */
    public static GameObject createPlayerMeleeArea(Vector2 position) {
        GameObject meleeArea = new GameObject("Melee", "MeleeAttack");
        meleeArea.addComponent(new TransformComponent(new Vector2(position.x, position.y)));
        meleeArea.addComponent(new LifetimeComponent(Player.getMeleeAttackDuration() / 6)); // 使用攻击持续时间的1/6
        
        return meleeArea;
    }
    
    /**
     * 创建敌人近程攻击效果
     */
    public static GameObject createEnemyMeleeAttack(Vector2 position) {
        // 创建敌人攻击范围效果
        GameObject attackRange = new GameObject("EnemyAttackRange", "EnemyAttackRange");
        attackRange.addComponent(new TransformComponent(new Vector2(position.x, position.y)));
        attackRange.addComponent(new AttackRangeComponent(Player.getMeleeAttackDuration(), Enemy.getMeleeRangeStatic())); // 使用静态方法获取参数
        
        // 创建敌人攻击判定区域（用于碰撞检测）
        GameObject meleeArea = new GameObject("EnemyMelee", "EnemyMeleeAttack");
        meleeArea.addComponent(new TransformComponent(new Vector2(position.x, position.y)));
        meleeArea.addComponent(new LifetimeComponent(Player.getMeleeAttackDuration() / 6)); // 使用攻击持续时间的1/6
        
        return attackRange; // 返回攻击范围效果，攻击判定区域需要单独添加
    }
    
    /**
     * 创建攻击判定区域（用于碰撞检测）
     */
    public static GameObject createMeleeArea(Vector2 position, String tag) {
        GameObject meleeArea = new GameObject("Melee", tag);
        meleeArea.addComponent(new TransformComponent(new Vector2(position.x, position.y)));
        meleeArea.addComponent(new LifetimeComponent(Player.getMeleeAttackDuration() / 6)); // 使用攻击持续时间的1/6
        return meleeArea;
    }
}
