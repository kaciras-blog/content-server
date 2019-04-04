package net.kaciras.blog.api.category;

import lombok.*;
import net.kaciras.blog.api.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.lang.NonNull;

import java.util.List;

@ToString(of = "id")
@EqualsAndHashCode(of = "id", callSuper = false)
@Data
@Configurable
public class Category extends CategoryAttributes {

	@Autowired
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private CategoryDAO dao;

	/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	private int id;

	public Category getParent() {
		return dao.selectParentAttributes(id);
	}

	public List<Category> getChildren() {
		return dao.selectSubLayer(id, 1);
	}

	public int getLevel() {
		return dao.selectDistance(0, id);
	}

	public List<Category> getPath() {
		return getPathTo(0);
	}

	/**
	 * 获取此分类到指定上级分类之间的所有分类。
	 *
	 * @param ancestor 上级分类id
	 * @return 路径上所有的分类，如果ancestor不是此分类的上级分类则返回空列表
	 */
	@NonNull
	public List<Category> getPathTo(int ancestor) {
		Utils.checkNotNegative(ancestor, "ancestor");
		if (ancestor == 0) {
			return dao.selectPathToRoot(id);
		}
		return dao.selectPathToAncestor(id, ancestor);
	}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 								移动操作
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	/**
	 * 将一个分类移动到目标分类下面（成为其子分类）。被移动分类的子类将自动上浮（成为
	 * 指定分类父类的子分类），即使目标是指定分类原本的父类。
	 * <p>
	 * 例如下图(省略顶级分类)：
	 * 1                                    1
	 * |                                  / | \
	 * 2                                 3  4  5
	 * / | \        (id=2).moveTo(7)            / \
	 * 3  4  5       ----------------->         6   7
	 * / \                                /  / | \
	 * 6    7                              8  9  10 2
	 * /    /  \
	 * 8    9    10
	 *
	 * @param target 目标分类的id
	 * @throws IllegalArgumentException 如果target所表示的分类不存在、或此分类的id==target
	 */
	public void moveTo(Category target) {
		if (this.equals(target)) {
			throw new IllegalArgumentException("不能移动到自己下面");
		}
		moveSubTree(id, dao.selectAncestor(id, 1));
		moveNode(id, target.getId());
	}

	/**
	 * 将一个分类移动到目标分类下面（成为其子分类），被移动分类的子分类也会随着移动。
	 * 如果目标分类是被移动分类的子类，则先将目标分类（连带子类）移动到被移动分类原来的
	 * 的位置，再移动需要被移动的分类。
	 * <p>
	 * 例如下图(省略顶级分类)：
	 * 1                                      1
	 * |                                      |
	 * 2                                      7
	 * / | \        (id=2).moveTreeTo(7)      / | \
	 * 3  4  5      -------------------->     9  10  2
	 * / \                                  / | \
	 * 6    7                                3  4  5
	 * /    /  \                                    |
	 * 8    9    10                                  6
	 * |
	 * 8
	 *
	 * @param target 目标分类的id
	 * @throws IllegalArgumentException 如果id或target所表示的分类不存在、或id==target
	 */
	public void moveTreeTo(Category target) {
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
			var parent = dao.selectAncestor(id, 1);
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

	public void moveSubTree(int parent) {
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
		var subs = dao.selectSubId(id);
		for (var sub : subs) {
			moveNode(sub, parent);
			moveSubTree(sub, sub);
		}
	}
}
