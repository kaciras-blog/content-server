package another.pkg;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.OptionalInt;

@Mapper
interface TestMybatisMapper {

	@Select("SELECT 1 FROM article WHERE id=-1")
	boolean selectNullableBool();

	// ID = 36 的文章存在
	@Select("SELECT id FROM article WHERE id=36")
	OptionalInt selectOptionalInt();
}
