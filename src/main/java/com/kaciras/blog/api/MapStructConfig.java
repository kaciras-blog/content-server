package com.kaciras.blog.api;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct的全局配置，忽略未映射的字段（本来转换的两个对象字段就不会一致），注入方式设为Spring。
 * 为什么要用个接口+注解呢，我觉得是因为注解的值都必须是静态的吧。
 */
@MapperConfig(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface MapStructConfig {}
