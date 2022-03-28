package it.noi.edisplay.utils;

import it.noi.edisplay.model.ImageField;
import it.noi.edisplay.model.ImageFieldType;
import it.noi.edisplay.model.Resolution;

import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ImageUtil {

    public byte[] convertToMonochrome(BufferedImage image) throws IOException {
        BufferedImage blackWhite = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = blackWhite.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(blackWhite, "png", baos);
        baos.flush();
        return baos.toByteArray();
    }

    public void drawImageTextFields(BufferedImage bImage, List<ImageField> fields,
            Map<ImageFieldType, String> dynamicFieldValues) {
        Graphics g = bImage.getGraphics();

        g.setColor(Color.BLACK);
        Font currentFont = g.getFont();
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.FAMILY, currentFont.getFamily());

        for (ImageField field : fields) {
            attributes.put(TextAttribute.SIZE, field.getFontSize());
            g.setFont(Font.getFont(attributes));

            String stringToDraw = '<' + field.getFieldType().toString() + '>';

            if (field.getFieldType() == ImageFieldType.CUSTOM_TEXT) {
                stringToDraw = Objects.toString(field.getCustomText(), "");
            } else if (dynamicFieldValues != null) {
                stringToDraw = dynamicFieldValues.getOrDefault(field.getFieldType(), stringToDraw);
            }

            drawStringMultiLine(g, stringToDraw, bImage.getWidth() - field.getxPos(), field.getHeight(),
                    field.getxPos(), field.getyPos());
        }

        g.dispose();
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

    private void drawStringMultiLine(Graphics g, String text, int maxLineWidth, int maxHeight, int x, int y) {
        maxLineWidth = maxLineWidth - 12;
        FontMetrics m = g.getFontMetrics();
        if (m.stringWidth(text) < maxLineWidth) {
            g.drawString(text, x, y);
        } else {
            String[] words = text.split(" ");
            String currentLine = words[0];
            int lineCount = 1;
            for (int i = 1; i < words.length; i++) {
                if (m.stringWidth(currentLine + words[i]) < maxLineWidth) {
                    // There is enough space in line, append text
                    currentLine += " " + words[i];
                } else {
                    // Not enough space in line, check if there's enough height for new line
                    if ((lineCount + 1) * m.getHeight() > maxHeight) {
                        // Not enough height, append three dots to the current line and finish the operation
                        currentLine = appendThreeDots(m, currentLine, maxLineWidth);
                        g.drawString(currentLine, x, y);
                        return;
                    }

                    g.drawString(currentLine, x, y);
                    y += m.getHeight();
                    lineCount++;
                    currentLine = words[i];
                }
            }
            if (currentLine.trim().length() > 0) {
                g.drawString(currentLine, x, y);
            }
        }
    }

    private String appendThreeDots(FontMetrics m, String text, int maxWidth) {
        int textLenght = text.length();
        if (m.stringWidth(text + "...") < maxWidth) {
            text = text + "...";
        } else if (textLenght >= 3) {
            text = text.substring(0, textLenght - 3) + "...";
        }
        return text;
    }

    private static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
        return Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, width, height);
    }

}
