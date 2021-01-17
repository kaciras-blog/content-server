package com.kaciras.blog.infra.codec;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class ExtendsCodecModule extends SimpleModule {

	@FunctionalInterface
	public interface SerializerFunction<T> {
		void apply(T t, JsonGenerator g) throws IOException;
	}

	@FunctionalInterface
	public interface DeserializerFunction<R> {
		R apply(JsonParser p) throws IOException;
	}

	public <T> void addSerializer(Class<T> clazz, SerializerFunction<T> serializeFunction) {
		var serializer = new JsonSerializer<T>() {
			@Override
			public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
				serializeFunction.apply(value, gen);
			}
		};
		addSerializer(clazz, serializer);
	}

	public <T> void addDeserializer(Class<T> clazz, DeserializerFunction<T> deserializeFunction) {
		var deserializer = new JsonDeserializer<T>() {
			@Override
			public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
				return deserializeFunction.apply(parser);
			}
		};
		addDeserializer(clazz, deserializer);
	}

	// ================ 上面的代码抄自 https://gist.github.com/jeremychone/a7e06b8baffef88a8816 ================

	@Override
	public void setupModule(SetupContext context) {
		addSerializer(ImageReference.class, new ImageReferenceJsonCodec.Serializer());
		addDeserializer(ImageReference.class, new ImageReferenceJsonCodec.Deserializer());

		addSerializer(LocalDateTime.class, (value, gen) ->
				gen.writeNumber(value.toInstant(ZoneOffset.UTC).toEpochMilli()));
		addDeserializer(LocalDateTime.class, parser ->
				Instant.ofEpochMilli(parser.getLongValue()).atOffset(ZoneOffset.UTC).toLocalDateTime());

		super.setupModule(context);
	}
}
