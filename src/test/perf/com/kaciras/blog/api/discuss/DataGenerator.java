package com.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 测试数据生成工具，向数据库中插入满足引用模式和楼中楼模式最大查询结果的数据。
 */
@RequiredArgsConstructor
final class DataGenerator {

	private final List<Discussion> list = new ArrayList<>();

	private final DiscussionQueryPerf perf;

	private int objectId;

	public void insertTestData() {
		var context = perf.getContext();
		generateTestData();

		var transaction = context.getBean(PlatformTransactionManager.class);
		var status = transaction.getTransaction(new DefaultTransactionDefinition());
		var dao = context.getBean(DiscussionDAO.class);

		try {
			list.forEach(dao::insert);
			transaction.commit(status);
		} catch (Exception ex) {
			transaction.rollback(status);
		}
	}

	private void generateTestData() {
		objectId = 1;
		for (int i = 0; i < 30; i++) {
			var top = newValue();
			IntStream.range(0, 5).forEach(__ -> addChild(top));
		}

		objectId = 2;
		for (int i = 0; i < 30; i++) {
			newValue();
		}
		var s = list.size();
		for (int i = s - 30; i < s; i++) {
			addChild(list.get(i));
		}
	}

	private void addChild(Discussion parent) {
		var value = newValue();
		value.setParent(parent.getId());

		if (parent.getParent() == 0) {
			value.setNestId(parent.getId());
		} else {
			value.setNestId(parent.getNestId());
		}
	}

	private Discussion newValue() {
		var value = new Discussion();
		value.setContent("test content");
		value.setObjectId(objectId);
		value.setType(1);
		value.setTime(Instant.EPOCH);
		value.setAddress(InetAddress.getLoopbackAddress());
		value.setState(DiscussionState.VISIBLE);

		list.add(value);
		value.setId(list.size());
		return value;
	}

	public static void main(String... args) {
		var perf = new DiscussionQueryPerf();
		perf.initSpringContext();
		new DataGenerator(perf).insertTestData();
		perf.getContext().close();
	}
}
