package com.kaciras.blog.infra.codec;

import org.apache.ibatis.executor.result.ResultMapException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class InetAddressTypeHandlerTest extends AbstractTypeHandlerTest {

	private static final TypeHandler<InetAddress> HANDLER = new InetAddressTypeHandler();

	private static final byte[] MAPPING_IPV4_DATA = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, 66, 77, 88, 99};

	private final InetAddress IPV4_ADDRESS = InetAddress.getByName("66.77.88.99");

	/** 上面那句 InetAddress.getByName 抛异常，不能把字段设为静态的，而且还得在构造方法上声明异常 */
	InetAddressTypeHandlerTest() throws Exception {}

	@Override
	@Test
	void setParameter() throws Exception {
		HANDLER.setParameter(preparedStatement, 1, InetAddress.getByName("66.77.88.99"), JdbcType.BINARY);
		Mockito.verify(preparedStatement).setBytes(1, MAPPING_IPV4_DATA);
	}

	@Override
	@Test
	void getResultFromResultSetByName() throws Exception {
		when(resultSet.getBytes("column")).thenReturn(MAPPING_IPV4_DATA);
		assertThat(HANDLER.getResult(resultSet, "column")).isEqualTo(IPV4_ADDRESS);
	}

	@Override
	@Test
	void getResultFromResultSetByPosition() throws Exception {
		when(resultSet.getBytes(1)).thenReturn(MAPPING_IPV4_DATA);
		assertThat(HANDLER.getResult(resultSet, 1)).isEqualTo(IPV4_ADDRESS);
	}

	@Override
	@Test
	void getResultFromCallableStatement() throws Exception {
		when(callableStatement.getBytes(1)).thenReturn(MAPPING_IPV4_DATA);
		assertThat(HANDLER.getResult(callableStatement, 1)).isEqualTo(IPV4_ADDRESS);
	}

	@Test
	void getIPv6() throws Exception {
		var youtube = InetAddress.getByName("2001:4860:4001:402::15");
		var resultSet = mock(ResultSet.class);
		when(resultSet.getBytes(1)).thenReturn(youtube.getAddress());

		var addr = HANDLER.getResult(resultSet, 1);
		assertThat(addr).isEqualTo(youtube);
	}

	@Test
	void invalidValue() throws SQLException {
		when(resultSet.getBytes(1)).thenReturn("invalid".getBytes());

		assertThatThrownBy(() -> HANDLER.getResult(resultSet, 1))
				.isInstanceOf(ResultMapException.class)
				.hasCauseInstanceOf(SQLException.class);
	}
}
