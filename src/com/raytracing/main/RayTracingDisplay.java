package com.raytracing.main;

import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.raytracing.tracer.RayTracer;

public class RayTracingDisplay extends JFrame {
    private static final long serialVersionUID = 1795903761475859536L;

    static class JImagePanel extends JPanel {
        private static final long serialVersionUID = -7718612494550027050L;
        private BufferedImage image;

        public JImagePanel(BufferedImage image) {
            super(true);
            this.setFocusable(false);
            this.image = image;
        }

        public void setNewImage(BufferedImage image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
        }
    }

    private BufferedImage image;
    private Camera camera;
    private JImagePanel imagePanel;

    public RayTracingDisplay(RayTracer rayTracer, boolean screenToUpdate) {
        super("Ray Tracing");
        this.camera = new Camera();
        this.image = rayTracer.getBufferedImage(camera.getEyePosition(), camera.getUpVector(),
                camera.getForwardVector(), camera.getRightVector());

        imagePanel = new JImagePanel(image);
        this.add(imagePanel);

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        dispose();
                        e.consume();
                        break;
                    case KeyEvent.VK_LEFT:
                        camera.rotateYaw(Camera.ROTATATION_ANGLE);
                        break;
                    case KeyEvent.VK_RIGHT:
                        camera.rotateYaw(-Camera.ROTATATION_ANGLE);
                        break;
                    case KeyEvent.VK_UP:
                        camera.rotatePitch(Camera.ROTATATION_ANGLE);
                        break;
                    case KeyEvent.VK_DOWN:
                        camera.rotatePitch(-Camera.ROTATATION_ANGLE);
                        break;
                    case KeyEvent.VK_COMMA:
                        camera.rotateRoll(-Camera.ROTATATION_ANGLE);
                        break;
                    case KeyEvent.VK_PERIOD:
                        camera.rotateRoll(Camera.ROTATATION_ANGLE);
                        break;
                    case KeyEvent.VK_W:
                        camera.walk(Camera.WALK_LENGTH);
                        break;
                    case KeyEvent.VK_S:
                        camera.walk(-Camera.WALK_LENGTH);
                        break;
                    case KeyEvent.VK_A:
                        camera.straf(-Camera.WALK_LENGTH);
                        break;
                    case KeyEvent.VK_D:
                        camera.straf(Camera.WALK_LENGTH);
                        break;
                    case KeyEvent.VK_OPEN_BRACKET:
                        camera.fly(-Camera.WALK_LENGTH);
                        break;
                    case KeyEvent.VK_CLOSE_BRACKET:
                        camera.fly(Camera.WALK_LENGTH);
                        break;
                    case KeyEvent.VK_0:
                        camera.resetCamera();
                        break;
                    case KeyEvent.VK_G:
                        rayTracer.setFastRenderMode(!rayTracer.isAtFastRenderMode());
                        break;
                        
                    case KeyEvent.VK_H:
                        rayTracer.increaseAntiAliasingSamples();
                        break;
                        
                    case KeyEvent.VK_J:
                        rayTracer.resetAntiAliasingSamples();
                        break;
                }

                rayTracer.interruptCurrentWork();
                    
                image = rayTracer.getBufferedImage(camera.getEyePosition(), camera.getUpVector(),
                        camera.getForwardVector(), camera.getRightVector());
                
                if (image != null) {
                    imagePanel.setNewImage(image);
                    imagePanel.repaint();
                }
            }
        });

        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(rayTracer.getDimension(), rayTracer.getDimension());
        this.setResizable(false);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static void displayTracing(RayTracer rayTracer) {
        new RayTracingDisplay(rayTracer, true).setVisible(true);
    }
}