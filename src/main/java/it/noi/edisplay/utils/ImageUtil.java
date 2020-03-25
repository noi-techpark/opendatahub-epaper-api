package it.noi.edisplay.utils;

import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.model.Resolution;
import org.imgscalr.Scalr;

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

	public static String getBinaryImage(byte[] image, boolean inverted, Resolution resolution) throws IOException {
		StringBuilder result = new StringBuilder();
		InputStream in = new ByteArrayInputStream(image);
		BufferedImage bufferedImage = ImageIO.read(in);

		if (bufferedImage.getHeight() != resolution.getHeight() || bufferedImage.getWidth() != resolution.getWidth())
			bufferedImage = getScaledImage(bufferedImage, resolution.getWidth(), resolution.getHeight());

		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				if (inverted)
					result.append(bufferedImage.getRGB(j, i) == -1 ? 1 : 0);
				else
					result.append(bufferedImage.getRGB(j, i) == -1 ? 0 : 1);
		return result.toString();
	}

	public static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
		return Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, width, height);
	}


	public static byte[] getImageForEvent(EventDto eventDto){

		return  null;
	}
}
