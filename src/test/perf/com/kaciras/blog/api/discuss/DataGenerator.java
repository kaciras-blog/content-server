package com.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;

import java.net.InetAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@RequiredArgsConstructor
final class DataGenerator {

	private final List<Discussion> list = new ArrayList<>();
	private final DiscussionQueryPerf perf;

	private int objectId;

	public void insertTestData() {
		var dao = perf.getContext().getBean(DiscussionDAO.class);
		generateTestData();
		list.forEach(dao::insert);
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
		value.setState(DiscussionState.Visible);

		list.add(value);
		value.setId(list.size());
		return value;
	}

	public static void main(String... args) throws Exception {
		var perf = new DiscussionQueryPerf();
		perf.initSpringContext();
		new DataGenerator(perf).insertTestData();
		perf.closeSpringContext();
	}
}
