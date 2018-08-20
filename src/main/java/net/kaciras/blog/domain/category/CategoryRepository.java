package net.kaciras.blog.domain.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.Utils;
import net.kaciras.blog.infrastructure.event.category.CategoryRemovedEvent;
import net.kaciras.blog.infrastructure.io.DBUtils;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 基于ClosureTable的的数据库存储分类树实现。
 *
 * @author Kaciras
 */
@RequiredArgsConstructor
@Repository
class CategoryRepository {

	private final CategoryDAO categoryDAO;
	private final DaoHelper helper;
	private final MessageClient messageClient;

	@NonNull
	public Category get(int id) {
		Utils.checkNotNegative(id, "id");
		return DBUtils.checkNotNullResource(categoryDAO.selectAttributes(id));
	}

	public int size() {
		return categoryDAO.selectCount();
	}

	public int sizeOfLevel(int level) {
		Utils.checkPositive(level, "level");
		return categoryDAO.selectCountByLayer(level);
	}

	public List<Category> getSubCategories(int id) {
		return getSubCategories(id, 1);
	}

	public List<Category> getSubCategories(int id, int n) {
		Utils.checkNotNegative(id, "id");
		Utils.checkPositive(n, "n");
		return categoryDAO.selectSubLayer(id, n);
	}

	@Transactional
	public int add(Category category, int parent) {
		Utils.checkNotNegative(parent, "parent");
		if (parent > 0) {
			helper.requireContains(parent); // getAncestor方法用于检查分类是否存在，下同
		}
		try {
			categoryDAO.insert(category);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException("分类实体中存在不合法的属性值");
		}
		categoryDAO.insertPath(category.getId(), parent);
		categoryDAO.insertNode(category.getId());
		return category.getId();
	}

	/**
	 * 该方法仅更新分类的属性，不修改继承关系，若要移动节点请使用
	 * <code>move</code>和<code>moveTree</code>
	 *
	 * @param category 新的分类信息对象
	 */
	public void update(Category category) {
		DBUtils.checkEffective(categoryDAO.update(category));
	}

	@Transactional
	public void remove(int id) {
		helper.requireContains(id);
		var parent = categoryDAO.selectAncestor(id, 1);

		if (parent == null) {
			parent = 0;
		}

		get(id).moveSubTree(get(parent));
		deleteBoth(id);

		messageClient.send(new CategoryRemovedEvent(id, parent));
	}

	@Transactional
	public void deleteTree(int id) {
		helper.requireContains(id);
		deleteBoth(id);
		for (int des : categoryDAO.selectDescendant(id)) {
			deleteBoth(des);
		}
	}

	/**
	 * 删除一个分类，两个表中的相关记录都删除
	 *
	 * @param id 分类id
	 */
	private void deleteBoth(int id) {
		categoryDAO.delete(id);
		categoryDAO.deletePath(id);
	}
}
