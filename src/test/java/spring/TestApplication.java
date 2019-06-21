package spring;

import net.kaciras.blog.infrastructure.autoconfigure.KxCodecAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(KxCodecAutoConfiguration.class)
@SpringBootApplication
public class TestApplication {}
