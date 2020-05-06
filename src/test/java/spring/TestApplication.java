package spring;

import com.kaciras.blog.infra.autoconfigure.KxCodecAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(KxCodecAutoConfiguration.class)
@SpringBootApplication
public class TestApplication {}
