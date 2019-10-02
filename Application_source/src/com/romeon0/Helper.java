package com.romeon0;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import java.lang.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.IntBuffer;
import java.util.*;
import java.util.List;

/**
 * Created by Romeon0 on 3/19/2018.
 */
public class Helper {
	public List<Pair<Integer, double[][]>> readDataFile(String path) {
		List<Pair<Integer, double[][]>> dataSet = new ArrayList<>();

		// open data file
		FileReader reader = null;
		try {
			reader = new FileReader(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		// read data file
		BufferedReader bReader = new BufferedReader(reader);
		String line;
		Scanner scanner;
		try {
			int nrPixels = 28 * 28;
			int imgSize = 28;
			while ((line = bReader.readLine()) != null) {// for (int a = 0; a < 1; ++a) { line = bReader.readLine();
				scanner = new Scanner(line);
				// Get pixels and answer. [0] - label, [1-x] - pixels values
				int[] pixels = new int[nrPixels];
				int labelId = scanner.nextInt();
				for (int nrPixel = 0; nrPixel < nrPixels; ++nrPixel) {
					pixels[nrPixel] = scanner.nextInt();
				}
				double[] answer = new double[26];
				for (int b = 0; b < 26; ++b)
					answer[b] = 0.0f;
				answer[labelId] = 1.0f;

				// normalizing data
				double[][] normalizedPixels = new double[imgSize][imgSize];
				for (int y = 0; y < imgSize; ++y) {
					for (int x = 0; x < imgSize; ++x) {
						normalizedPixels[y][x] = pixels[y * imgSize + x] / 255.0f;
					}
				}
				dataSet.add(new Pair<>(labelId, normalizedPixels));
			}
			bReader.close();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			dataSet = null;
			return null;
		}
		return dataSet;
	}

	public Image loadImageFromFile(String path) {
		File imgFile = new File(path);
		if (!imgFile.exists()) {
			System.out.println("File " + imgFile.getAbsolutePath() + " not exists.");
		}

		return new Image(imgFile.toURI().toString());
	}

	public void saveImageToFile(Image image) {
		File outputFile = new File("./files/images/" + System.currentTimeMillis() + ".png");
		BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
		try {
			ImageIO.write(bImage, "png", outputFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void saveImageToFile(Image image, String name) {
		File outputFile = null;
		try {
			String filePath = "./files/images/" + name + ".png";
			outputFile = new File(filePath);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (!outputFile.exists())
			try {
				outputFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
		try {
			ImageIO.write(bImage, "png", outputFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public double[][] poolMatrix2x2(double[][] matrix) {
		int kernelSize = 2;
		int srcImgWidth = (int) matrix[0].length;
		int srcImgHeight = (int) matrix.length;
		int destImgWidth = srcImgWidth / kernelSize;
		int destImgHeight = srcImgHeight / kernelSize;
		double[][] dest = new double[destImgHeight][destImgWidth];

		int maxX = srcImgWidth - 1;
		int maxY = srcImgHeight - 1;
		for (int y = 0, i = 0; y < maxY; y += 2, ++i) {
			for (int x = 0, j = 0; x < maxX; x += 2, ++j) {
				double currColor = 0.0;
				double maxColor = -1000.0;
				for (int a = 0; a < 2; ++a) {
					for (int b = 0; b < 2; ++b) {
						currColor = matrix[y + a][x + b];
						if (currColor > maxColor) {
							maxColor = currColor;
						}
					}
				}
				dest[i][j] = maxColor;
			}
		}
		return dest;
	}

	public double[][] convolvePixelMatrix3x3(double[][] matrix, double[][] filter) {
		int kernelSize = 3;
		int srcImgWidth = matrix[0].length;
		int srcImgHeight = matrix.length;
		int destImgWidth = srcImgWidth - kernelSize + 1;
		int destImgHeight = srcImgHeight - kernelSize + 1;
		double[][] dest = new double[destImgHeight][destImgWidth];
		for (int y = 1; y < srcImgHeight - 1; ++y) {
			for (int x = 1; x < srcImgWidth - 1; ++x) {
				// get pixels colors
				double[][] colors = new double[kernelSize][kernelSize];
				int currX = x - 1, currY = y - 1;
				for (int kernelY = 0; kernelY < kernelSize; ++kernelY) {
					for (int kernelX = 0; kernelX < kernelSize; ++kernelX) {
						double color = matrix[currY + kernelY][currX + kernelX];
						colors[kernelY][kernelX] = color;
					}
				}

				// multiply with kernel
				double val = 0;
				for (int a = 0; a < kernelSize; ++a) {
					for (int b = 0; b < kernelSize; ++b) {
						double color = colors[a][b];
						val += color * filter[a][b];
					}
				}

				// gradient magnitude
				/*
				 * double valR = (int) Math.abs(valXR) + Math.abs(valYR); double valG = (int)
				 * Math.abs(valXG) + Math.abs(valYG); double valB = (int) Math.abs(valXB) +
				 * Math.abs(valYB);
				 */

				// to 0-1 range
				if (val > 1.0f)
					val = 1.0f;
				if (val < 0.0f)
					val = 0.0f;

				dest[y - 1][x - 1] = val;
			}
		}

		return dest;
	}

	public double[][] fullConvolve3x3(double[][] img, double[][] kernel) {
		int kernelSize = 3;
		int srcImgWidth = img[0].length;
		int srcImgHeight = img.length;
		int destImgWidth = srcImgWidth + kernelSize - 1;
		int destImgHeight = srcImgHeight + kernelSize - 1;

		// rotate filter 180 degrees for full convolution
		kernel = flipKernel(kernel);
		Helper h = new Helper();

		double[][] output = new double[destImgHeight][destImgWidth];
		for (int y = 0; y < srcImgHeight + 2; ++y) {
			for (int x = 0; x < srcImgWidth + 2; ++x) {
				output[y][x] = 0.0;
				int kernelOriginY = y - 2;
				int kernelOriginX = x - 2;
				for (int kernelY = 0; kernelY < 3; ++kernelY) {
					for (int kernelX = 0; kernelX < 3; ++kernelX) {
						int currY = kernelOriginY + kernelY;
						int currX = kernelOriginX + kernelX;
						if (currY >= 0 && currY < srcImgHeight && currX >= 0 && currX < srcImgWidth) {
							output[y][x] += kernel[kernelY][kernelX]
									* img[kernelOriginY + kernelY][kernelOriginX + kernelX];
							// System.out.println(String.format("%d %d ::: %d %d",
							// kernelY,kernelX,
							// kernelOriginY + kernelY,kernelOriginX + kernelX));
						}
					}
					// System.out.println();
				}
			}
		}
		return output;
	}

	public boolean[][] maxPoolOp2x2(double v1, double v2, double v3, double v4) {
		int idx = 1;

		if (v1 > v2) { //
			if (v1 > v3) {
				if (v1 < v4)
					idx = 4;
			} else {
				idx = 3;
				if (v3 < v4)
					idx = 4;
			}
		} else { // v1<v2
			idx = 2;
			if (v2 > v3) {
				if (v2 < v4)
					idx = 4;
			} else {
				idx = 3;
				if (v3 < v4)
					idx = 4;
			}
		}

		boolean[][] matrix = new boolean[2][2];
		switch (idx) {
		case 1:
			matrix[0][0] = true;
			matrix[0][1] = false;
			matrix[1][0] = false;
			matrix[1][1] = false;
			break;
		case 2:
			matrix[0][0] = false;
			matrix[0][1] = true;
			matrix[1][0] = false;
			matrix[1][1] = false;
			break;
		case 3:
			matrix[0][0] = false;
			matrix[0][1] = false;
			matrix[1][0] = true;
			matrix[1][1] = false;
			break;
		case 4:
			matrix[0][0] = false;
			matrix[0][1] = false;
			matrix[1][0] = false;
			matrix[1][1] = true;
			break;
		}

		return matrix;
	}

	public Image to28x28(Image img) {
		int srcImgHeight = (int) img.getHeight();
		int srcImgWidth = (int) img.getWidth();
		double[][] imgMatrix = new double[srcImgHeight][srcImgWidth];

		// PixelReader pixelReader = img.getPixelReader();
		// WritableImage imgDestination = new WritableImage(destImgWidth,
		// destImgHeight);
		// PixelWriter pixelWriter = imgDestination.getPixelWriter();

		saveImageToFile(img, "tmp");
		Image resultImage = null;
		try {
			resultImage = new Image(new File("./files/images/tmp.png").toURI().toURL().toString(), 28.0, 28.0, false, true);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		saveImageToFile(resultImage, "tmp2");
		return resultImage;
	}

	public double[][] imageToMatrix(Image img) {
		int srcImgHeight = (int) img.getHeight();
		int srcImgWidth = (int) img.getWidth();
		double[][] imgMatrix = new double[srcImgHeight][srcImgWidth];

		PixelReader pixelReader = img.getPixelReader();
		PixelFormat format = pixelReader.getPixelFormat();
		// WritableImage imgDestination = new WritableImage(destImgWidth,
		// destImgHeight);
		// PixelWriter pixelWriter = imgDestination.getPixelWriter();

		img.getPixelReader().getPixelFormat();
		for (int y = 0; y < srcImgHeight; ++y) {
			for (int x = 0; x < srcImgWidth; ++x) {
				Color color = pixelReader.getColor(y, x);
				imgMatrix[y][x] = 1.0 - color.getBrightness();
			}
		}
		return imgMatrix;
	}

	public double[][] flipKernel(double[][] filter) {
		// 180 grade rotating
		int filterSizeY = filter.length;
		int filterSizeX = filter[0].length;
		double[][] rotatedFilter = new double[filterSizeY][filterSizeX];
		for (int filterY = 0; filterY < filterSizeY; ++filterY) {
			for (int filterX = 0; filterX < filterSizeX; ++filterX) {
				rotatedFilter[filterSizeY - 1 - filterY][filterSizeX - 1 - filterX] = filter[filterY][filterX];
			}
		}
		return rotatedFilter;
	}

	public void showMatrix(double[][] matrix) {
		for (int y = 0; y < matrix.length; ++y) {
			for (int x = 0; x < matrix[0].length; ++x) {
				System.out.print(String.format("%.7f ", matrix[y][x]));
			}
			System.out.println();
		}
		System.out.println("---------------");
	}

	public double[][] vectorToMatrix(double[] vector, int imgWidth, int imgHeight) throws IllegalArgumentException {
		int imgVolume = imgWidth * imgHeight;
		if (imgVolume > vector.length)
			throw new IllegalArgumentException();
		// new String[]{"Can't create image: vector too small! Needs/has:
		// ("+imgVolume+"/"+vector.length+")"});
		double[][] matrix = new double[imgHeight][imgWidth];
		for (int y = 0; y < imgHeight; ++y) {
			for (int x = 0; x < imgWidth; ++x) {
				matrix[y][x] = vector[y * imgWidth + x];
			}
		}
		return matrix;
	}

	public double[] matrixToVector(double[][] matrix) {
		int matriHeight = matrix.length;
		int matrixWidth = matrix[0].length;
		double[] vector = new double[matriHeight * matrixWidth];
		for (int a = 0; a < matriHeight; ++a) {
			for (int b = 0; b < matrixWidth; ++b) {
				vector[a * matrixWidth + b] = matrix[a][b];
			}
		}
		return vector;
	}

	public void showVector(double[] prevErrors) {
		for (int a = 0; a < prevErrors.length; ++a)
			System.out.print(String.format("%.7f ", prevErrors[a]));
		System.out.println();
	}

	// TODO delete or check
	private void segmentLetters(WritableImage img, Vector<Segment> segments) {
		// WritableImage img= cnvDrawField.snapshot(null, null);
		if (img.getHeight() == 0 || img.getWidth() == 0)
			return;

		PixelReader pixelReader = img.getPixelReader();

		int maxX = -1000, minX = 1000;
		int maxY = -1000, minY = 1000;
		boolean found = false;
		boolean isStarted = false, isEnd = false;
		// System.out.println(String.format("Image: %.2f x %.2f", img.getWidth(),
		// img.getHeight()));
		Segment segment = new Segment();
		for (int x = 0; x < img.getWidth(); ++x) {
			found = false;
			for (int y = 0; y < img.getHeight(); ++y) {
				Color pixelColor = pixelReader.getColor(x, y);

				if (pixelColor.getRed() >= 0.50 && pixelColor.getGreen() <= 0.10 && pixelColor.getBlue() <= 0.10)
					break;
				else if (pixelColor.getRed() <= 0.10 && pixelColor.getGreen() <= 0.10 && pixelColor.getBlue() <= 0.10) {
					if (!isStarted) {// start segment
						isStarted = true;
						isEnd = false;
					}
					if (maxX < x)
						maxX = x;
					if (maxY < y)
						maxY = y;
					if (minX > x)
						minX = x;
					if (minY > y)
						minY = y;

					found = true;
				}

			}

			// if no pixel in column then the letter ends
			if (!found && isStarted) {
				isEnd = true;
			}
			// if letters end then add coordinates
			if (!found && isStarted && isEnd) {
				isStarted = false;
				isEnd = false;
				maxY += 1;
				segment.p0 = new Point(minX, minY);
				segment.p1 = new Point(maxX, minY);
				segment.p2 = new Point(maxX, maxY);
				segment.p3 = new Point(minX, maxY);
				maxX = -1000;
				maxY = -1000;
				minX = 1000;
				minY = 1000;
				segments.add(segment);
				segment = new Segment();
			}
		}

		if (segments.size() == 0)
			return;

		/*
		 * for(Segment r: segments){ gc.setLineWidth(1); Point p0 = r.p0; Point p1 =
		 * r.p1; Point p2 = r.p2; Point p3 = r.p3; //draw borders in clock direction
		 * gc.strokeLine(p0.x,p0.y,p1.x,p1.y); gc.strokeLine(p1.x,p1.y,p2.x,p2.y);
		 * gc.strokeLine(p2.x,p2.y,p3.x,p3.y); gc.strokeLine(p3.x,p3.y,p0.x,p0.y); }
		 */
	}
}
