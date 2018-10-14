package net.kaciras.blog.api.discuss;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
interface DiscussionDAO {

	@Insert("INSERT INTO discussion(user_id, object_id, `type`, floor, parent, content) " +
			"VALUES (#{userId}, #{objectId}, #{type}, #{floor}, #{parent}, #{content})")
	@Options(useGeneratedKeys = true, keyColumn = "id")
	void insert(Discussion discuz);

	@SelectProvider(type = SqlProvidor.class, method = "select")
	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.DiscussionMap")
	List<Discussion> selectList(DiscussionQuery query);

	@SelectProvider(type = SqlProvidor.class, method = "selectCount")
	int selectCount(DiscussionQuery query);

	@Select("SELECT * FROM discussion WHERE id=#{id}")
	@ResultMap("net.kaciras.blog.domain.dao.ResultMap.DiscussionMap")
	Discussion selectById(long id);

	@Update("UPDATE discussion SET deleted=#{value} WHERE id=#{id}")
	int updateDeleted(long id, boolean value);

	/**
	 * 查找指定对象的评论数量，不包含评论的回复（楼中楼）。
	 *
	 * @param oid 对象ID
	 * @param type 对象类型
	 * @return 评论数
	 */
	@Select("SELECT COUNT(*) FROM discussion " +
			"WHERE object_id=#{arg0} AND `type`=#{arg1} AND parent=0")
	int selectCountByObject(int oid, int type);
}
