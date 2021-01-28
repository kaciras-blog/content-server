package misc.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.OptionalInt;
import java.util.stream.Stream;

@Mapper
interface TestMybatisMapper {

	@SuppressWarnings("UnusedReturnValue")
	@Select("SELECT 1 FROM article WHERE id=-1")
	boolean selectNullableBool();

	@Select("SELECT id FROM article")
	Stream<Integer> selectStream();

	// ID = 36 的文章存在
	@Select("SELECT id FROM article WHERE id=36")
	OptionalInt selectOptionalInt();
}
