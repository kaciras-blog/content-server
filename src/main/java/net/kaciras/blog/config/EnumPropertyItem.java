package net.kaciras.blog.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kaciras.blog.EnumConfigItem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Getter
@Setter
final class EnumPropertyItem<T extends Enum> extends PropertyItem {

	private List<EnumEntry> options;

	EnumPropertyItem(String type, String desc) {
		super(type, desc);
		try {
			Class<T> aClass = (Class<T>) Class.forName(type);
			options = Arrays.stream(aClass.getEnumConstants())
					.map(EnumEntry::of)
					.collect(Collectors.toList());
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@EqualsAndHashCode(of = "key")
	@Getter
	@RequiredArgsConstructor
	private static final class EnumEntry {

		private final String key;
		private final String name;

		private static EnumEntry of(Enum constant) {
			try {
				String key = constant.name();
				EnumConfigItem annotation = constant.getDeclaringClass().getField(key)
						.getAnnotation(EnumConfigItem.class);
				String name = annotation == null ? key : annotation.value();
				return new EnumEntry(key, name);
			} catch (NoSuchFieldException e) {
				throw new Error("field changed.", e);
			}
		}
	}
}
