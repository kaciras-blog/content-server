package com.kaciras.blog.infra.codec;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;

/**
 * 将InetAddress转换成长度为16的字节数组(IPv6)存储在数据库中。
 * IPv4 将使用 IPv4 Mapped IPv6 Address 格式转码。
 *
 * 【设计原理】
 * https://blog.kaciras.com/article/7/how-to-store-ip-address-in-database
 */
public final class InetAddressTypeHandler extends BaseTypeHandler<InetAddress> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, InetAddress address, JdbcType jdbcType) throws SQLException {
		ps.setBytes(i, CodecUtils.toIPv6Bytes(address));
	}

	@Override
	public InetAddress getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return decode(rs.getBytes(columnName));
	}

	@Override
	public InetAddress getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return decode(rs.getBytes(columnIndex));
	}

	@Override
	public InetAddress getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return decode(cs.getBytes(columnIndex));
	}

	/**
	 * 把字节数组转换为InetAddress，包装了下异常。
	 *
	 * @param bytes 字节数组
	 * @return InetAddress
	 * @throws SQLDataException 读取的数据不是IP地址
	 */
	private static InetAddress decode(byte[] bytes) throws SQLDataException {
		try {
			// InetAddress.getByAddress() 能自动识别 IPv4-Mapped addresses
			return InetAddress.getByAddress(bytes);
		} catch (UnknownHostException e) {
			throw new SQLDataException("读取的数据不是IP地址", e);
		}
	}
}
