package com.kaciras.blog.infra.codec;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.nio.ByteBuffer;
import java.sql.*;

import static com.kaciras.blog.infra.codec.ImageReference.HASH_SIZE;

/**
 * 图片文件的引用，该类将对Hash作为文件名的图片路径做二进制编码，存储在数据库
 * 中时比字符串更高效，但也限制了文件名的长度。
 *
 * <h2>过度设计</h2>
 * 说实话，这么搞也没提高多少性能，反倒复杂了很多，如果不是练手完全没意义。
 */
public final class ImageReferenceTypeHandler extends BaseTypeHandler<ImageReference> {

	/**
	 * 编码 ImageReference 对象，将其转换为字节数组存储在数据库里，格式如下：
	 * +----------+-----------+
	 * | 类型序号 | 文件 HASH |
	 * +----------+-----------+
	 * <p>
	 * 该编码输出固定长度的字节数组，其长度为文件 Hash 字节长度 + 1；
	 */
	private byte[] encode(ImageReference image) {
		var buffer = ByteBuffer.allocate(HASH_SIZE + 1)
				.put((byte) image.getType().ordinal());

		var hash = CodecUtils.decodeHex(image.getName());
		if (hash.length != HASH_SIZE) {
			throw new IllegalArgumentException("图片名的Hash长度必须为32字节");
		}

		return buffer.put(hash).array();
	}

	private ImageReference decode(byte[] bytes) throws SQLDataException {
		if (bytes == null) {
			return null;
		}
		try {
			var type = ImageType.values()[bytes[0]];
			return new ImageReference(CodecUtils.encodeHex(bytes, 1, HASH_SIZE), type);
		} catch (IndexOutOfBoundsException | IllegalArgumentException e) {
			throw new SQLDataException("数据无法解析为图片引用");
		}
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, ImageReference parameter, JdbcType jdbcType) throws SQLException {
		ps.setBytes(i, encode(parameter));
	}

	@Override
	public ImageReference getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return decode(rs.getBytes(columnName));
	}

	@Override
	public ImageReference getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return decode(rs.getBytes(columnIndex));
	}

	@Override
	public ImageReference getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return decode(cs.getBytes(columnIndex));
	}
}
