package waba.fx;

/*
Copyright (c) 1998, 1999, 2000 Wabasoft  All rights reserved.

This software is furnished under a license and may be used only in accordance
with the terms of that license. This software and documentation, and its
copyrights are owned by Wabasoft and are protected by copyright law.

THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.

WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
*/

import waba.applet.Applet;

/**
 * Image is a rectangular image.
 * <p>
 * You can draw into an image and copy an image to a surface using a Graphics
 * object.
 * @see Graphics
 */
public class Image implements ISurface
{
int width;
int height;
java.awt.Image _awtImage;

/**
 * Creates an image of the specified width and height. The image has
 * a color depth (number of bitplanes) and color map that matches the
 * default drawing surface.
 */
public Image(int width, int height)
	{
	this.width = width;
	this.height = height;
	_awtImage = Applet.currentApplet.createImage(width, height);
	}
/**
 * Loads and constructs an image from a file. The path given is the path to the
 * image file. The file must be in 2, 16 or 256 color uncompressed BMP bitmap
 * format. If the image cannot be loaded, the image constructed will have
 * a width and height of 0.
 */

public Image(String path)
	{
	width = 0;
	height = 0;
	loadImage(path);
	if (width == 0 || height == 0)
		_awtImage = Applet.currentApplet.createImage(1, 1);
	}
/**
 * Sets the image width and height to 0 and frees any systems resources
 * associated with the image.
 */

public void free()
	{
	width = 0;
	height = 0;
	_awtImage.flush();
	}
public java.awt.Image getAWTImage()
	{
	return _awtImage;
	}
/** Returns the height of the image. */
public int getHeight()
	{
	return height;
	}
/** Returns the width of the image. */
public int getWidth()
	{
	return width;
	}
// Intel architecture getUInt16
private static int inGetUInt16(byte bytes[], int off)
	{
	return ((bytes[off + 1]&0xFF) << 8) | (bytes[off]&0xFF);
	}
// Intel architecture getUInt32
private static int inGetUInt32(byte bytes[], int off)
	{
	return ((bytes[off + 3]&0xFF) << 24) | ((bytes[off + 2]&0xFF) << 16) |
		((bytes[off + 1]&0xFF) << 8) | (bytes[off]&0xFF);
	}
private void loadImage(String path)
	{
	java.io.InputStream stream = waba.sys.Vm.openInputStream(path);
	//	NOTE: we could use the following to read out of an applet's JAR file
	//	if we could get a pathObject which was in the root directory (the
	//	App for example). However, if we don't have one. If we loaded an
	//	image in the App constructor we wouldn't have the App object yet...
	//	if (!isApp)
	//		try
	//			{
	//			Object pathObject = Applet.currentApplet;
	//			stream = pathObject.getClass().getResourceAsStream(path);
	//			}
	//		catch (Exception e) {};

	if (stream == null)
		{
		System.out.println("ERROR: can't open image file " + path);
		return;
		}
	try
		{
		java.io.DataInputStream data = new java.io.DataInputStream(stream);
		readBMP(data, path);
		data.close();
		stream.close();
		}
	catch (Exception e)
		{
		System.out.println("ERROR: when loading Image. Trace appears below.");
		e.printStackTrace();
		}
	}
private void pixelsToRGB(int bitsPerPixel, int width, byte pixels[], int pixelOffset,
	int rgb[], int rgbOffset, int cmap[])
	{
	int mask, step;
	if (bitsPerPixel == 1)
		{
		mask = 0x1;
		step = 1;
		}
	else if (bitsPerPixel == 4)
		{
		mask = 0x0F;
		step = 4;
		}
	else // bitsPerPixel == 8
		{
		mask = 0xFF;
		step = 8;
		}
	int bit = 8 - step;
	int bytnum = pixelOffset;
	int byt = pixels[bytnum++];
	int x = 0;
	while (true)
		{
		int colorIndex = ((mask << bit) & byt) >> bit;
		rgb[rgbOffset++] = cmap[colorIndex] | (0xFF << 24);
		if (++x >= width)
			break;
		if (bit == 0)
			{
			bit = 8 - step;
			byt = pixels[bytnum++];
			}
		else
			bit -= step;
		}
	}
private void readBMP(java.io.DataInputStream data, String name)
	{
	// read header (54 bytes)
	// 0-1   magic chars 'BM'
	// 2-5   uint32 filesize (not reliable)
	// 6-7   uint16 0
	// 8-9   uint16 0
	// 10-13 uint32 bitmapOffset
	// 14-17 uint32 info size
	// 18-21 int32  width
	// 22-25 int32  height
	// 26-27 uint16 nplanes
	// 28-29 uint16 bits per pixel
	// 30-33 uint32 compression flag
	// 34-37 uint32 image size in bytes
	// 38-41 int32  biXPelsPerMeter (unused)
	// 32-45 int32  biYPelsPerMeter (unused)
	// 46-49 uint32 colors used (unused)
	// 50-53 uint32 important color count (unused)
	byte header[] = new byte[54];
	if (readBytes(data, header) != 54)
		{
		System.out.println("ERROR: can't read image header for " + name);
		return;
		}
	if (header[0] != 'B' || header[1] != 'M')
		{
		System.out.println("ERROR: " + name + " is not a BMP image");
		return;
		}
	int bitmapOffset = inGetUInt32(header, 10);

	int infoSize = inGetUInt32(header, 14);
	if (infoSize != 40)
		{
		System.out.println("ERROR: " + name + " is old-style BMP");
		return;
		}
	int width = inGetUInt32(header, 18);
	int height = inGetUInt32(header, 22);
	if (width < 0 || height < 0 || width > 65535 || height > 65535)
		{
		System.out.println("ERROR: " + name + " has invalid width/height");
		return;
		}
	int bpp = inGetUInt16(header, 28);
	if (bpp != 1 && bpp != 4 && bpp != 8)
		{
		System.out.println("ERROR: " + name + " is not a 2, 16 or 256 color image");
		return;
		}
	int compression = inGetUInt32(header, 30);
	if (compression != 0)
		{
		System.out.println("ERROR: " + name + " is a compressed image");
		return;
		}
	int numColors = 1 << bpp;
	int scanlen = (width * bpp + 7) / 8; // # bytes
	scanlen = ((scanlen + 3) / 4) * 4; // end on 32 bit boundry

	// read colormap
	//
	// 0-3 uint32 col[0]
	// 4-7 uint32 col[1]
	// ...
	int cmapSize = bitmapOffset - 54;
	byte cmapData[] = new byte[cmapSize];
	if (readBytes(data, cmapData) != cmapSize)
		{
		System.out.println("ERROR: can't read colormap of " + name);
		return;
		}
	int cmap[] = new int[numColors];
	int j = 0;
	for (int i = 0; i < numColors; i++)
		{
		byte blue = cmapData[j++];
		byte green = cmapData[j++];
		byte red = cmapData[j++];
		j++; // skip reserved
		cmap[i] = ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
		}

	// read pixels and store in RGB buffer
	int rgb[] = new int[width * height];
	byte pixels[] = new byte[scanlen];
	for (int y = height - 1; y >= 0; y--)
		{
		if (readBytes(data, pixels) != scanlen)
			{
			System.out.println("ERROR: scanline " + y + " bad in image " + name);
			return;
			}
		if (width == 0)
			continue;
		pixelsToRGB(bpp, width, pixels, 0, rgb, y * width, cmap);
		}

	// create the image from the RGB buffer
	this.width = width;
	this.height = height;
	java.awt.image.MemoryImageSource imageSource;
	imageSource = new java.awt.image.MemoryImageSource(width, height, rgb, 0, width);
	java.awt.Toolkit awtToolkit = java.awt.Toolkit.getDefaultToolkit();
	_awtImage = awtToolkit.createImage(imageSource);
	}
private int readBytes(java.io.DataInputStream data, byte b[])
	{
	int nread = 0;
	int len = b.length;
	while (true)
		{
		int n = 0;
		try { n = data.read(b, nread, len - nread); }
		catch (Exception e) {}
		if (n <= 0)
			return -1;
		nread += n;
		if (nread == len)
			return len;
		}
	}
/**
 * Sets one or more row(s) of pixels in an image. This method sets the values of
 * a number of pixel rows in an image and is commonly used when writing code
 * to load an image from a stream such as a file. The source pixels byte array
 * must be in 1, 4 or 8 bit per pixel format with a matching color map size
 * of 2, 16 or 256 colors.
 * <p>
 * Each color in the color map of the source pixels is identified by a single
 * integer value. The integer is composed of 8 bits (value [0..255]) of red,
 * green and blue using the following calculation:
 * <pre>
 * int color = (red << 16) | (green << 8) | blue;
 * </pre>
 * As an example, to load a 16 color image, we would pass bitsPerPixel
 * as 4 and would create a int array of 16 values for the color map.
 * Then we would set each of the values in the color map to the colors
 * used using the equation above. We could then either read data line
 * by line from the source stream, calling this method for each row of
 * pixels or could read a number of rows at once and then call this
 * method to set the pixels.
 * <p>
 * The former approach uses less memory, the latter approach is faster.
 *
 * @param bitsPerPixel bits per pixel of the source pixels (1, 4 or 8)
 * @param colorMap the color map of the source pixels (must be 2, 16 or 256 in length)
 * @param bytesPerRow number of bytes per row of pixels in the source pixels array
 * @param numRows the number of rows of pixels in the source pixels array
 * @param y y coordinate in the image to start setting pixels
 * @param pixels array containing the source pixels
 */

public void setPixels(int bitsPerPixel, int colorMap[], int bytesPerRow,
	int numRows, int y, byte pixels[])
	{
	if (bitsPerPixel != 1 && bitsPerPixel != 4 && bitsPerPixel != 8)
		return;
	// convert pixels to RGB values
	int rgb[] = new int[width * numRows];
	for (int r = 0; r < numRows; r++)
		pixelsToRGB(bitsPerPixel, width, pixels, r * bytesPerRow,
			rgb, r * width, colorMap);
	java.awt.image.MemoryImageSource imageSource;
	imageSource = new java.awt.image.MemoryImageSource(width, numRows, rgb, 0, width);
	java.awt.Toolkit awtToolkit = java.awt.Toolkit.getDefaultToolkit();
	java.awt.Image rowImage = awtToolkit.createImage(imageSource);
	java.awt.Graphics g = _awtImage.getGraphics();
	g.drawImage(rowImage, 0, y, null);
	g.dispose();
	rowImage.flush();
	}
}