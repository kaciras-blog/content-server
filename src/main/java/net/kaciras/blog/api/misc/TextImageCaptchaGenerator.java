package net.kaciras.blog.api.misc;

import lombok.Cleanup;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * 验证码生成工具，使用AWT绘制字符验证码，并能够添加噪点，干扰线，扭曲，随机颜色等干扰。
 */
@Component
public final class TextImageCaptchaGenerator {

	private static final Random RANDOM = new Random();

	// 字符集
	private static final char[] CAPTCHA_TEXT =
			"0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

	private static final int CAPTCHA_WIDTH = 150; // 图片宽度
	private static final int CAPTCHA_HEIGHT = 40; // 图片高度

	private final Font font;

	// 使用独立的字体文件，统一不同系统上的字体
	public TextImageCaptchaGenerator() throws IOException, FontFormatException {
		var fontFile = TextImageCaptchaGenerator.class.getClassLoader().getResource("CENTURY.TTF");
		if (fontFile == null) {
			throw new Error("找不到验证码字体文件：CENTURY.TTF");
		}
		@Cleanup var stream = fontFile.openStream();
		font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(Font.PLAIN, CAPTCHA_HEIGHT - 4);
	}

	/**
	 * 将生成的图片写入到输出流中,并返回验证码字符串
	 *
	 * @param output 输出流
	 * @return 验证码字符串
	 * @throws IOException 如果发生IO错误
	 */
	public String generate(OutputStream output) throws IOException {
		var text = randomCaptchaText(5);
		ImageIO.write(createCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, text), "jpg", output);
		return text.toLowerCase();
	}

	private String randomCaptchaText(int length) {
		var text = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			text.append(CAPTCHA_TEXT[RANDOM.nextInt(CAPTCHA_TEXT.length)]);
		}
		return text.toString();
	}

	private BufferedImage createCaptcha(int w, int h, String text) {
		var verifySize = text.length();
		var image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		var g2 = image.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.GRAY); // 边框色
		g2.fillRect(0, 0, w, h);

		var c = randomColor(200, 250);
		g2.setColor(c); // 设置背景色
		g2.fillRect(0, 2, w, h - 4);

		shearX(g2, w, h, c); // 扭曲
		shearY(g2, w, h, c);

		// 干扰线
		g2.setColor(randomColor(160, 200)); // 线条的颜色
		for (int i = 0; i < 20; i++) {
			var x = RANDOM.nextInt(w - 1);
			var y = RANDOM.nextInt(h - 1);
			var xl = RANDOM.nextInt(6) + 1;
			var yl = RANDOM.nextInt(12) + 1;
			g2.drawLine(x, y, x + xl + 40, y + yl + 20);
		}

		// 噪点
		var yawpRate = 0.08f; // 噪声率
		var area = (int) (yawpRate * w * h);
		for (var i = 0; i < area; i++) {
			image.setRGB(RANDOM.nextInt(w), RANDOM.nextInt(h), randomColorValue(0, 0xFF));
		}

		g2.setFont(font);
		var chars = text.toCharArray();

		for (var i = 0; i < verifySize; i++) {
			var affine = new AffineTransform();
			var angle = Math.PI / 4 * RANDOM.nextDouble() * (RANDOM.nextBoolean() ? 1 : -1);

			affine.setToRotation(angle, ((double) w / verifySize) * i + font.getSize() / 2D, h / 2D);
			g2.setColor(randomColor(50, 140));
			g2.setTransform(affine);
			g2.drawChars(chars, i, 1, ((w - 10) / verifySize) * i + 5, h / 2 + font.getSize() / 2 - 10);
		}

		g2.dispose();
		return image;
	}

	private Color randomColor(int lo, int hi) {
		return new Color(randomColorValue(lo, hi));
	}

	/**
	 * 随机生成一个颜色的整数表示，其RGB每通道都是在[lo, hi]区间的随机值。
	 *
	 * @param lo 单通道下限，不能为负，未检查
	 * @param hi 单通道上限，不能大于255，未检查
	 * @return 颜色值ARGB
	 */
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	private int randomColorValue(int lo, int hi) {
		return IntStream.range(0, 3)
				.map(i -> lo + RANDOM.nextInt(hi - lo))
				.reduce((rgb, channel) -> rgb << 8 | channel)
				.getAsInt();
	}

	private void shearX(Graphics g, int w1, int h1, Color color) {
		var period = RANDOM.nextInt(2);
		var frames = 1;
		var phase = RANDOM.nextInt(2);

		for (var i = 0; i < h1; i++) {
			var d = (period >> 1) * Math.sin(i / (double) period + Math.PI * 2 * phase / frames);
			g.copyArea(0, i, w1, 1, (int) d, 0);
			g.setColor(color);
			g.drawLine((int) d, i, 0, i);
			g.drawLine((int) d + w1, i, w1, i);
		}
	}

	private void shearY(Graphics g, int w1, int h1, Color color) {
		var period = RANDOM.nextInt(40) + 10; // 50;
		var frames = 20;
		var phase = 7;
		for (var i = 0; i < w1; i++) {
			var d = (period >> 1) * Math.sin(i / (double) period + Math.PI * 2 * phase / frames);
			g.copyArea(i, 0, 1, h1, 0, (int) d);
			g.setColor(color);
			g.drawLine(i, (int) d, i, 0);
			g.drawLine(i, (int) d + h1, i, h1);
		}
	}
}
