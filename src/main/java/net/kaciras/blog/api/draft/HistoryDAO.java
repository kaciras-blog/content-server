package net.kaciras.blog.api.draft;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
interface HistoryDAO {

	@Select("SELECT MAX(save_count) FROM draft_history WHERE id=#{id}")
	Integer selectLastSaveCount(int id);

	@Select("SELECT *, CHAR_LENGTH(content) AS wordCount FROM draft_history WHERE id=#{id} AND save_count=#{saveCount}")
	@ResultMap("net.kaciras.blog.api.draft.HistoryDAO.DraftHistoryMap")
	History select(int id, int saveCount);

	// SQL in xml file.
	void insert(int id, int saveCount, DraftContent draft);

	// SQL in xml file.
	void deleteOldest(int id);

	@Select("SELECT COUNT(*) FROM draft_history WHERE id=#{id}")
	int selectCount(int id);

	@Select("SELECT save_count, CHAR_LENGTH(content) AS wordCount, `time` " +
			"FROM draft_history WHERE id=#{id} ORDER BY save_count DESC")
	@ResultMap("net.kaciras.blog.api.draft.HistoryDAO.DraftHistoryMap")
	List<History> selectAll(int id);

	@Update("UPDATE draft_history SET " +
			"title=#{value.title}," +
			"cover=#{value.cover}," +
			"summary=#{value.summary}," +
			"keywords=#{value.keywords}," +
			"content=#{value.content} " +
			"WHERE id=#{id} AND save_count=#{saveCount}")
	int update(int id, int saveCount, DraftContent value);
}
