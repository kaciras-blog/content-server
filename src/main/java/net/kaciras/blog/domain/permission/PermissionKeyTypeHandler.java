package net.kaciras.blog.domain.permission;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class PermissionKeyTypeHandler extends BaseTypeHandler<PermissionKey> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, PermissionKey parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, parameter.getModule() + "#" + parameter.getName());
	}

	@Override
	public PermissionKey getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return convert(rs.getString(columnName));
	}

	@Override
	public PermissionKey getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return convert(rs.getString(columnIndex));
	}

	@Override
	public PermissionKey getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return convert(cs.getString(columnIndex));
	}

	private PermissionKey convert(String string) throws SQLException {
		if (string == null) {
			return null;
		}
		String[] split = string.split("#", 2);
		if (split.length != 2) {
			throw new SQLException("Can not convert data to PermissionKey");
		}
		return new PermissionKey(split[0], split[1]);
	}
}
