package com.kaciras.blog.infra.codec;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.regex.Pattern;

/**
 * 表示一个图片文件的引用，该类是不可变的。
 * <p>
 * 该类也是连接前端、服务器和数据库的桥梁。向前端序列化时将被转换为图片文件的 URL，在数据库中能够以更紧凑的
 * 格式来存储{@link ImageReferenceTypeHandler ImageReferenceTypeHandler}。
 * <p>
 * 此类仅表示文件名，而不包含文件所在的目录、服务器等，这些信息由前端序列化时
 * 处理 {@link ImageReferenceJsonCodec.Serializer Serializer}，{@link ImageReferenceJsonCodec.Deserializer Deserializer}
 * <p>
 * 【更新】如果要保存原图的话，扩展名是必须要有的，不同格式的副本在前端处理。
 *
 * @author Kaciras
 */
@AllArgsConstructor
@Data
public final class ImageReference {

	/** 上传的图片文件名是 15 字节的 HASH */
	static final int HASH_SIZE = 15;

	/** 15 字节转 base64url 去除尾部填充后是 20 个字 */
	private static final Pattern checker = Pattern.compile("^[0-9a-zA-z_-]{20}$");

	private final String name;
	private final ImageType type;

	/**
	 * 返回原始的文件名，包含扩展名，但是不包含文件所在的目录。
	 *
	 * @return 文件名
	 */
	public String toString() {
		return name + '.' + type.name().toLowerCase();
	}

	/**
	 * 解析文件名，生成 ImageReference 实例。
	 *
	 * @param name 文件名
	 * @throws IllegalArgumentException 如果文件名中出现非法字符
	 */
	public static ImageReference parse(String name) {
		var dot = name.lastIndexOf('.');
		if (dot == -1) {
			throw new IllegalArgumentException("图片引用必须要有扩展名");
		}
		var ext = name.substring(dot + 1);
		var baseName = name.substring(0, dot);

		if (!checker.matcher(baseName).find()) {
			throw new IllegalArgumentException("无效的图片名：" + name);
		}

		try {
			var type = ImageType.valueOf(ext.toUpperCase());
			return new ImageReference(baseName, type);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("不支持的图片格式：" + ext);
		}
	}
}
