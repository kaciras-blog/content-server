package com.kaciras.blog.infra.codec;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;

/**
 * 将 ImageReference 和Json(com.fasterxml.jackson)互相转换的工具。
 * 转换时会加上图片所在服务器的URL前缀。
 */
final class ImageReferenceJsonCodec {

	private static final String STATIC_IMAGES = "/static/img/";
	private static final String IMAGE_SERVER = "/image/";

	static final class Serializer extends JsonSerializer<ImageReference> {

		@Override
		public void serialize(ImageReference value,
							  JsonGenerator gen,
							  SerializerProvider serializers) throws IOException {
			var directory = value.getType() == ImageType.Internal ? STATIC_IMAGES : IMAGE_SERVER;
			gen.writeString(directory + value.toString());
		}
	}

	static final class Deserializer extends JsonDeserializer<ImageReference> {

		@Override
		public ImageReference deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
			var text = p.getText();
			try {
				if (text.startsWith(IMAGE_SERVER)) {
					return ImageReference.parse(text.substring(IMAGE_SERVER.length()));
				}
				if (text.startsWith(STATIC_IMAGES)) {
					return ImageReference.parse(text.substring(STATIC_IMAGES.length()));
				}
				throw new InvalidFormatException(p, "图片路径错误", text, ImageReference.class);
			} catch (IllegalArgumentException e) {
				throw new InvalidFormatException(p, "无效的图片引用", text, ImageReference.class);
			}
		}
	}
}
