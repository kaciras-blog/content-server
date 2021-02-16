package com.kaciras.blog.infra;

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.util.regex.Pattern;

/**
 * 扩展 Mybatis 的解析器，让注解中的 SQL 也支持集合参数。
 * 新增的规则会将 (#{xxx}) 这样的字符串将其转换为 foreach 块。
 *
 * <a href="https://stackoverflow.com/a/29076097">详情见该回答</a>
 */
public final class MybatisInlineScriptDriver extends XMLLanguageDriver {

	private final Pattern regex = Pattern.compile("\\(#\\{(\\w+)}\\)");

	public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
		var matcher = regex.matcher(script);
		if (matcher.find()) {
			script = matcher.replaceAll("(<foreach collection=\"$1\" item=\"__item\" separator=\",\" >#{__item}</foreach>)");
			script = "<script>" + script + "</script>";
		}
		return super.createSqlSource(configuration, script, parameterType);
	}
}
