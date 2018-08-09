package net.kaciras.blog.domain.config;

import io.reactivex.Observable;
import lombok.*;
import net.kaciras.blog.domain.EnumConfigItem;

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
