package net.kaciras.blog.api.category;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Utils;
import net.kaciras.blog.infrastructure.DBUtils;
import net.kaciras.blog.infrastructure.event.category.CategoryRemovedEvent;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

/**
 * 基于ClosureTable的的数据库存储分类树实现。
 *
 * @author Kaciras
 */
@RequiredArgsConstructor
@Repository
public class CategoryRepository {

	private final CategoryDAO categoryDAO;
	private final DaoHelper helper;
	private final MessageClient messageClient;

	public Category get(int id) {
		return DBUtils.checkNotNullResource(categoryDAO.selectAttributes(id));
	}

	public int size() {
		return categoryDAO.selectCount();
	}

	@Transactional
	public int add(Category category, int parent) {
		Utils.checkNotNegative(parent, "parent");
		if (parent > 0) {
			helper.requireContains(parent);
		}
		try {
			categoryDAO.insert(category);
		} catch (DataIntegrityViolationException ex) {
			throw new IllegalArgumentException("分类实体中存在不合法的属性值", ex);
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
		if (category.getId() == 0) {
			categoryDAO.updateRoot(category);
		} else {
			DBUtils.checkEffective(categoryDAO.update(category));
		}
	}

	@Transactional
	public void remove(int id) {
		Utils.checkPositive(id, "id"); // 顶级分类不可删除
		helper.requireContains(id);
		var parent = categoryDAO.selectAncestor(id, 1);

		if (parent == null) {
			parent = 0;
		}
		get(id).moveSubTree(parent);
		deleteBoth(id);

		messageClient.send(new CategoryRemovedEvent(id, parent));
	}

	@Transactional
	public void removeTree(int id) {
		Utils.checkPositive(id, "id");
		helper.requireContains(id);
		deleteBoth(id);
		Arrays.stream(categoryDAO.selectDescendant(id)).forEach(this::deleteBoth);
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
