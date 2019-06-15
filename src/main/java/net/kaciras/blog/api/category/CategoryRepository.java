package net.kaciras.blog.api.category;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Utils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * 基于ClosureTable的的数据库存储分类树实现。
 *
 * @author Kaciras
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Repository
public class CategoryRepository {

	private final CategoryDAO dao;

	public Category get(int id) {
		return Utils.checkNotNullResource(dao.selectAttributes(id));
	}

	public int size() {
		return dao.selectCount();
	}

	@Transactional
	public int add(@NonNull Category category, int parent) {
		Utils.checkNotNegative(parent, "parent");
		if (parent > 0) {
			requireContains(parent);
		}
		try {
			dao.insert(category);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException("分类实体中存在不合法的属性值", ex);
		}
		dao.insertPath(category.getId(), parent);
		dao.insertNode(category.getId());
		return category.getId();
	}

	/**
	 * 该方法仅更新分类的属性，不修改继承关系，若要移动节点请使用
	 * <code>move</code>和<code>moveTree</code>
	 *
	 * @param category 新的分类信息对象
	 */
	public void update(@NonNull Category category) {
		if (category.getId() == 0) {
			dao.updateRoot(category);
		} else {
			Utils.checkEffective(dao.update(category));
		}
	}

	@Transactional
	public void remove(int id) {
		Utils.checkPositive(id, "id"); // 顶级分类不可删除
		requireContains(id);
		var parent = dao.selectAncestor(id, 1);

		if (parent == null) {
			parent = 0;
		}
		get(id).moveSubTree(parent);
		deleteBoth(id);
	}

	@Transactional
	public void removeTree(int id) {
		Utils.checkPositive(id, "id");
		requireContains(id);
		deleteBoth(id);
		Arrays.stream(dao.selectDescendant(id)).forEach(this::deleteBoth);
	}

	/**
	 * 删除一个分类，两个表中的相关记录都删除
	 *
	 * @param id 分类id
	 */
	private void deleteBoth(int id) {
		dao.delete(id);
		dao.deletePath(id);
	}

	private void requireContains(int id) {
		var v = dao.contains(id);
		if (v == null || !v) throw new IllegalArgumentException("指定的分类不存在");
	}
}
