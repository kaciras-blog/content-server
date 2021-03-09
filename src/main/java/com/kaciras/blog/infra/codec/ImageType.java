package com.kaciras.blog.infra.codec;

/**
 * 表示图片类型。序列化使用了字段次序，所以请勿改动顺序，新增的往后加。
 *
 * <h2>为何使用次序</h2>
 * <ul>
 *     <li>如果不用次序，则需要自己维护由int到ImageType的映射，麻烦。</li>
 *     <li>该枚举所序列化的值除了区分不同类型外没有其他意义，用顺序是可以的。</li>
 *     <li>图片类型不太可能被移除支持，所以不会因删除而导致次序改变。</li>
 * </ul>
 */
public enum ImageType {

	/** 内置的图片，文件名不是 Hash 值 */
	INTERNAL,

	JPG, WEBP, PNG, GIF, SVG, BMP,
}
