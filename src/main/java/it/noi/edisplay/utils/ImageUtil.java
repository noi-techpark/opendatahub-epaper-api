package it.noi.edisplay.utils;

import it.noi.edisplay.model.ImageField;
import it.noi.edisplay.model.ImageFieldType;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.*;
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

    public static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
        return Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, width, height);
    }

    public void setImageFields(BufferedImage bImage, List<ImageField> fields,
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

            g.drawString(stringToDraw, field.getxPos(), field.getyPos());
        }

        g.dispose();
    }

    public byte[] convertToByteArray(BufferedImage image, boolean to24bitBMP) throws IOException {
        BufferedImage outputImage;
        String format;
        if (to24bitBMP) {
            format = "BMP";
            outputImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

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
}
