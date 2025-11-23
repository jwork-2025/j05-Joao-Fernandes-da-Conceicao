package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;
import com.gameengine.graphics.Renderer;
import com.gameengine.input.InputManager;
import com.gameengine.scene.Scene;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * 主菜单界面组件
 * 处理游戏开始界面的显示和交互
 */
public class MainMenuComponent extends Component<MainMenuComponent> {
    private Renderer renderer;
    private Scene scene;
    private boolean isActive = true;
    private int selectedOption = 0; // 0: 新游戏, 1: 加载存档, 2: 退出游戏
    private final String[] menuOptions = {"新游戏", "加载存档", "退出游戏"};
    private boolean hasSaveFile = false;
    
    // 按键状态跟踪
    private boolean upKeyPressed = false;
    private boolean downKeyPressed = false;
    private boolean wKeyPressed = false;
    private boolean sKeyPressed = false;
    private boolean enterKeyPressed = false;
    
    public MainMenuComponent(Renderer renderer, Scene scene) {
        this.renderer = renderer;
        this.scene = scene;
    }
    
    @Override
    public void initialize() {
        // 检查是否有存档文件
        GameObject saveSystem = scene.findGameObjectByTag("SaveSystem");
        if (saveSystem != null && saveSystem.hasComponent(SaveSystemComponent.class)) {
            SaveSystemComponent save = saveSystem.getComponent(SaveSystemComponent.class);
            hasSaveFile = save.hasSaveFile();
        }
    }
    
    @Override
    public void update(float deltaTime) {
        if (!isActive) return;
        
        InputManager input = InputManager.getInstance();
        
        // 处理键盘输入 - 上方向键或W键
        if (input.isKeyPressed(38) || input.isKeyPressed(87)) { // 上箭头或W
            if (!upKeyPressed && !wKeyPressed) {
                selectedOption = (selectedOption - 1 + menuOptions.length) % menuOptions.length;
                // 如果选择加载存档但没有存档文件，跳过
                if (selectedOption == 1 && !hasSaveFile) {
                    selectedOption = (selectedOption - 1 + menuOptions.length) % menuOptions.length;
                }
            }
            upKeyPressed = input.isKeyPressed(38);
            wKeyPressed = input.isKeyPressed(87);
        } else {
            upKeyPressed = false;
            wKeyPressed = false;
        }
        
        // 处理键盘输入 - 下方向键或S键
        if (input.isKeyPressed(40) || input.isKeyPressed(83)) { // 下箭头或S
            if (!downKeyPressed && !sKeyPressed) {
                selectedOption = (selectedOption + 1) % menuOptions.length;
                // 如果选择加载存档但没有存档文件，跳过
                if (selectedOption == 1 && !hasSaveFile) {
                    selectedOption = (selectedOption + 1) % menuOptions.length;
                }
            }
            downKeyPressed = input.isKeyPressed(40);
            sKeyPressed = input.isKeyPressed(83);
        } else {
            downKeyPressed = false;
            sKeyPressed = false;
        }
        
        // 处理回车键
        if (input.isKeyPressed(10)) { // 回车键
            if (!enterKeyPressed) {
                handleMenuSelection();
            }
            enterKeyPressed = true;
        } else {
            enterKeyPressed = false;
        }
    }
    
    @Override
    public void render() {
        if (!isActive) return;
        
        // 绘制半透明背景
        renderer.drawRect(0, 0, 1100, 600, 0.0f, 0.0f, 0.0f, 0.8f);
        
        // 计算居中位置
        int windowWidth = 1100;
        int windowHeight = 600;
        int centerX = windowWidth / 2;
        int centerY = windowHeight / 2;
        
        // 绘制标题（居中）
        renderer.drawText("葫芦娃大战妖精", centerX - 100, centerY - 100, 1.0f, 1.0f, 0.0f, 1.0f);
        
        // 绘制菜单选项（居中）
        int startY = centerY - 20;
        for (int i = 0; i < menuOptions.length; i++) {
            String option = menuOptions[i];
            
            // 如果是加载存档但没有存档文件，显示为灰色
            if (i == 1 && !hasSaveFile) {
                renderer.drawText(option + " (无存档)", centerX - 60, startY + i * 50, 0.5f, 0.5f, 0.5f, 1.0f);
            } else {
                // 选中项用不同颜色
                if (i == selectedOption) {
                    renderer.drawText("> " + option + " <", centerX - 60, startY + i * 50, 1.0f, 1.0f, 0.0f, 1.0f);
                } else {
                    renderer.drawText(option, centerX - 40, startY + i * 50, 1.0f, 1.0f, 1.0f, 1.0f);
                }
            }
        }
        
        // 绘制操作说明（居中）
        renderer.drawText("使用方向键或WASD选择，回车键确认", centerX - 120, centerY + 150, 0.8f, 0.8f, 0.8f, 1.0f);
        
        // 显示音频状态（居中）
        String audioStatus = "音效(M): " + (AudioSystemComponent.isAudioEnabled() ? "开启" : "关闭");
        String bgmStatus = "背景音乐(N): " + (BackgroundMusicComponent.isBgmEnabled() ? "开启" : "关闭");
        renderer.drawText(audioStatus + "    " + bgmStatus, centerX - 100, centerY + 170, 0.7f, 0.7f, 1.0f, 1.0f);
        
        // 如果有存档，显示存档信息（居中）
        if (hasSaveFile) {
            renderer.drawText("发现存档文件，可以加载游戏", centerX - 100, centerY + 190, 0.0f, 1.0f, 0.0f, 1.0f);
        }
    }
    
    private void handleMenuSelection() {
        switch (selectedOption) {
            case 0: // 新游戏
                startNewGame();
                break;
            case 1: // 加载存档
                if (hasSaveFile) {
                    loadGame();
                }
                break;
            case 2: // 退出游戏
                exitGame();
                break;
        }
    }
    
    private void startNewGame() {
        System.out.println("开始新游戏");
        isActive = false;
        
        // 通知场景开始新游戏
        if (scene instanceof GameScene) {
            ((GameScene) scene).startNewGame();
        }
    }
    
    private void loadGame() {
        System.out.println("加载存档");
        
        // 创建文件选择器
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("resources/saves"));
        fileChooser.setDialogTitle("选择存档文件");
        
        // 设置文件过滤器，只显示JSONL文件
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSONL存档文件", "jsonl");
        fileChooser.setFileFilter(filter);
        
        // 显示文件选择对话框
        int result = fileChooser.showOpenDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("选择的存档文件: " + selectedFile.getAbsolutePath());
            
            // 通知场景加载游戏
            if (scene instanceof GameScene) {
                ((GameScene) scene).loadGameFromFile(selectedFile.getAbsolutePath());
            }
        } else {
            System.out.println("用户取消了文件选择");
        }
    }
    
    private void exitGame() {
        System.out.println("退出游戏");
        System.exit(0);
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    // 内部接口，用于GameScene回调
    public interface GameScene {
        void startNewGame();
        void loadGame();
        void loadGameFromFile(String filePath);
    }
}
