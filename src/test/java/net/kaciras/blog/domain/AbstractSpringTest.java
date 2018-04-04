package net.kaciras.blog.domain;

import net.kaciras.blog.facade.WebContextConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebAppConfiguration
@ContextHierarchy({
		@ContextConfiguration(name = "parent", classes = TestContextConfig.class),
		@ContextConfiguration(name = "web", classes = WebContextConfig.class),
})
abstract class AbstractSpringTest {}
