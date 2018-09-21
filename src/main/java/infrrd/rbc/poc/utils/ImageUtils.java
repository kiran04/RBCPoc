package infrrd.rbc.poc.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.springframework.util.FileCopyUtils;

public class ImageUtils {
	
	
	public static void saveImage(String filePath, InputStream uploadedInputStream) throws IOException {
		File file = new File(filePath);
		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
		FileCopyUtils.copy(uploadedInputStream, stream);
		stream.close();
		uploadedInputStream.close();
	}

	public static void saveImage(String filePath, BufferedImage image) throws IOException {
		// extracts extension of output file
		String formatName = filePath.substring(filePath.lastIndexOf(".") + 1);

		// writes to output file
		ImageIO.write(image, formatName, new File(filePath));
	}

	public static BufferedImage resize(String inputImagePath, int scaledWidth, int scaledHeight) throws IOException {
		// reads input image
		File inputFile = new File(inputImagePath);
		BufferedImage inputImage = ImageIO.read(inputFile);
		return resize(inputImage, scaledWidth, scaledHeight);
	}

	public static BufferedImage resize(BufferedImage inputImage, int scaledWidth, int scaledHeight) throws IOException {
		// creates output image
		BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

		// scales the input image to the output image
		Graphics2D g2d = outputImage.createGraphics();
		g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
		g2d.dispose();

		return outputImage;
	}

	public static BufferedImage resize(String inputImagePath, double percent) throws IOException {
		File inputFile = new File(inputImagePath);
		BufferedImage inputImage = ImageIO.read(inputFile);
		return resize(inputImage, percent);
	}

	public static BufferedImage resize(BufferedImage inputImage, double percent) throws IOException {
		int scaledWidth = (int) (inputImage.getWidth() * percent);
		int scaledHeight = (int) (inputImage.getHeight() * percent);
		return resize(inputImage, scaledWidth, scaledHeight);
	}

	/**
	 * crops image accordingly a rectangular region
	 * 
	 * @param image
	 *            the image to crop
	 * @param x
	 *            the X coordinate of the upper-left corner of the specified
	 *            rectangular region
	 * @param y
	 *            the Y coordinate of the upper-left corner of the specified
	 *            rectangular region
	 * @param width
	 *            the width of the specified rectangular region
	 * @param height
	 *            the height of the specified rectangular region
	 * @return cropped image
	 */
	public static BufferedImage cropImage(BufferedImage image, int x, int y, int width, int height) {
		return image.getSubimage(x, y, width, height);
	}

	public static BufferedImage rotateImage(BufferedImage image, double angleOfRotation) {
		angleOfRotation = Math.toRadians(angleOfRotation);
		double sin = Math.abs(Math.sin(angleOfRotation)), cos = Math.abs(Math.cos(angleOfRotation));
		int w = image.getWidth(), h = image.getHeight();
		int newwidth = (int) Math.floor(w * cos + h * sin), newheight = (int) Math.floor(h * cos + w * sin);

		BufferedImage rotatedImage = new BufferedImage(newwidth, newheight, image.getType());

		Graphics2D g = rotatedImage.createGraphics();
		g.translate((newwidth - w) / 2, (newheight - h) / 2);
		g.rotate(angleOfRotation, w / 2, h / 2);
		g.drawRenderedImage(image, null);
		g.dispose();

		return rotatedImage;
	}

}
