package it.noi.edisplay.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ImageUtil {

    public static byte[] convertToMonochrome(BufferedImage image) throws IOException {
        BufferedImage blackWhite = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2d = blackWhite.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(blackWhite, "png", baos);
        baos.flush();
        return baos.toByteArray();
    }

    public static String getBinaryImage(byte[] image) {
        StringBuffer result = new StringBuffer();
        InputStream in = new ByteArrayInputStream(image);
        try {
            BufferedImage bufferedImage = ImageIO.read(in);

            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            for (int i = 0; i < height; i++)
                for (int j = 0; j < width; j++)
                    result.append(bufferedImage.getRGB(j, i) == -1 ? 0 : 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }


}
