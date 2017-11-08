package org.panda.utility;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author Ozgun Babur
 */
public class GUIUtil
{
	public static void saveImageToClipboard(Component comp)
	{
		Image image = new BufferedImage(
			comp.getSize().width,
			comp.getSize().height,
			BufferedImage.TYPE_4BYTE_ABGR);
		comp.paint(image.getGraphics());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			ImageIO.write((RenderedImage) image, "png", bos);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		ClipImage ci = new ClipImage(bos.toByteArray());
		clipboard.setContents(ci, null);
	}

	public static void writeImage(Component comp, String filename)
		throws IOException
	{
		if (!filename.endsWith(".png")) filename += ".png";

		Image image = new BufferedImage(
			comp.getSize().width,
			comp.getSize().height,
			BufferedImage.TYPE_4BYTE_ABGR);
		comp.paint(image.getGraphics());

		ImageIO.write((RenderedImage) image, "png", new File(filename));
	}


	static class ClipImage implements Transferable, ClipboardOwner
	{
		private byte[] image;

		public ClipImage(byte[] im)
		{
			image = im;
		}

		public DataFlavor[] getTransferDataFlavors()
		{
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			return DataFlavor.imageFlavor.equals(flavor);
		}

		public Object getTransferData(DataFlavor flavor) throws
			UnsupportedFlavorException
		{
			if (!isDataFlavorSupported(flavor))
				throw new UnsupportedFlavorException(flavor);
			return Toolkit.getDefaultToolkit().createImage(image);
		}

		public void lostOwnership(java.awt.datatransfer.Clipboard clip,
			java.awt.datatransfer.Transferable tr)
		{
		}
	}
}
