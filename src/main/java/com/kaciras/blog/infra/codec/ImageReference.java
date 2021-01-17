package com.kaciras.blog.infra.codec;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 表示一个图片文件的引用，该类是不可变的。
 * <p>
 * 该类也是连接前端、服务器和数据库的桥梁。向前端序列化时将被转换为图片文件的URL，在数据库中能够以更紧凑的
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

	/** 上传的图片文件名是32字节的摘要 */
	static final int HASH_SIZE = 32;

	private final String name;
	private final ImageType type;

	/**
	 * 返回原始的文件名，包含扩展名，但是不包含文件所在的目录。
	 *
	 * @return 文件名
	 */
	public String toString() {
		return type == ImageType.Internal ? name : name + '.' + type.name().toLowerCase();
	}

	/**
	 * 解析文件名，生成 ImageReference 实例。
	 *
	 * @param name 文件名
	 * @return ImageReference
	 */
	public static ImageReference parse(String name) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("无效的图片文件名");
		}
		var reference = parseHex(name);
		if (reference != null) {
			return reference;
		}
		return new ImageReference(name, ImageType.Internal);
	}

	/**
	 * 尝试解析散列值文件名，如果文件名不符合散列值的格式则返回null
	 *
	 * @param name 文件名
	 * @return 图片引用，或者null
	 * @throws IllegalArgumentException 如果文件名中出现非法字符
	 */
	private static ImageReference parseHex(String name) {
		var dot = name.lastIndexOf('.');
		if (dot == -1) {
			throw new IllegalArgumentException("图片引用必须有扩展名");
		}
		var ext = name.substring(dot + 1);
		var baseName = name.substring(0, dot);

		// 要检查文件名里有没有路径分割符，必须一个个查看
		var hexChars = 0;
		for (var ch : baseName.toCharArray()) {
			if (CodecUtils.isHexDigit(ch)) {
				hexChars++;
			} else if (ch == '/' || ch == '\\') {
				throw new IllegalArgumentException("文件名中存在路径分隔符：" + name);
			}
		}

		if (hexChars != HASH_SIZE << 1) {
			return null;
		}

		try {
			return new ImageReference(baseName, ImageType.valueOf(ext.toUpperCase()));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("不支持的图片格式：" + ext);
		}
	}
}
