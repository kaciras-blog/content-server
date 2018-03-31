package net.kaciras.blog.domain;


import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;

import java.util.Properties;

public abstract class AbstractFilterPlugin implements Interceptor {

	private ThreadLocal<Interceptor> binding = new ThreadLocal<>();

	@Override
	public Object intercept(Invocation invocation) {
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		BoundSql sql = statementHandler.getBoundSql();
//		return invocation.proceed();
		throw new AssertionError("这个方法不会被调用");
	}

	@Override
	public Object plugin(Object target) {
		Interceptor interceptor = binding.get();
		if (interceptor == null) {
			return target;
		}
		binding.remove();
		return Plugin.wrap(target, interceptor);
	}

	@Override
	public void setProperties(Properties properties) {
	}

	public void startFilte(String statement) {

	}

	protected abstract boolean acceptField(String field);
}
