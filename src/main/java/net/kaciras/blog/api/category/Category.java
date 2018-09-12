package net.kaciras.blog.api.category;

import lombok.*;
import net.kaciras.blog.api.Utils;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

@EqualsAndHashCode(of = "id", callSuper = false)
@Data
@Configurable
public class Category extends CategoryAttributes {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private CategoryDAO dao;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private DaoHelper helper;

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private MessageClient messageClient;

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	private int id;

	Integer getParent() {
		return dao.selectAncestor(id, 1);
	}

	int getLevel() {
		return dao.selectDistance(0, id);
	}

	@NonNull
	List<Category> getPath() {
		return dao.selectPathToRoot(id);
	}

	/**
	 * 获取此分类到指定上级分类之间的所有分类。
	 *
	 * @param ancestor 上级分类id
	 * @return 路径上所有的分类，如果ancestor不是此分类的上级分类则为null
	 */
	@Nullable
	List<Category> pathTo(int ancestor) {
		Utils.checkPositive(ancestor, "ancestor");
		return dao.selectPathToAncestor(id, ancestor);
	}

	void moveTo(Category target) {
		if (this.equals(target)) {
			throw new IllegalArgumentException("不能移动到自己下面");
		}
		moveSubTree(id, dao.selectAncestor(id, 1));
		moveNode(id, target.getId());
	}

	void moveTreeTo(Category target) {
		if (this.equals(target)) {
			throw new IllegalArgumentException("不能移动到自己下面");
		}

		// 通过子节点距离判断是否是移到自己的子节点下面
		var distance = dao.selectDistance(id, target.getId());

		if (distance != null) {
			if (distance == 0) {
				throw new IllegalArgumentException("不能移动到自己下面");
			}
			// 如果移动的目标是其子类，需要先把子类移动到本类的位置
			int parent = dao.selectAncestor(id, 1);
			moveNode(target.getId(), parent);
			moveSubTree(target.getId(), target.getId());
		}

		moveNode(id, target.getId());
		moveSubTree(id, id);
	}

	/**
	 * 将指定节点移动到另某节点下面，该方法不修改子节点的相关记录，
	 * 为了保证数据的完整性，需要与moveSubTree()方法配合使用。
	 *
	 * @param id     指定节点id
	 * @param parent 某节点id
	 */
	private void moveNode(int id, int parent) {
		dao.deletePath(id);
		dao.insertPath(id, parent);
		dao.insertNode(id);
	}

	void moveSubTree(Category parent) {
		moveSubTree(id, parent.getId());
	}

	/**
	 * 将指定节点的所有子树移动到某节点下
	 * 如果两个参数相同，则相当于重建子树，用于移动父节点后同步路径
	 *
	 * @param id     指定节点id
	 * @param parent 某节点id
	 */
	private void moveSubTree(int id, int parent) {
		int[] subs = dao.selectSubId(id);
		for (int sub : subs) {
			moveNode(sub, parent);
			moveSubTree(sub, sub);
		}
	}
}
