package other.pkg;

import net.kaciras.blog.infrastructure.autoconfig.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling
@EnableTransactionManagement(proxyTargetClass = true)
@EnableLoadTimeWeaving
@EnableSpringConfigured
@Import({
		KxGlobalCorsAutoConfiguration.class,
		KxWebUtilsAutoConfiguration.class,
		KxSpringSessionAutoConfiguration.class,
		KxCodecAutoConfiguration.class,
		KxPrincipalAutoConfiguration.class,
		DevelopmentAutoConfiguration.class
})
@SpringBootApplication(scanBasePackages = {"net.kaciras.blog", "other.pkg"})
public class TestApplicationWithScan {
}
