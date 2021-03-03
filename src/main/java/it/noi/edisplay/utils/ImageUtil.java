package it.noi.edisplay.utils;

import it.noi.edisplay.dto.EventDto;
import it.noi.edisplay.model.Resolution;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBuffer;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


@Component
public class ImageUtil {

	private static final String DEFAULT_FONT = "Terminus";


	public byte[] convertToMonochrome(BufferedImage image) throws IOException {
		BufferedImage blackWhite = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g2d = blackWhite.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(blackWhite, "png", baos);
		baos.flush();
		return baos.toByteArray();
	}

	public  String getBinaryImage(byte[] image, boolean inverted, Resolution resolution) throws IOException {
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

	public byte[] getBMPfromImage(byte[] bytes) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		InputStream in = new ByteArrayInputStream(bytes);
		BufferedImage image = ImageIO.read(in);

		// writes to the output image in specified format
		ImageIO.write(image, "BMP", outputStream);
		outputStream.flush();
		return  outputStream.toByteArray();
	}


	public String getCodeFromImage(byte[] bytes) throws IOException {

		InputStream in = new ByteArrayInputStream(bytes);
		BufferedImage image = ImageIO.read(in);


		BufferedImage blackWhite = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
		ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		op.filter(image, blackWhite);

		DataBuffer buf = blackWhite.getRaster().getDataBuffer();
		int byteWidth = (image.getWidth() + 7) /8;
		StringBuffer str = new StringBuffer();
//		str.append("{\n");
		int i=0,max=buf.getSize();
		while(i < max) {
			for(int j=0;j<byteWidth;++j) {
				str.append("0x"+Integer.toString(buf.getElem(i++),16)+(i != max ?",":""));
			}
			str.append("\n");
		}
//		str.append("};\n");
		return str.toString();
	}

	public static BufferedImage getScaledImage(BufferedImage image, int width, int height) {
		return Scalr.resize(image, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_EXACT, width, height);
	}


	public  byte[] getImageForEvent(EventDto eventDto, byte[] image) throws IOException, FontFormatException {
		InputStream in = new ByteArrayInputStream(image);
		BufferedImage bufferedImage = ImageIO.read(in);


		Graphics graphics = bufferedImage.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 26));

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
		graphics.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 18));


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


		graphics.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 20));

		DateFormat formatter = new SimpleDateFormat("HH:mm");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		graphics.drawString(formatter.format(eventDto.getRoomStartDateUTC()+ 7200000), 400, 330); //TODO replace 7200000 with correct time conversion
		graphics.drawString("to", 470, 330);
		graphics.drawString(formatter.format(eventDto.getRoomEndDateUTC() + 7200000), 500, 330);

		DateFormat dayFormatter = new SimpleDateFormat("dd/MM");
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		graphics.drawString(dayFormatter.format(eventDto.getRoomStartDateUTC() + 7200000), 320, 330);

		return convertToMonochrome(bufferedImage);
	}

	public byte[] getImageForEmptyEventDisplay(String roomName, byte[] image) throws IOException {
		InputStream in = new ByteArrayInputStream(image);
		BufferedImage bufferedImage = ImageIO.read(in);

		Graphics graphics = bufferedImage.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.setFont(new Font(DEFAULT_FONT, Font.PLAIN, 30));
		graphics.drawString(roomName, 20, 50);


		return convertToMonochrome(bufferedImage);
	}
}
