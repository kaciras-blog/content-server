package com.kaciras.blog.infra.codec;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.executor.result.ResultMapException;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

final class ImageReferenceTypeHandlerTest extends AbstractTypeHandlerTest {

	private static final TypeHandler<ImageReference> HANDLER = new ImageReferenceTypeHandler();

	private static final String INTERNAL_NAME = "picture.png";
	private static final byte[] INTERNAL_DATA;

	private static final String HASH_NAME = "4c21ffdf38ae94a4108ed27d3c650d55fce4798438795d42be4991d0333c0208";
	private static final byte[] HASH_DATA;

	static {
		INTERNAL_DATA = ByteBuffer.allocate(33)
				.put((byte) 0)
				.put((byte) INTERNAL_NAME.length())
				.put(INTERNAL_NAME.getBytes()).array();

		HASH_DATA = ByteBuffer.allocate(33)
				.put((byte) 2)
				.put(CodecUtils.decodeHex(HASH_NAME)).array();
	}

	@Override
	@Test
	void setParameter() throws Exception {
		HANDLER.setParameter(preparedStatement, 1, ImageReference.parse(INTERNAL_NAME), JdbcType.BINARY);
		Mockito.verify(preparedStatement).setBytes(1, INTERNAL_DATA);
	}

	@Test
	void encodeInvalidName() {
		var image = new ImageReference(HASH_NAME + INTERNAL_NAME, ImageType.Internal);

		assertThatThrownBy(() -> HANDLER.setParameter(preparedStatement, 1, image, JdbcType.BINARY))
				.isInstanceOf(PersistenceException.class);
	}

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
		when(resultSet.getBytes("column")).thenReturn(INTERNAL_DATA);
		assertThat(HANDLER.getResult(resultSet, "column"))
				.isEqualTo(ImageReference.parse(INTERNAL_NAME));
	}

	@Override
	@Test
	void getResultFromResultSetByPosition() throws Exception {
		when(resultSet.getBytes(1)).thenReturn(INTERNAL_DATA);
		assertThat(HANDLER.getResult(resultSet, 1))
				.isEqualTo(ImageReference.parse(INTERNAL_NAME));
	}

	@Override
	@Test
	void getResultFromCallableStatement() throws Exception {
		when(callableStatement.getBytes(1)).thenReturn(INTERNAL_DATA);
		assertThat(HANDLER.getResult(callableStatement, 1))
				.isEqualTo(ImageReference.parse(INTERNAL_NAME));
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
