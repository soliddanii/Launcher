package com.skcraft.launcher.m4e.utils;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;


public class ResourceUtils {
    
    private static final String resourcesLocation = "com/skcraft/launcher/m4e/";
    
     public static Font getMinecraftFont(int size) {
        Font minecraft;
        try {
            minecraft = Font.createFont(Font.TRUETYPE_FONT, ResourceUtils.class.getClassLoader().getResourceAsStream(resourcesLocation+"minecraft.ttf")).deriveFont((float) size);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            // Fallback
            minecraft = new Font("Arial", Font.PLAIN, 12);
        }
        return minecraft;
    }

    public static Font getFrameFont(int size) {
        Font font;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, ResourceUtils.class.getClassLoader().getResourceAsStream(resourcesLocation+"Raleway-ExtraLight.ttf")).deriveFont((float) size);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            // Fallback
            font = new Font("Arial", Font.PLAIN, 12);
        }
        return font;
    }
    
    public static void setIcon(JLabel label, String iconName, int w, int h) {
        try {
            label.setIcon(new ImageIcon(scaleImage(ImageIO.read(ResourceUtils.class.getClassLoader().getResourceAsStream(resourcesLocation+iconName)), w, h)));
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

    }
    
    public static void setIcon(JButton button, String iconName, int size) {
        try {
            button.setIcon(new ImageIcon(ResourceUtils.scaleImage(ImageIO.read(ResourceUtils.class.getClassLoader().getResourceAsStream(resourcesLocation+iconName)), size, size)));
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
   
    public static BufferedImage scaleImage(BufferedImage img, int width, int height) {
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(img, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return newImage;
    }

    public static BufferedImage scaleWithAspectWidth(BufferedImage img, int width) {
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        int height = imgHeight * width / imgWidth;
        return scaleImage(img, width, height);
    }

    public static ImageIcon getIcon(String iconName) {
        return new ImageIcon(ResourceUtils.class.getClassLoader().getResource(resourcesLocation+iconName));
    }

    public static ImageIcon getIcon(String iconName, int w, int h) {
        try {
            return new ImageIcon(scaleImage(ImageIO.read(ResourceUtils.class.getClassLoader().getResourceAsStream(resourcesLocation+iconName)), w, h));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    public static BufferedImage getImage(String imageName) {
        try {
            return ImageIO.read(ResourceUtils.class.getClassLoader().getResourceAsStream(resourcesLocation+imageName));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

}
