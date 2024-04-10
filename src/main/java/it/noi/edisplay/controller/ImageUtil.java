package it.noi.edisplay.controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import it.noi.edisplay.model.ImageField;

public class ImageUtil {

    public static String saveCanvas(List<ImageField> boxes) {
        int canvasWidth = 1000; // Set canvas width
        int canvasHeight = 800; // Set canvas height

        // Create a BufferedImage object
        BufferedImage canvasImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvasImage.createGraphics();

        // Set the background color to white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvasWidth, canvasHeight);

        // Draw boxes on the canvas
        for (ImageField box : boxes) {
            if ("img".equals(box.getCustomText())) {
                try {
                    // Load image from file or URL
                    Image image = ImageIO.read(new File(box.getImage()));
                    g2d.drawImage(image, box.getxPos(), box.getyPos(), box.getWidth(), box.getHeight(), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Draw a rectangle for text boxes with white background and border
                g2d.setColor(Color.WHITE);
                g2d.fillRect(box.getxPos(), box.getyPos(), box.getWidth(), box.getHeight());

                // Draw text
                g2d.setColor(Color.BLACK);
                Font font = new Font("SansSerif", box.isBold() ? Font.BOLD : Font.PLAIN,
                        box.isItalic() ? box.getFontSize() : box.getFontSize());
                g2d.setFont(font);
                g2d.drawString(box.getCustomText(), box.getxPos(), box.getyPos() + box.getFontSize());

                // Draw border if specified
                if (box.isBorder()) {
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRect(box.getxPos(), box.getyPos(), box.getWidth(), box.getHeight());
                }
            }
        }

        // Dispose of the graphics context
        g2d.dispose();

        // Save the canvas image to a file
        File output = new File("canvas_image.png");
        try {
            ImageIO.write(canvasImage, "png", output);
            return output.getAbsolutePath(); // Return the path to the saved image
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Define a class to represent a canvas box

}
