package net.kaciras.blog.domain.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
class MessageRepository {

	private final MessageDao messageDao;

	Message get(int id) {
		return null;
	}
}
