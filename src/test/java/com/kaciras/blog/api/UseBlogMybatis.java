package com.kaciras.blog.api;

import com.kaciras.blog.infra.autoconfigure.BlogMybatisAutoConfiguration;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@AutoConfigureMybatis
@Import(BlogMybatisAutoConfiguration.class)
@Transactional

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface UseBlogMybatis {}
