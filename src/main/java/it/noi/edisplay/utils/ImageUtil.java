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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


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
					result.append(bufferedImage.getRGB(j, i) == -1 ? 0 : 1);
				else
					result.append(bufferedImage.getRGB(j, i) == -1 ? 1 : 0);

		return result.toString();
	}

	public static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
		return Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, width, height);
	}


	public static byte[] getImageForEvent(EventDto eventDto, byte[] image) throws IOException {
		InputStream in = new ByteArrayInputStream(image);
		BufferedImage bufferedImage = ImageIO.read(in);

		Graphics graphics = bufferedImage.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.setFont(new Font("Monospaced", Font.PLAIN, 26));

		graphics.drawString(eventDto.getSpaceDesc(), 20, 50);

		//check if description fits, else split in two lines
		if (eventDto.getEventDescriptionEN().length() < 35)
			graphics.drawString(eventDto.getEventDescriptionEN(), 20, 225);
		else {
			StringBuilder firstLine = new StringBuilder();
			StringBuilder secondLine = new StringBuilder();
			String[] descriptions = eventDto.getEventDescriptionEN().split(" ");
			for (int i = 0; i < descriptions.length; i++) {
				if (i < descriptions.length / 2)
					firstLine.append(descriptions[i]).append(" ");
				else
					secondLine.append(descriptions[i]).append(" ");
			}
			graphics.drawString(firstLine.toString(), 20, 200);
			graphics.drawString(secondLine.toString(), 20, 250);
		}
		graphics.setFont(new Font("Monospaced", Font.PLAIN, 18));


		//check if  company name fits, else split in two lines
		if (eventDto.getCompanyName().length() < 35)
			graphics.drawString(eventDto.getCompanyName(), 20, 90);
		else {
			StringBuilder firstLine = new StringBuilder();
			StringBuilder secondLine = new StringBuilder();
			String[] descriptions = eventDto.getCompanyName().split(" ");
			for (int i = 0; i < descriptions.length; i++) {
				if (i < descriptions.length / 2)
					firstLine.append(descriptions[i]).append(" ");
				else
					secondLine.append(descriptions[i]).append(" ");
			}
			graphics.drawString(firstLine.toString(), 20, 90);
			graphics.drawString(secondLine.toString(), 20, 130);
		}


		graphics.setFont(new Font("Monospaced", Font.PLAIN, 20));

		DateFormat formatter = new SimpleDateFormat("HH:mm");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		graphics.drawString(formatter.format(eventDto.getEventStartDateUTC() + 7200000), 400, 330); //TODO replace 7200000 with correct time conversion
		graphics.drawString("to", 470, 330);
		graphics.drawString(formatter.format(eventDto.getEventEndDateUTC() + 7200000), 500, 330);

		DateFormat dayFormatter = new SimpleDateFormat("dd/MM");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		graphics.drawString(dayFormatter.format(eventDto.getEventStartDateUTC() + 7200000), 320, 330);

		return convertToMonochrome(bufferedImage);
	}

	public static byte[] getImageForEmptyEventDisplay(String roomName, byte[] image) throws IOException {
		InputStream in = new ByteArrayInputStream(image);
		BufferedImage bufferedImage = ImageIO.read(in);

		Graphics graphics = bufferedImage.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.setFont(new Font("Monospaced", Font.PLAIN, 30));
		graphics.drawString(roomName, 20, 50);


		return convertToMonochrome(bufferedImage);
	}
}
