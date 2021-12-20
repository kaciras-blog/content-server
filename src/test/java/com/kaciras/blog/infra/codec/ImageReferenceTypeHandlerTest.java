package com.kaciras.blog.infra.codec;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.result.ResultMapException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

final class ImageReferenceTypeHandlerTest extends AbstractTypeHandlerTest {

	private static final String HASH_NAME = "ZBLARqvF4-_cDUmPkjsH";
	private static final byte[] HASH_DATA;

	private static final ImageReference IMAGE = new ImageReference(HASH_NAME, ImageType.WEBP);

	private final TypeHandler<ImageReference> handler = new ImageReferenceTypeHandler();

	static {
		HASH_DATA = ByteBuffer.allocate(16)
				.put((byte) 1)
				.put(Base64.getUrlDecoder().decode(HASH_NAME)).array();
	}

	@Override
	@Test
	void setParameter() throws Exception {
		handler.setParameter(preparedStatement, 1, IMAGE, JdbcType.BINARY);
		Mockito.verify(preparedStatement).setBytes(1, HASH_DATA);
	}

	@Test
	void encodeNonInternal() throws Exception {
		var image = new ImageReference(HASH_NAME, ImageType.WEBP);
		handler.setParameter(preparedStatement, 1, image, JdbcType.BINARY);

		Mockito.verify(preparedStatement).setBytes(1, HASH_DATA);
	}

	@Test
	void encodeInvalidHashName() {
		var image = new ImageReference(HASH_NAME + "12", ImageType.WEBP);

		assertThatThrownBy(() -> handler.setParameter(preparedStatement, 1, image, JdbcType.BINARY))
				.isInstanceOf(PersistenceException.class);
	}

	@Override
	@Test
	void getResultFromResultSetByName() throws Exception {
		when(resultSet.getBytes("column")).thenReturn(HASH_DATA);
		assertThat(handler.getResult(resultSet, "column")).isEqualTo(IMAGE);
	}

	@Override
	@Test
	void getResultFromResultSetByPosition() throws Exception {
		when(resultSet.getBytes(1)).thenReturn(HASH_DATA);
		assertThat(handler.getResult(resultSet, 1)).isEqualTo(IMAGE);
	}

	@Override
	@Test
	void getResultFromCallableStatement() throws Exception {
		when(callableStatement.getBytes(1)).thenReturn(HASH_DATA);
		assertThat(handler.getResult(callableStatement, 1)).isEqualTo(IMAGE);
	}

	@Test
	void getResultNull() throws Exception {
		when(resultSet.getBytes(1)).thenReturn(null);
		assertThat(handler.getResult(resultSet, 1)).isNull();
	}

	@Test
	void hexName() throws SQLException {
		when(resultSet.getBytes(1)).thenReturn(HASH_DATA);

		assertThat(handler.getResult(resultSet, 1))
				.isEqualTo(new ImageReference(HASH_NAME, ImageType.WEBP));
	}

	@Test
	void decodeInvalidValue() throws SQLException {
		when(resultSet.getBytes(1)).thenReturn("invalid".getBytes());

		assertThatThrownBy(() -> handler.getResult(resultSet, 1))
				.isInstanceOf(ResultMapException.class)
				.hasCauseInstanceOf(SQLException.class);
	}
}
