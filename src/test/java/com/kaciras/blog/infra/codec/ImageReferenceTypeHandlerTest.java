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

	private static final TypeHandler<ImageReference> HANDLER = new ImageReferenceTypeHandler();

	private static final String HASH_NAME = "ZBLARqvF4-_cDUmPkjsH";
	private static final byte[] HASH_DATA;

	static {
		HASH_DATA = ByteBuffer.allocate(16)
				.put((byte) 1)
				.put(Base64.getUrlDecoder().decode(HASH_NAME)).array();
	}

	@Override
	@Test
	void setParameter() throws Exception {
		var image = new ImageReference(HASH_NAME, ImageType.WEBP);
		HANDLER.setParameter(preparedStatement, 1, image, JdbcType.BINARY);
		Mockito.verify(preparedStatement).setBytes(1, HASH_DATA);
	}

//	@Test
//	void encodeInvalidName() {
//		var image = new ImageReference(, ImageType.PNG);
//
//		assertThatThrownBy(() -> HANDLER.setParameter(preparedStatement, 1, image, JdbcType.BINARY))
//				.isInstanceOf(PersistenceException.class);
//	}

	@Test
	void encodeNonInternal() throws Exception {
		var image = new ImageReference(HASH_NAME, ImageType.WEBP);
		HANDLER.setParameter(preparedStatement, 1, image, JdbcType.BINARY);

		Mockito.verify(preparedStatement).setBytes(1, HASH_DATA);
	}

	@Test
	void encodeInvalidHashName() {
		var image = new ImageReference(HASH_NAME + "12", ImageType.WEBP);

		assertThatThrownBy(() -> HANDLER.setParameter(preparedStatement, 1, image, JdbcType.BINARY))
				.isInstanceOf(PersistenceException.class);
	}

	@Override
	@Test
	void getResultFromResultSetByName() throws Exception {
//		when(resultSet.getBytes("column")).thenReturn(INTERNAL_DATA);
//		assertThat(HANDLER.getResult(resultSet, "column"))
//				.isEqualTo(ImageReference.parse(INTERNAL_NAME));
	}

	@Override
	@Test
	void getResultFromResultSetByPosition() throws Exception {
//		when(resultSet.getBytes(1)).thenReturn(INTERNAL_DATA);
//		assertThat(HANDLER.getResult(resultSet, 1))
//				.isEqualTo(ImageReference.parse(INTERNAL_NAME));
	}

	@Override
	@Test
	void getResultFromCallableStatement() throws Exception {
//		when(callableStatement.getBytes(1)).thenReturn(INTERNAL_DATA);
//		assertThat(HANDLER.getResult(callableStatement, 1))
//				.isEqualTo(ImageReference.parse(INTERNAL_NAME));
	}

	@Test
	void getResultNull() throws Exception {
		when(resultSet.getBytes(1)).thenReturn(null);
		assertThat(HANDLER.getResult(resultSet, 1)).isNull();
	}

	@Test
	void hexName() throws SQLException {
		when(resultSet.getBytes(1)).thenReturn(HASH_DATA);

		assertThat(HANDLER.getResult(resultSet, 1))
				.isEqualTo(new ImageReference(HASH_NAME, ImageType.WEBP));
	}

	@Test
	void decodeInvalidValue() throws SQLException {
		when(resultSet.getBytes(1)).thenReturn("invalid".getBytes());

		assertThatThrownBy(() -> HANDLER.getResult(resultSet, 1))
				.isInstanceOf(ResultMapException.class)
				.hasCauseInstanceOf(SQLException.class);
	}
}
