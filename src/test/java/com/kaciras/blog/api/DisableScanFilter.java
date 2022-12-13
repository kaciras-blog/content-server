package com.kaciras.blog.api;

import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

class DisableScanFilter extends TypeExcludeFilter {

	@Override
	public boolean match(MetadataReader r, MetadataReaderFactory f) {
		return true;
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj != null) && (getClass() == obj.getClass());
	}
}
