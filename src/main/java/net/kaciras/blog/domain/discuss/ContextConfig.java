package net.kaciras.blog.domain.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.Utils;
import net.kaciras.blog.domain.permission.Authenticator;
import net.kaciras.blog.domain.permission.AuthenticatorFactory;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.message.event.DiscussionDeletedEvent;
import net.kaciras.blog.infrastructure.message.event.DiscussionRestoreEvent;
import net.kaciras.blog.infrastructure.message.event.DiscussionVoteEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration("DiscussionContextConfig")
class ContextConfig {

	private final MessageClient messageClient;
	private final DiscussionDAO dao;
	private final VoteDAO voteDAO;

	@PostConstruct
	void setUp() {
		messageClient.subscribe(DiscussionVoteEvent.class, this::updateVotes);

		messageClient.subscribe(DiscussionDeletedEvent.class,
				event -> dao.updateDeleted(event.getDiscussionId(), true));

		messageClient.subscribe(DiscussionRestoreEvent.class,
				event -> dao.updateDeleted(event.getDiscussionId(), false));

		Discussion.messageClient = messageClient;
	}

	private void updateVotes(DiscussionVoteEvent event) {
		try {
			if (event.isRevoke()) {
				Utils.checkEffective(voteDAO.deleteRecord(event.getDiscussionId(), event.getUserId()));
				dao.descreaseVote(event.getDiscussionId());
			} else {
				voteDAO.insertRecord(event.getDiscussionId(), event.getUserId());
				dao.increaseVote(event.getDiscussionId());
			}
		} catch (DataIntegrityViolationException ex) {
			throw new ResourceStateException();
		}
	}

	@Bean
	Authenticator discussionAuthenticator(AuthenticatorFactory factory) {
		return factory.create("DISCUSSION");
	}
}
