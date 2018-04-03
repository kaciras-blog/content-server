package net.kaciras.blog.domain.category;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kaciras.blog.domain.Utils;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@EqualsAndHashCode(of = "id")
@Data
public class Category {

	static CategoryDAO dao;
	static DaoHelper helper;
	static MessageClient messageClient;

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	private int id;
	private String name;

	private String cover;
	private String description;
	private String background;

	Integer getParent() {
		return dao.selectAncestor(id, 1);
	}

	int getLevel() {
		return dao.selectDistance(0, id);
	}

	List<Category> getPath() {
		return dao.selectPathToRoot(id);
	}

	List<Category> pathTo(int ancestor) {
		Utils.checkPositive(ancestor, "ancestor");
		return dao.selectPathToAncestor(id, ancestor);
	}

	@Transactional
	void moveTo(int target) {
		if (id == target) {
			throw new IllegalArgumentException("不能移动到自己下面");
		}
		Utils.checkNotNegative(target, "target");
		if (target > 0) {
			helper.requireContains(target);
		}
		moveSubTree(id, dao.selectAncestor(id, 1));
		moveNode(id, target);
	}

	@Transactional
	void moveTreeTo(int target) {
		Utils.checkNotNegative(target, "target");
		if (target > 0) {
			helper.requireContains(target);
		}

		Integer distance = dao.selectDistance(id, target);
		if (distance == null) {
			// 移动到父节点或其他无关系节点，不需要做额外动作
		} else if (distance == 0) {
			throw new IllegalArgumentException("不能移动到自己下面");
		} else {
			// 如果移动的目标是其子类，需要先把子类移动到本类的位置
			int parent = dao.selectAncestor(id, 1);
			moveNode(target, parent);
			moveSubTree(target, target);
		}

		moveNode(id, target);
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

	void moveSubTree(int parent) {
		moveSubTree(id, parent);
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
