package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * 背景音乐系统组件
 * 用于播放背景音乐（支持WAV格式，MP3需要额外库支持）
 */
public class BackgroundMusicComponent extends Component<BackgroundMusicComponent> {
    private static boolean bgmEnabled = true; // 全局背景音乐开关
    private Clip musicClip;
    private String musicPath;
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private long pausePosition = 0;
    
    public BackgroundMusicComponent() {
        this.musicPath = "resources/music/background.wav"; // 默认背景音乐路径
    }
    
    public BackgroundMusicComponent(String musicPath) {
        this.musicPath = musicPath;
    }
    
    @Override
    public void initialize() {
        loadMusic();
    }
    
    @Override
    public void update(float deltaTime) {
        // 背景音乐不需要更新
    }
    
    @Override
    public void render() {
        // 背景音乐不需要渲染
    }
    
    /**
     * 加载背景音乐
     */
    private void loadMusic() {
        try {
            File musicFile = new File(musicPath);
            if (musicFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
                musicClip = AudioSystem.getClip();
                musicClip.open(audioStream);
                System.out.println("背景音乐加载成功: " + musicPath);
            } else {
                System.out.println("背景音乐文件不存在，跳过: " + musicPath);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("背景音乐加载失败: " + musicPath + " - " + e.getMessage());
        }
    }
    
    /**
     * 播放背景音乐
     */
    public void playMusic() {
        if (!bgmEnabled) {
            System.out.println("背景音乐已关闭，跳过播放");
            return;
        }
        
        if (musicClip == null) {
            System.out.println("背景音乐未加载，跳过播放");
            return;
        }
        
        try {
            if (isPaused) {
                // 从暂停位置继续播放
                musicClip.setMicrosecondPosition(pausePosition);
                musicClip.start();
                isPaused = false;
                System.out.println("背景音乐从暂停位置继续播放");
            } else if (!isPlaying) {
                // 从头开始播放
                musicClip.setFramePosition(0);
                musicClip.start();
                isPlaying = true;
                System.out.println("背景音乐从头开始播放");
            } else {
                System.out.println("背景音乐已在播放中");
            }
        } catch (Exception e) {
            System.out.println("播放背景音乐失败: " + e.getMessage());
        }
    }
    
    /**
     * 暂停背景音乐
     */
    public void pauseMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            pausePosition = musicClip.getMicrosecondPosition();
            musicClip.stop();
            isPaused = true;
            System.out.println("背景音乐暂停");
        }
    }
    
    /**
     * 停止背景音乐
     */
    public void stopMusic() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.setFramePosition(0);
            isPlaying = false;
            isPaused = false;
            pausePosition = 0;
            System.out.println("背景音乐停止");
        }
    }
    
    /**
     * 设置背景音乐开关
     */
    public static void setBgmEnabled(boolean enabled) {
        bgmEnabled = enabled;
        System.out.println("背景音乐" + (enabled ? "开启" : "关闭"));
    }
    
    /**
     * 获取背景音乐开关状态
     */
    public static boolean isBgmEnabled() {
        return bgmEnabled;
    }
    
    /**
     * 切换背景音乐开关
     */
    public static void toggleBgm() {
        setBgmEnabled(!bgmEnabled);
    }
    
    /**
     * 检查音乐是否正在播放
     */
    public boolean isMusicPlaying() {
        return musicClip != null && musicClip.isRunning();
    }
    
    /**
     * 检查音乐是否暂停
     */
    public boolean isMusicPaused() {
        return isPaused;
    }
    
    /**
     * 设置音乐路径
     */
    public void setMusicPath(String path) {
        this.musicPath = path;
        stopMusic();
        loadMusic();
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        stopMusic();
        if (musicClip != null) {
            musicClip.close();
        }
    }
}
