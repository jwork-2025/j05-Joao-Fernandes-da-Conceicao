package com.gameengine.components;

import com.gameengine.core.Component;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Image;

public class SpriteComponent extends Component<SpriteComponent> {
    private transient BufferedImage image; // BufferedImage is not serializable, mark as transient
    private String imagePath;
    private int width;
    private int height;

    public SpriteComponent(String imagePath, int width, int height) {
        this.imagePath = imagePath;
        this.width = width;
        this.height = height;
    }

    @Override
    public void initialize() {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            System.err.println("Error loading image: " + imagePath);
            e.printStackTrace();
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // Placeholder
        }
    }

    @Override
    public void update(float deltaTime) {
        // Sprites do not have update logic by default
    }

    @Override
    public void render() {
        // Render logic is handled by the Renderer
    }

    public Image getImage() {
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
