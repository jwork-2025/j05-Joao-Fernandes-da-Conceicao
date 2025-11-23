package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 音效系统组件
 * 用于播放游戏音效
 */
public class AudioSystemComponent extends Component<AudioSystemComponent> {
    private static boolean audioEnabled = true; // 全局音效开关
    private Map<String, Clip> soundClips;
    private Map<String, String> soundPaths;
    
    public AudioSystemComponent() {
        this.soundClips = new HashMap<>();
        this.soundPaths = new HashMap<>();
        initializeSoundPaths();
    }
    
    @Override
    public void initialize() {
        // 预加载音效文件（如果存在）
        loadSounds();
    }
    
    @Override
    public void update(float deltaTime) {
        // 音效系统不需要更新
    }
    
    @Override
    public void render() {
        // 音效系统不需要渲染
    }
    
    /**
     * 初始化音效文件路径
     */
    private void initializeSoundPaths() {
        soundPaths.put("bomb_throw", "resources/sounds/bomb_throw.wav");
        soundPaths.put("bomb_explode", "resources/sounds/bomb_explode.wav");
        soundPaths.put("damage", "resources/sounds/damage.wav");
        soundPaths.put("bullet_fire", "resources/sounds/bullet_fire.wav");
        soundPaths.put("melee_attack", "resources/sounds/melee_attack.wav");
        soundPaths.put("cannon_fire", "resources/sounds/cannon_fire.wav");
    }
    
    /**
     * 加载音效文件
     */
    private void loadSounds() {
        for (Map.Entry<String, String> entry : soundPaths.entrySet()) {
            String soundName = entry.getKey();
            String soundPath = entry.getValue();
            
            try {
                File soundFile = new File(soundPath);
                if (soundFile.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    soundClips.put(soundName, clip);
                    System.out.println("音效加载成功: " + soundName);
                } else {
                    System.out.println("音效文件不存在，跳过: " + soundPath);
                }
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.out.println("音效加载失败: " + soundName + " - " + e.getMessage());
            }
        }
    }
    
    /**
     * 播放音效
     */
    public void playSound(String soundName) {
        if (!audioEnabled) {
            return; // 音效已关闭
        }
        
        Clip clip = soundClips.get(soundName);
        if (clip != null) {
            try {
                // 重置音效到开始位置
                clip.setFramePosition(0);
                clip.start();
            } catch (Exception e) {
                System.out.println("播放音效失败: " + soundName + " - " + e.getMessage());
            }
        } else {
            // 音效文件不存在，输出调试信息但不报错
            System.out.println("音效不存在: " + soundName);
        }
    }
    
    /**
     * 设置全局音效开关
     */
    public static void setAudioEnabled(boolean enabled) {
        audioEnabled = enabled;
        System.out.println("音效" + (enabled ? "开启" : "关闭"));
    }
    
    /**
     * 获取音效开关状态
     */
    public static boolean isAudioEnabled() {
        return audioEnabled;
    }
    
    /**
     * 切换音效开关
     */
    public static void toggleAudio() {
        setAudioEnabled(!audioEnabled);
    }
    
    /**
     * 停止所有音效
     */
    public void stopAllSounds() {
        for (Clip clip : soundClips.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        stopAllSounds();
        for (Clip clip : soundClips.values()) {
            clip.close();
        }
        soundClips.clear();
    }
}
