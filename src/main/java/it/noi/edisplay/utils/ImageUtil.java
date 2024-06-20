// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.noi.edisplay.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;

import it.noi.edisplay.model.ImageField;
import it.noi.edisplay.model.Resolution;
import it.noi.edisplay.model.Template;

@Component
public class ImageUtil {

    public int fixValue(int value) {
        return (int) (value * 1.82);
    }

    public byte[] convertToMonochrome(BufferedImage image) throws IOException {
        BufferedImage blackWhite = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = blackWhite.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(blackWhite, "png", baos);
        baos.flush();
        return baos.toByteArray();
    }

    private static BufferedImage invertImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Create a new BufferedImage to hold the inverted image
        BufferedImage invertedImage = new BufferedImage(width, height, image.getType());

        // Process each pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the RGB value of the current pixel
                int rgb = image.getRGB(x, y);

                // Invert the color
                int invertedRgb = ~rgb | 0xFF000000; // Invert and keep alpha

                // Set the new RGB value to the inverted image
                invertedImage.setRGB(x, y, invertedRgb);
            }
        }

        return invertedImage;
    }

    public String drawImageTextFields(List<ImageField> boxes, int width, int height, Template template) {
        int canvasWidth = (width);
        int canvasHeight = (height);
        boolean invert = template.getRoomData()[3] == 1 ? true : false;
        BufferedImage canvasImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = canvasImage.createGraphics();

        // Set the background color to white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvasWidth, canvasHeight);

        // Draw boxes on the canvas
        for (ImageField box : boxes) {
            if ("img".equals(box.getCustomText())) {
                String base64Image = box.getImage().split(",")[1];
                // Decode the Base64 string into a byte array
                byte[] imageData = Base64.getDecoder().decode(base64Image);
                try {
                    // Create an image from the decoded byte array
                    Image image = ImageIO.read(new ByteArrayInputStream(imageData));
                    g2d.drawImage(image, fixValue(box.getxPos()), (box.getyPos()), fixValue(box.getWidth()),
                            (box.getHeight() + 7), null);
                    if (box.isBorder()) {
                        g2d.setColor(Color.BLACK);
                        g2d.setStroke(new BasicStroke(3));
                        g2d.drawRect(fixValue(box.getxPos()), (box.getyPos()), fixValue(box.getWidth()),
                                (box.getHeight()) + 5);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Draw a rectangle for text boxes with white background and border
                Font currentFont = g2d.getFont();
                if (box.isInvert()) {
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(fixValue(box.getxPos()), (box.getyPos()), fixValue(box.getWidth() + 2),
                            (box.getHeight()) + 5);

                }

                if (box.isBorder()) {
                    g2d.setColor(Color.BLACK);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRect(fixValue(box.getxPos()), (box.getyPos()), fixValue(box.getWidth() + 2),
                            (box.getHeight()) + 5);

                }
                Map<TextAttribute, Object> attributes = new HashMap<>();
                attributes.put(TextAttribute.FAMILY, currentFont.getFamily());
                attributes.put(TextAttribute.SIZE, fixValue(box.getFontSize() - 2));
                if (box.isBold()) {
                    attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
                }
                if (box.isItalic()) {
                    attributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
                } else {
                    attributes.put(TextAttribute.POSTURE, TextAttribute.POSTURE_REGULAR);
                }

                g2d.setFont(Font.getFont(attributes));
                if (box.getCustomText().length() > 0 && box.isInvert()) {
                    g2d.setColor(Color.WHITE);
                    g2d = drawStringMultiLine(g2d, box.getCustomText(), fixValue(box.getWidth()), (box.getHeight()),
                            fixValue(box.getxPos()), (box.getyPos()));
                } else if (box.getCustomText().length() > 0) {
                    g2d.setColor(Color.BLACK);
                    g2d = drawStringMultiLine(g2d, box.getCustomText(), fixValue(box.getWidth()), (box.getHeight()),
                            fixValue(box.getxPos()), (box.getyPos()));

                }

            }
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.dispose();
        try (

                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (invert) {
                ImageIO.write(invertImage(canvasImage), "png", baos);
            } else {
                ImageIO.write(canvasImage, "png", baos);
            }

            baos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public byte[] convertToByteArray(BufferedImage image, boolean toNativeFormat, Resolution resolution)
            throws IOException {
        BufferedImage outputImage;
        String format;
        if (toNativeFormat && resolution != null) {
            format = "BMP";

            image = getScaledImage(image, resolution.getWidth(), resolution.getHeight());

            switch (resolution.getBitDepth()) {
            case 4:
                byte[] v = new byte[1 << 4];
                for (int i = 0; i < v.length; ++i)
                    v[i] = (byte) (i * 17);
                ColorModel cm = new IndexColorModel(4, v.length, v, v, v);
                WritableRaster wr = cm.createCompatibleWritableRaster(image.getWidth(), image.getHeight());
                outputImage = new BufferedImage(cm, wr, false, null);
                break;
            case 24:
                outputImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                break;
            default:
                outputImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            }

            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
        } else {
            format = "PNG";
            outputImage = image;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(outputImage, format, baos);
        return baos.toByteArray();
    }

    public String convertToMD5Hash(byte[] image) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(image);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; ++i) {
            sb.append(Integer.toHexString((digest[i] & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    public Graphics2D drawStringMultiLine(Graphics2D g, String text, int maxLineWidth, int maxHeight, int x, int y) {
        FontMetrics m = g.getFontMetrics();

        int availableLines = maxHeight / m.getHeight();
        if (availableLines == 0) {
            availableLines = 1;
        }
        String[] linesToDraw = splitTextIntoLines(m, text, maxLineWidth, availableLines);

        for (String lineToDraw : linesToDraw) {
            if (lineToDraw != null && lineToDraw.trim().length() > 0) {
                y += m.getHeight();
                g.drawString(lineToDraw, x, y);

            }

        }
        return g;
    }

    public String[] splitTextIntoLines(FontMetrics m, String text, int maxWidth, int availableLines) {
        String[] lines = new String[availableLines];
        int currentLineIndex = 0;

        String[] enforcedLines = text.split("\\n");
        for (int i = 0; i < enforcedLines.length; i++) {
            String[] words = enforcedLines[i].split(" ");

            if (currentLineIndex < availableLines) {
                lines[currentLineIndex] = words[0];
            }

            for (int j = 1; j < words.length; j++) {
                if (currentLineIndex < availableLines && m.stringWidth(lines[currentLineIndex] + words[j]) < maxWidth) {
                    // There is enough space in line, append text
                    lines[currentLineIndex] += " " + words[j];
                } else {
                    // Not enough space, go to new line
                    currentLineIndex++;
                    if (currentLineIndex < availableLines) {
                        lines[currentLineIndex] = words[j];
                    } else {
                        // All lines are completed but there's still text to write, append three dots to
                        // the last line
                        lines[availableLines - 1] = appendThreeDots(m, lines[availableLines - 1], maxWidth);
                        return lines;
                    }
                }
            }
            currentLineIndex++;
        }
        return lines;
    }

    public String appendThreeDots(FontMetrics m, String text, int maxWidth) {
        int textLenght = text.length();
        if (m.stringWidth(text + "...") < maxWidth) {
            text = text + "...";
        } else if (textLenght >= 3) {
            text = text.substring(0, textLenght - 3) + "...";
        } else {
            text = "...";
        }
        return text;
    }

    private static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
        return Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, width, height);
    }

}
