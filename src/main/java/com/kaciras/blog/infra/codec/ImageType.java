package com.kaciras.blog.infra.codec;

/**
 * 表示图片的类型。
 *
 * 序列化使用了字段次序，所以请勿改动顺序，新增的往后加。
 *
 * 为何不给项目添字段而是使用次序作为数据库列的值？
 * 首先，如果不用次序，则需要自己维护由int到ImageType的映射，而用次序则可以直接 ImageType.values()[xxx]。
 * 其次，该枚举所序列化的值除了区分不同类型外没有其他意义，用顺序是可以的
 * 第三，图片类型不太可能被移除支持，所以不会因删除而导致次序改变
 */
public enum ImageType {

	/** 内置的图片，文件名不是Hash值 */
	Internal,

	JPG, WEBP, PNG, GIF, SVG, BMP,
}
