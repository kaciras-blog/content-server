package com.kaciras.blog.api;

import com.kaciras.blog.infra.autoconfigure.BlogJsonAutoConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.*;

@ActiveProfiles("test")
@TestExecutionListeners(value = AutoFlushRedis.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@AutoConfigureJson
@Import({BlogJsonAutoConfiguration.class, SnapshotAssertion.class})

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited

@BootstrapWith(SpringBootTestContextBootstrapper.class)
@ExtendWith({SpringExtension.class, SnapshotAssertion.ContextHolder.class})
@ImportAutoConfiguration
@OverrideAutoConfiguration(enabled = false)
@TypeExcludeFilters(DisableScanFilter.class)
public @interface MinimumSpringTest {}
