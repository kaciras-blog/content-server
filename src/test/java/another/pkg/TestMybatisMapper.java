package another.pkg;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
interface TestMybatisMapper {

	@Select("SELECT 1 FROM article WHERE id=-1")
	boolean selectNullableBool();
}
