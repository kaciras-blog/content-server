package net.kaciras.blog.api.config;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;

@Getter
@Component
public class TestBindingBean {

	@ConfigBind("test.int")
	private int intValue = 33;

	@ConfigBind("test.enum")
	private ElementType enumValue = ElementType.FIELD;

	@ConfigBind("test.init")
	private double initValue;
}
