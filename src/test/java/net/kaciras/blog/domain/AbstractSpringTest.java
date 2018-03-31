package net.kaciras.blog.domain;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Rollback
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestContextConfig.class)
@ActiveProfiles("test")
abstract class AbstractSpringTest {
}
