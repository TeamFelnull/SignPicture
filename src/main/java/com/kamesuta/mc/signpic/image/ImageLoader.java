package com.kamesuta.mc.signpic.image;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.kamesuta.mc.signpic.Reference;
import com.kamesuta.mc.signpic.image.exception.InvaildImageException;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class ImageLoader implements Runnable {
	public static final ImageSize MAX_SIZE = new ImageSize(32, 32);

	protected Image image;
	protected ImageInputStream stream;

	public ImageLoader(final Image image, final ImageInputStream stream) throws IOException {
		this.image = image;
		this.stream = stream;
	}

	public ImageLoader(final Image image, final File file) throws IOException {
		this(image, ImageIO.createImageInputStream(file));
	}

	public ImageLoader(final Image image, final InputStream in) throws IOException {
		this(image, ImageIO.createImageInputStream(in));
	}

	public ImageLoader(final Image image, final IResourceManager manager, final ResourceLocation location) throws IOException {
		this(image, manager.getResource(location).getInputStream());
	}

	@Override
	public void run() {
		try {
			if (this.stream == null) throw new InvaildImageException();
			final Iterator<ImageReader> iter = ImageIO.getImageReaders(this.stream);
			if (!iter.hasNext()) throw new InvaildImageException();

			final ImageReader reader = iter.next();
			if (reader.getFormatName()=="gif") {
				loadGif(reader);
			} else {
				loadImage(reader);
			}
			this.image.state = ImageState.IOLOADED;
		} catch (final IOException e) {
			this.image.state = ImageState.FAILED;
			this.image.advmsg = I18n.format("signpic.advmsg.io", e);
			Reference.logger.error("IO Error", e);
		} catch (final Exception e) {
			this.image.state = ImageState.FAILED;
			this.image.advmsg = I18n.format("signpic.advmsg.unknown", e);
			Reference.logger.error("Unknown Error", e);
		}

	}

	protected static final String[] imageatt = new String[] {
			"imageLeftPosition",
			"imageTopPosition",
			"imageWidth",
			"imageHeight",
	};

	protected void loadGif(final ImageReader reader) throws IOException {
		reader.setInput(this.stream, false);
		final int noi = reader.getNumImages(true);

		try {
			BufferedImage canvas = null;
			final ArrayList<ImageTexture> textures = new ArrayList<ImageTexture>();
			for (int i = 0; i < noi; i++) {
				final BufferedImage image = reader.read(i);
				final IIOMetadata metadata = reader.getImageMetadata(i);

				final Node tree = metadata.getAsTree("javax_imageio_gif_image_1.0");
				final NodeList children = tree.getChildNodes();

				final ImageTexture texture = new ImageTexture();
				for (int j = 0; j < children.getLength(); j++) {
					final Node nodeItem = children.item(j);

					if(nodeItem.getNodeName().equals("ImageDescriptor")){
						final Map<String, Integer> imageAttr = new HashMap<String, Integer>();

						for (int k = 0; k < imageatt.length; k++) {
							final NamedNodeMap attr = nodeItem.getAttributes();
							final Node attnode = attr.getNamedItem(imageatt[k]);
							imageAttr.put(imageatt[k], Integer.valueOf(attnode.getNodeValue()));
						}
						if(i==0){
							final int wid = imageAttr.get("imageWidth");
							final int hei = imageAttr.get("imageHeight");
							canvas = new BufferedImage(wid, hei, BufferedImage.TYPE_INT_ARGB);
						}
						canvas.getGraphics().drawImage(image, imageAttr.get("imageLeftPosition"), imageAttr.get("imageTopPosition"), null);
					} else if (nodeItem.getNodeName().equals("GraphicControlExtension")) {
						final NamedNodeMap attr = nodeItem.getAttributes();
						final Node nodedelay = attr.getNamedItem("delayTime");
						texture.setDelay((float)Integer.valueOf(nodedelay.getNodeValue())/100f);
					}
				}
				final ImageSize newsize = ImageSize.createSize(ImageSizes.LIMIT, canvas.getWidth(), canvas.getHeight(), MAX_SIZE);
				texture.setImage(createResizedImage(canvas, newsize));
				textures.add(texture);
			}
			this.image.texture = new ImageTextures(textures);
		} finally {
			reader.dispose();
			this.stream.close();
		}
	}

	protected void loadImage(final ImageReader reader) throws IOException {
		final ImageReadParam param = reader.getDefaultReadParam();
		reader.setInput(this.stream, true, true);
		BufferedImage canvas;
		try {
			canvas = reader.read(0, param);
		} finally {
			reader.dispose();
			this.stream.close();
		}
		final ImageSize newsize = ImageSize.createSize(ImageSizes.LIMIT, canvas.getWidth(), canvas.getHeight(), MAX_SIZE);
		this.image.texture = new ImageTextures(Lists.newArrayList(new ImageTexture(createResizedImage(canvas, newsize))));
	}

	protected BufferedImage createResizedImage(final BufferedImage image, final ImageSize newsize) {
		final int wid = (int)newsize.width;
		final int hei = (int)newsize.height;
		final BufferedImage thumb = new BufferedImage(wid, hei, image.getType());
		final Graphics g = thumb.getGraphics();
		g.drawImage(image.getScaledInstance(wid, hei, java.awt.Image.SCALE_AREA_AVERAGING), 0, 0, wid, hei, null);
		g.dispose();
		return thumb;
	}
}
