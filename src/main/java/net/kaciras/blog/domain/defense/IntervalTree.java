package net.kaciras.blog.domain.defense;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@SuppressWarnings({"Duplicates", "StatementWithEmptyBody"})
public class IntervalTree<T extends Comparable<T>> {

	private static final boolean RED = false;
	private static final boolean BLACK = true;

	private static final class Node<T extends Comparable<T>> {

		private Interval<T> interval;

		private T max;

		private Node<T> left, right, parent;

		private boolean color;

		private Node(Interval<T> interval) {
			this.interval = interval;
			this.max = interval.high();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Node<?> node = (Node<?>) o;
			return Objects.equals(interval.low(), node.interval.low())
					&& Objects.equals(interval.high(), node.interval.high());
		}

		@Override
		public int hashCode() {
			return Objects.hash(interval.low(), interval.high());
		}

		@Override
		public String toString() {
			return String.format("%s[%s]%s", color ? "B" : "R", interval, max);
		}
	}

	private Node<T> root;
	private int size;

	private void verifyRange(T low, T high) {
		if (low.compareTo(high) > 0) throw new IllegalArgumentException("lower point is bigger than high point");
	}

	public boolean add(Interval<T> interval) {
		verifyRange(interval.low(), interval.high());

		if (root == null) {
			root = new Node<>(interval);
			root.color = BLACK;
			size = 1;
			return true;
		}

		Node<T> parent = null;
		Node<T> node = root;

		while (node != null) {
			parent = node;
			int cmp = compareInterval(interval, node);
			if (cmp == 0) return false;
			node = cmp < 0 ? node.left : node.right;
		}

		Node<T> newNode = new Node<>(interval);
		newNode.parent = parent;
		if (compareInterval(interval, parent) < 0) {
			parent.left = newNode;
		} else {
			parent.right = newNode;
		}

		size++;
		computeMaxRecursive(parent);
		balanceAfterInsert(newNode);
		root.color = BLACK;
		return true;
	}

	private Node<T> findEquals(T low, T high) {
		verifyRange(low, high);
		Node<T> node = root;
		while (node != null) {
			int cmp = compareInterval(low, high, node);
			if (cmp == 0) return node;
			node = cmp < 0 ? node.left : node.right;
		}
		return null;
	}

	private int compareInterval(Interval<T> interval, Node<T> node) {
		return compareInterval(interval.low(),interval.high(), node);
	}

	private int compareInterval(T low, T high, Node<T> node) {
		int cmp = low.compareTo(node.interval.low());
		return cmp == 0 ? high.compareTo(node.interval.high()) : cmp;
	}

	private void balanceAfterInsert(Node<T> node) {
		if (node == root || node.color == BLACK) {
			return;
		}
		Node<T> p = node.parent;

		if (p.color == RED) {
			if (p == p.parent.left) {
				if (node == p.right) {
					rotateLeft(node);
					p = node; //这句去掉也是正确的，因为去掉后下面的if就不会执行，进入下一轮，此
							  //时可以看做是向红节点左边插入，将执行下面的if，但是这会导致多递归一次。
					node = node.left;
				}
				if (node == p.left) {
					rotateRight(p);
					flipColors(p);
				}
			} else {
				if (node == p.left) {
					rotateRight(node);
					p = node;
					node = node.right;
				}
				if (node == p.right) {
					rotateLeft(p);
					flipColors(p);
				}
			}
		} else {
			if (p.right == node && isRed(p.left)) {
				flipColors(p);
			} else if (p.left == node && isRed(p.right)) {
				flipColors(p);
			}
		}
		balanceAfterInsert(p);
	}

	public Interval<T> remove(T low, T high) {
		Node<T> node = findEquals(low, high); //verifyRange in findEquals
		if (node == null) {
			return null;
		}
		remove(node);
		return node.interval;
	}

	private void remove(Node<T> node) {
		Node<T> suc = null;
		if (node.left != null) { //左边的最右
			for (suc = node.left; suc.right != null; suc = suc.right) ;
		} else if (node.right != null) { //右边的最左
			for (suc = node.right; suc.left != null; suc = suc.left) ;
		}

		if (suc != null) {
			node.interval = suc.interval;
			node = suc;
		} else if (node == root) {
			size = 0;
			root = null; //整棵树只有一个根节点
			return;
		} else {
			suc = node; //被删除节点为叶节点，替代者就是自己
		}

		/*
		 * 替代节点只是最接近被删除节点的，并不一定是叶节点，但一定不会有两个
		 * 子节点。我们的目标是将被删除节点转换到叶节点，所以这里还要再判断一下
		 */
		Node<T> rep = suc.left == null ? suc.right : suc.left;
		if (rep != null) {
			if (node == node.parent.left)
				node.parent.left = rep;
			else
				node.parent.right = rep;
			rep.parent = node.parent;

			/*
			 * 上面说到替代节点不会有两个子节点，所以被删除节点没有兄
			 * 弟，直接从其父节点（替代节点）开始修复即可。
			 */
			computeMaxRecursive(rep);
			if (node.color == BLACK) {
				balanceAfterDeletion(rep);
			}
		} else {
			if (node.color == BLACK) {
				balanceAfterDeletion(node);
			}
			if (node.parent != null) {
				if (node == node.parent.left)
					node.parent.left = null;
				else
					node.parent.right = null;
			}
			computeMaxRecursive(node.parent);
		}

		size--;
	}

	private void balanceAfterDeletion(Node<T> node) {
		if (node == root || node.color == RED) {
			node.color = BLACK;
			return;
		}
		Node<T> p = node.parent;

		if (node == p.left) {
			Node<T> another = p.right;
			if (another.color == RED) {
				p.color = RED;
				another.color = BLACK;
				rotateLeft(another);
				another = p.right;
			}
			if (blackOrNull(another.left) && blackOrNull(another.right)) {
				another.color = RED;
				balanceAfterDeletion(p);
			} else {
				if (isRed(another.left)) {
					another.color = RED;
					another.left.color = BLACK;
					rotateRight(another.left);
					another = p.right;
				}
				another.color = another.parent.color;
				another.parent.color = BLACK;
				another.right.color = BLACK;
				rotateLeft(another);
			}
		} else {
			Node<T> another = p.left;
			if (another.color == RED) {
				p.color = RED;
				another.color = BLACK;
				rotateRight(another);
				another = p.left;
			}
			if (blackOrNull(another.right) && blackOrNull(another.left)) {
				another.color = RED;
				balanceAfterDeletion(p);
			} else {
				if (isRed(another.right)) {
					another.color = RED;
					another.right.color = BLACK;
					rotateLeft(another.right);
					another = p.left;
				}
				another.color = another.parent.color;
				another.parent.color = BLACK;
				another.left.color = BLACK;
				rotateRight(another);
			}
		}
	}

	private void rotateLeft(Node<T> node) {
		Node<T> p = node.parent;

		if (p.parent == null) {
			root = node;
		} else if (p.parent.left == p) {
			p.parent.left = node;
		} else {
			p.parent.right = node;
		}

		p.right = node.left;
		if (p.right != null) {
			p.right.parent = p;
		}
		node.left = p;

		node.parent = p.parent;
		p.parent = node;
		computeMax(p);
		computeMax(node);
	}

	private void rotateRight(Node<T> node) {
		Node<T> p = node.parent;

		if (p.parent == null) {
			root = node;
		} else if (p.parent.right == p) {
			p.parent.right = node;
		} else {
			p.parent.left = node;
		}

		p.left = node.right;
		if (p.left != null) {
			p.left.parent = p;
		}

		node.parent = p.parent;
		p.parent = node;
		node.right = p;
		computeMax(p);
		computeMax(node);
	}

	private boolean blackOrNull(Node node) {
		return node == null || node.color == BLACK;
	}

	private boolean isRed(Node node) {
		return node != null && node.color == RED;
	}

	/**
	 * 类似23树的分裂，将节点设为红，两个子节点设为黑。
	 *
	 * @param node 节点
	 */
	private void flipColors(Node node) {
		node.color = RED;
		if (node.left != null) node.left.color = BLACK;
		if (node.right != null) node.right.color = BLACK;
	}

	/**
	 * 添加或删除节点后，节点到根的路径上所有的max值都需要更新。
	 *
	 * @param node 节点
	 */
	private void computeMaxRecursive(Node<T> node) {
		while (node != null) {
			T max = node.max;
			computeMax(node);
			if (node.max.compareTo(max) <= 0) {
				break;
			}
			node = node.parent;
		}
	}

	/**
	 * 将节点的max值设为其与两个子节点中最大的一个。
	 *
	 * @param node 节点
	 */
	private void computeMax(Node<T> node) {
		T max = node.max;
		if (node.left != null) {
			max = maxOf(node.left.max, max);
		}
		if (node.right != null) {
			max = maxOf(node.right.max, max);
		}
		node.max = max;
	}

	private T maxOf(T a, T b) {
		return a.compareTo(b) < 0 ? b : a;
	}

	public boolean contains(Interval<T> interval) {
		return contains(interval.low(), interval.high());
	}

	public boolean contains(T low, T high) {
		return findEquals(low, high) != null;
	}

	public Interval<T> get(T low, T high) {
		Node<T> node = findEquals(low, high);
		return node != null ? node.interval : null;
	}

	public int size() {
		return size;
	}

/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - *\
					 		  区间查询
\* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */

	/**
	 * 查询任意一个包含了value的区间
	 *
	 * @param value 给定值
	 * @return 区间
	 */
	public Interval<T> intersect(T value) {
		AtomicReference<Interval<T>> ref = new AtomicReference<>();
		matchInclude(root, value, value, i -> {
			ref.set(i);
			return false;
		});
		return ref.get();
	}

	/**
	 * 查询所有包含了value的区间
	 *
	 * @param value 给定值
	 * @return 区间列表
	 */
	public List<Interval<T>> intersectAll(T value) {
		return include(value, value);
	}

	/**
	 * 查询所有与给定区间有相交的区间。
	 *
	 * @param low 给定区间左端点
	 * @param high 给定区间右端点
	 * @return 区间列表
	 */
	public List<Interval<T>> intersectAll(T low, T high) {
		verifyRange(low, high);
		List<Interval<T>> collection = new ArrayList<>();
		matchOverlap(root, low, high, collection::add);
		return collection;
	}

	/**
	 * 查找所有包含了以low，high参数为端点的区间。
	 *
	 * @param low  左端点
	 * @param high 右端点
	 * @return 区间列表
	 */
	public List<Interval<T>> include(T low, T high) {
		verifyRange(low, high);
		List<Interval<T>> collection = new ArrayList<>();
		matchInclude(root, low, high, collection::add);
		return collection;
	}

	private boolean matchInclude(Node<T> node, T low, T high, Predicate<Interval<T>> acceptor) {
		if (low.compareTo(node.max) > 0) {
			return true;
		}
		boolean _continue = true; //false则结束查询
		Interval<T> interval = node.interval;

		if (low.compareTo(interval.low()) >= 0) {
			if (high.compareTo(interval.high()) <= 0) {
				_continue = acceptor.test(interval);
			}
			if (_continue && node.right != null) {
				_continue = matchInclude(node.right, low, high, acceptor);
			}
		}

		if (_continue && node.left != null) {
			_continue = matchInclude(node.left, low, high, acceptor);
		}
		return _continue;
	}

	private boolean matchOverlap(Node<T> node, T low, T high, Predicate<Interval<T>> acceptor) {
		if (low.compareTo(node.max) > 0) {
			return true;
		}
		boolean _continue = true; //false则结束查询
		Interval<T> interval = node.interval;

		if (high.compareTo(interval.low()) >= 0) {
			if (low.compareTo(interval.high()) <= 0) {
				_continue = acceptor.test(interval);
			}
			if (_continue && node.right != null) {
				_continue = matchOverlap(node.right, low, high, acceptor);
			}
		}

		if (_continue && node.left != null) {
			_continue = matchOverlap(node.left, low, high, acceptor);
		}
		return _continue;
	}

	/**
	 * 查询所有被以low，high参数为端点的区间所包含的区间。
	 *
	 * @param low  左端点
	 * @param high 右端点
	 * @return 区间列表
	 */
	public List<Interval<T>> includeIn(T low, T high) {
		verifyRange(low, high);
		List<Interval<T>> collection = new ArrayList<>();
		matchIncludeIn(root, low, high, collection::add);
		return collection;
	}

	private boolean matchIncludeIn(Node<T> node, T low, T high, Predicate<Interval<T>> acceptor) {
		if (low.compareTo(node.max) > 0) {
			return true;
		}
		boolean _continue = true; //false则结束查询
		Interval<T> interval = node.interval;

		if (low.compareTo(interval.low()) <= 0) {
			if (high.compareTo(interval.high()) >= 0) {
				_continue = acceptor.test(interval);
			}
			if (_continue && node.left != null) {
				_continue = matchIncludeIn(node.left, low, high, acceptor);
			}
		}

		if (_continue && high.compareTo(interval.low()) >= 0 && node.right != null) {
			_continue = matchIncludeIn(node.right, low, high, acceptor);
		}
		return _continue;
	}

}
