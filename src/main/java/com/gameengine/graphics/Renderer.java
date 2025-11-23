package com.gameengine.graphics;

import com.gameengine.input.InputManager;
import com.gameengine.math.Vector2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * 渲染器
 */
public class Renderer extends JFrame {
    private int width;
    private int height;
    private String title;
    private GamePanel gamePanel;
    private InputManager inputManager;
    
    public Renderer(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.inputManager = InputManager.getInstance();
        
        initialize();
    }
    
    private void initialize() {
        setTitle(title);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        gamePanel = new GamePanel();
        add(gamePanel);
        
        setupInput();
        
        setVisible(true);
    }
    
    private void setupInput() {
        // 键盘输入
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                inputManager.onKeyPressed(e.getKeyCode());
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                inputManager.onKeyReleased(e.getKeyCode());
            }
        });
        
        // 鼠标输入
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                inputManager.onMousePressed(e.getButton());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                inputManager.onMouseReleased(e.getButton());
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                inputManager.onMouseMoved(e.getX(), e.getY());
            }
        });
        
        setFocusable(true);
        requestFocus();
    }
    
    /**
     * 开始渲染帧
     */
    public void beginFrame() {
        gamePanel.clear();
    }
    
    /**
     * 结束渲染帧
     */
    public void endFrame() {
        gamePanel.repaint();
    }
    
    /**
     * 绘制矩形
     */
    public void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {
        gamePanel.addDrawable(new RectDrawable(x, y, width, height, r, g, b, a));
    }
    
    /**
     * 绘制圆形
     */
    public void drawCircle(float x, float y, float radius, int segments, float r, float g, float b, float a) {
        gamePanel.addDrawable(new CircleDrawable(x, y, radius, r, g, b, a));
    }
    
    /**
     * 绘制线条
     */
    public void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
        gamePanel.addDrawable(new LineDrawable(x1, y1, x2, y2, r, g, b, a));
    }

    /**
     * 绘制图像
     */
    public void drawImage(Image image, float x, float y, int width, int height) {
        gamePanel.addDrawable(new ImageDrawable(image, x, y, width, height));
    }
    
    /**
     * 绘制进度条
     */
    public void drawProgressBar(float x, float y, float width, float height, float progress, float r, float g, float b, float a) {
        gamePanel.addDrawable(new ProgressBarDrawable(x, y, width, height, progress, r, g, b, a));
    }
    
    /**
     * 绘制文本
     */
    public void drawText(String text, float x, float y, float r, float g, float b, float a) {
        gamePanel.addDrawable(new TextDrawable(text, x, y, r, g, b, a));
    }
    
    /**
     * 绘制渐变圆形
     */
    public void drawGradientCircle(float x, float y, float radius, float alpha, float r, float g, float b) {
        gamePanel.addDrawable(new GradientCircleDrawable(x, y, radius, alpha, r, g, b));
    }
    
    /**
     * 绘制粒子系统
     */
    public void drawParticles(com.gameengine.components.ParticleSystemComponent particleSystem) {
        if (particleSystem.isActive()) {
            for (com.gameengine.components.ParticleSystemComponent.Particle particle : particleSystem.getParticles()) {
                gamePanel.addDrawable(new ParticleDrawable(particle));
            }
        }
    }
    
    /**
     * 检查窗口是否应该关闭
     */
    public boolean shouldClose() {
        return !isVisible();
    }
    
    /**
     * 处理事件
     */
    public void pollEvents() {
        // Swing自动处理事件
    }
    
    /**
     * 清理资源
     */
    public void cleanup() {
        dispose();
    }
    
    // Getters
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public String getTitle() {
        return title;
    }
    
    /**
     * 游戏面板类
     */
    private class GamePanel extends JPanel {
        private List<Drawable> drawables = new ArrayList<>();
        
        public GamePanel() {
            setPreferredSize(new Dimension(width, height));
            setBackground(Color.BLACK);
        }
        
        public void clear() {
            drawables.clear();
        }
        
        public void addDrawable(Drawable drawable) {
            drawables.add(drawable);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            for (Drawable drawable : drawables) {
                drawable.draw(g2d);
            }
        }
    }
    
    /**
     * 可绘制对象接口
     */
    private interface Drawable {
        void draw(Graphics2D g);
    }
    
    /**
     * 矩形绘制类
     */
    private static class RectDrawable implements Drawable {
        private float x, y, width, height;
        private Color color;
        
        public RectDrawable(float x, float y, float width, float height, float r, float g, float b, float a) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = new Color(r, g, b, a);
        }
        
        @Override
        public void draw(Graphics2D g) {
            g.setColor(color);
            g.fillRect((int) x, (int) y, (int) width, (int) height);
        }
    }
    
    /**
     * 圆形绘制类
     */
    private static class CircleDrawable implements Drawable {
        private float x, y, radius;
        private Color color;
        
        public CircleDrawable(float x, float y, float radius, float r, float g, float b, float a) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = new Color(r, g, b, a);
        }
        
        @Override
        public void draw(Graphics2D g) {
            g.setColor(color);
            g.fillOval((int) (x - radius), (int) (y - radius), (int) (radius * 2), (int) (radius * 2));
        }
    }
    
    /**
     * 线条绘制类
     */
    private static class LineDrawable implements Drawable {
        private float x1, y1, x2, y2;
        private Color color;
        
        public LineDrawable(float x1, float y1, float x2, float y2, float r, float g, float b, float a) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.color = new Color(r, g, b, a);
        }
        
        @Override
        public void draw(Graphics2D g) {
            g.setColor(color);
            g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        }
    }

    /**
     * 图像绘制类
     */
    private static class ImageDrawable implements Drawable {
        private Image image;
        private float x, y;
        private int width, height;

        public ImageDrawable(Image image, float x, float y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        @Override
        public void draw(Graphics2D g) {
            if (image != null) {
                g.drawImage(image, (int) x, (int) y, width, height, null);
            }
        }
    }
    
    /**
     * 进度条绘制类
     */
    private static class ProgressBarDrawable implements Drawable {
        private float x, y, width, height, progress;
        private Color color;
        
        public ProgressBarDrawable(float x, float y, float width, float height, float progress, float r, float g, float b, float a) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.progress = Math.max(0, Math.min(1, progress)); // 限制在0-1之间
            this.color = new Color(r, g, b, a);
        }
        
        @Override
        public void draw(Graphics2D g) {
            // 绘制背景
            g.setColor(new Color(0.2f, 0.2f, 0.2f, 1.0f));
            g.fillRect((int) x, (int) y, (int) width, (int) height);
            
            // 绘制进度
            g.setColor(color);
            float progressWidth = width * progress;
            g.fillRect((int) x, (int) y, (int) progressWidth, (int) height);
            
            // 绘制边框
            g.setColor(Color.WHITE);
            g.drawRect((int) x, (int) y, (int) width, (int) height);
        }
    }
    
    /**
     * 文本绘制类
     */
    private static class TextDrawable implements Drawable {
        private String text;
        private float x, y;
        private Color color;
        
        public TextDrawable(String text, float x, float y, float r, float g, float b, float a) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.color = new Color(r, g, b, a);
        }
        
        @Override
        public void draw(Graphics2D g) {
            g.setColor(color);
            // 使用支持中文的字体
            Font font = new Font("Microsoft YaHei", Font.BOLD, 14);
            if (font.canDisplay('中')) {
                g.setFont(font);
            } else {
                // 如果微软雅黑不可用，尝试其他中文字体
                font = new Font("SimSun", Font.BOLD, 14);
                if (font.canDisplay('中')) {
                    g.setFont(font);
                } else {
                    // 使用系统默认字体
                    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                }
            }
            g.drawString(text, (int) x, (int) y);
        }
    }
    
    /**
     * 渐变圆形绘制类
     */
    private static class GradientCircleDrawable implements Drawable {
        private float x, y, radius, alpha;
        private Color color;
        
        public GradientCircleDrawable(float x, float y, float radius, float alpha, float r, float g, float b) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.alpha = Math.max(0, Math.min(1, alpha)); // 限制在0-1之间
            this.color = new Color(r, g, b, alpha);
        }
        
        @Override
        public void draw(Graphics2D g) {
            // 创建径向渐变
            java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(x, y);
            float[] dist = {0.0f, 1.0f};
            Color[] colors = {
                new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255)), // 中心
                new Color(color.getRed(), color.getGreen(), color.getBlue(), 0) // 边缘透明
            };
            RadialGradientPaint gradient = new RadialGradientPaint(center, radius, dist, colors);
            
            g.setPaint(gradient);
            g.fillOval((int)(x - radius), (int)(y - radius), (int)(radius * 2), (int)(radius * 2));
        }
    }
    
    /**
     * 粒子绘制类
     */
    private static class ParticleDrawable implements Drawable {
        private com.gameengine.components.ParticleSystemComponent.Particle particle;
        
        public ParticleDrawable(com.gameengine.components.ParticleSystemComponent.Particle particle) {
            this.particle = particle;
        }
        
        @Override
        public void draw(Graphics2D g) {
            float[] color = particle.color;
            float alpha = particle.getAlpha();
            float size = particle.getSize();
            
            g.setColor(new Color(color[0], color[1], color[2], alpha));
            g.fillOval((int)(particle.position.x - size/2), (int)(particle.position.y - size/2), 
                      (int)size, (int)size);
        }
    }
}
