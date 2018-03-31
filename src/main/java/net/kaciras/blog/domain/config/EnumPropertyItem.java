package net.kaciras.blog.domain.config;

import io.reactivex.Observable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.EnumConfigItem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@EqualsAndHashCode(callSuper = true)
@Data
final class EnumPropertyItem<T extends Enum> extends PropertyItem {

	private List<EnumEntry> options;

	EnumPropertyItem(String clazz, String name, String desc) {
		super("enum", name, desc);
		try {
			Class<T> aClass = (Class<T>) Class.forName(clazz);
			options = Observable.fromArray(aClass.getEnumConstants())
					.map(EnumEntry::of)
					.toList()
					.blockingGet();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	@EqualsAndHashCode(of = "key")
	@Getter
	@RequiredArgsConstructor
	static final class EnumEntry {

		private final String key;
		private final String name;

		private static EnumEntry of(Enum constant) throws NoSuchFieldException {
			String key = constant.name();
			EnumConfigItem annotation = constant.getDeclaringClass().getField(key)
					.getAnnotation(EnumConfigItem.class);
			String name = annotation == null ? key : annotation.value();
			return new EnumEntry(key, name);
		}
	}
}
