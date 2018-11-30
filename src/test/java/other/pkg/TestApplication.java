package other.pkg;

import net.kaciras.blog.infrastructure.autoconfig.KxCodecAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Import;

@EnableLoadTimeWeaving
@Import(KxCodecAutoConfiguration.class)
@SpringBootApplication
public class TestApplication {


}
