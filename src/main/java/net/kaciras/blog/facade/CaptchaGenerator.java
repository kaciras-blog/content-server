package net.kaciras.blog.facade;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import static net.kaciras.blog.domain.Utils.RANDOM;

/**
 * 验证码生成工具，使用AWT绘制字符验证码。
 * 干扰包括噪点，干扰线，扭曲，随机颜色
 */
@Component
public final class CaptchaGenerator {

	//字符集
	private static final String CAPTCHA_TEXT = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private static final int CAPTCHA_WIDTH = 150; //图片宽度
	private static final int CAPTCHA_HEIGHT = 40; //图片高度

	/**
	 * 将生成的图片写入到输出流中,并返回验证码字符串
	 *
	 * @param output 输出流
	 * @return 验证码字符串
	 * @throws IOException 如果发生IO错误
	 */
	public String writeCaptcha(OutputStream output) throws IOException {
		String text = randomCaptchaText(5);
		ImageIO.write(createCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, text), "jpg", output);
		return text;
	}

	private static String randomCaptchaText(int captchaSize) {
		StringBuilder text = new StringBuilder(captchaSize);
		for (int i = 0; i < captchaSize; i++) {
			int position = RANDOM.nextInt(CAPTCHA_TEXT.length() - 1);
			text.append(CAPTCHA_TEXT.charAt(position));
		}
		return text.toString();
	}

	private static BufferedImage createCaptcha(int w, int h, String text) {
		int verifySize = text.length();
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

		Graphics2D g2 = image.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(Color.GRAY); //边框色
		g2.fillRect(0, 0, w, h);

		Color c = getRandColor(200, 250);
		g2.setColor(c); //设置背景色
		g2.fillRect(0, 2, w, h - 4);

		shearX(g2, w, h, c); //扭曲
		shearY(g2, w, h, c);

		//干扰线
		g2.setColor(getRandColor(160, 200)); //线条的颜色
		for (int i = 0; i < 20; i++) {
			int x = RANDOM.nextInt(w - 1);
			int y = RANDOM.nextInt(h - 1);
			int xl = RANDOM.nextInt(6) + 1;
			int yl = RANDOM.nextInt(12) + 1;
			g2.drawLine(x, y, x + xl + 40, y + yl + 20);
		}

		//噪点
		float yawpRate = 0.06f; //噪声率
		int area = (int) (yawpRate * w * h);
		for (int i = 0; i < area; i++) {
			int x = RANDOM.nextInt(w);
			int y = RANDOM.nextInt(h);
			int rgb = getRandomIntColor();
			image.setRGB(x, y, rgb);
		}

		int fontSize = h - 4;
		Font font = new Font(null, Font.ITALIC, fontSize);
		g2.setFont(font);
		char[] chars = text.toCharArray();
		for (int i = 0; i < verifySize; i++) {
			AffineTransform affine = new AffineTransform();
			affine.setToRotation(Math.PI / 4 * RANDOM.nextDouble() * (RANDOM.nextBoolean() ? 1 : -1), (w / verifySize) * i + fontSize / 2, h / 2);
			g2.setColor(getRandColor(50, 140));
			g2.setTransform(affine);
			g2.drawChars(chars, i, 1, ((w - 10) / verifySize) * i + 5, h / 2 + fontSize / 2 - 10);
		}

		g2.dispose();
		return image;
	}

	private static Color getRandColor(int fc, int bc) {
		fc &= 0xFF;
		bc &= 0xFF;
		int r = fc + RANDOM.nextInt(bc - fc);
		int g = fc + RANDOM.nextInt(bc - fc);
		int b = fc + RANDOM.nextInt(bc - fc);
		return new Color(r, g, b);
	}

	private static int getRandomIntColor() {
		int[] rgb = getRandomRgb();
		int color = 0;
		for (int c : rgb) {
			color = color << 8;
			color = color | c;
		}
		return color;
	}

	private static int[] getRandomRgb() {
		int[] rgb = new int[3];
		for (int i = 0; i < 3; i++) {
			rgb[i] = RANDOM.nextInt(255);
		}
		return rgb;
	}

	private static void shearX(Graphics g, int w1, int h1, Color color) {
		int period = RANDOM.nextInt(2);
		int frames = 1;
		int phase = RANDOM.nextInt(2);

		for (int i = 0; i < h1; i++) {
			double d = (period >> 1) * Math.sin(i / (double) period
					+ 6.2831853071795862D * phase / frames);
			g.copyArea(0, i, w1, 1, (int) d, 0);
			g.setColor(color);
			g.drawLine((int) d, i, 0, i);
			g.drawLine((int) d + w1, i, w1, i);
		}
	}

	private static void shearY(Graphics g, int w1, int h1, Color color) {
		int period = RANDOM.nextInt(40) + 10; // 50;
		int frames = 20;
		int phase = 7;
		for (int i = 0; i < w1; i++) {
			double d = (period >> 1) * Math.sin(i / (double) period
					+ 6.2831853071795862D * phase / frames);
			g.copyArea(i, 0, 1, h1, 0, (int) d);
			g.setColor(color);
			g.drawLine(i, (int) d, i, 0);
			g.drawLine(i, (int) d + h1, i, h1);
		}
	}

}
