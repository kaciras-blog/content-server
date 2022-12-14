package com.kaciras.blog.api;

import com.kaciras.blog.infra.autoconfigure.RedisUtilsAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@AutoConfigureDataRedis
@Import(RedisUtilsAutoConfiguration.class)

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface UseBlogRedis {}
