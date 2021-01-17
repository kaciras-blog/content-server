package com.kaciras.blog.infra.codec;

import java.net.Inet6Address;
import java.net.InetAddress;

public final class CodecUtils {

	private CodecUtils() {}

	private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'a', 'b', 'c', 'd', 'e', 'f'};

	/**
	 * encode a part of bytes to upper case hex string.
	 *
	 * @param bytes  bytes to be encode.
	 * @param offset start position.
	 * @param length length of part.
	 * @return hex string.
	 */
	public static String encodeHex(byte[] bytes, int offset, int length) {
		var out = new char[length << 1];
		for (int i = offset, j = 0; i < offset + length; i++) {
			out[j++] = DIGITS[(0xF0 & bytes[i]) >>> 4];
			out[j++] = DIGITS[0x0F & bytes[i]];
		}
		return new String(out);
	}

	/**
	 * encode bytes to upper case hex string.
	 *
	 * @param bytes bytes to be encode.
	 * @return hex string.
	 */
	public static String encodeHex(byte[] bytes) {
		return encodeHex(bytes, 0, bytes.length);
	}

	/**
	 * decode a hex string to bytes. The length of hex string must be even.
	 *
	 * @param text hex string.
	 * @return bytes.
	 */
	public static byte[] decodeHex(String text) {
		return decodeHex(new byte[text.length() >> 1], 0, text);
	}

	/**
	 * decode a hex string into a byte array. The length of hex string must be even.
	 *
	 * @param target a byte array to put decode result.
	 * @param offset start position.
	 * @param text   hex string.
	 * @return the <code>target</code>
	 */
	public static byte[] decodeHex(byte[] target, int offset, String text) {
		var data = text.toCharArray();

		if ((data.length & 1) != 0) {
			throw new IllegalArgumentException("The length of hex string must be even");
		}

		for (int i = offset, j = 0; j < data.length; i++, j++) {
			var f = toDigit(data[j], j) << 4;
			j++;
			f = f | toDigit(data[j], j);
			target[i] = (byte) (f & 0xFF);
		}
		return target;
	}

	private static int toDigit(char ch, int index) {
		var digit = Character.digit(ch, 16);
		if (digit == -1) {
			throw new IllegalArgumentException("char at index " + index + " is not a hex digit: " + ch);
		}
		return digit;
	}

	/**
	 * 检查一个字符是否是合法的Hex字符。
	 *
	 * @param ch 字符
	 * @return 如果是返回true，否则false
	 */
	public static boolean isHexDigit(char ch) {
		return ch >= '0' && ch <= '9' || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
	}

	/**
	 * Returns the index within a byte array of the first occurrence of
	 * the specified subarray.
	 *
	 * @param bytes the byte array.
	 * @param part  subarray.
	 * @param start the index from which to start the search.
	 * @return the index of the first occurrence of the subarray in the
	 * byte array, or {@code -1} if the character does not occur.
	 */
	public static int indexOfBytes(byte[] bytes, byte[] part, int start) {
		var len = bytes.length - part.length + 1;
		for (var i = start; i < len; ++i) {
			var found = true;
			for (var j = 0; j < part.length; ++j) {
				if (bytes[i + j] != part[j]) {
					found = false;
					break;
				}
			}
			if (found) return i;
		}
		return -1;
	}

	/**
	 * 把InetAddress转换为16字节的数组
	 *
	 * @param address 地址
	 * @return 字节数组
	 */
	public static byte[] toIPv6Bytes(InetAddress address) {
		if (address instanceof Inet6Address) {
			return address.getAddress();
		}
		return mappingToIPv6(address.getAddress());
	}

	/**
	 * 使用IPv4-mapped addresses,将IPv4的4字节地址转换成IPv6的16字节地址
	 *
	 * @param ipv4 表示IPv4地址的4个字节
	 * @return IPv4-mapped IPv6 Address bytes
	 * @see <a href="https://tools.ietf.org/html/rfc3493#section-3.7">IPv4-mapped addresses</a>
	 */
	private static byte[] mappingToIPv6(byte[] ipv4) {
		var ipv6 = new byte[16];
		ipv6[10] = ipv6[11] = (byte) 0xFF;
		System.arraycopy(ipv4, 0, ipv6, 12, 4);
		return ipv6;
	}
}
