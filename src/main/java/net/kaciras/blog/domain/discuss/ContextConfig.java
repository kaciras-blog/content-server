package net.kaciras.blog.domain.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.permission.Authenticator;
import net.kaciras.blog.domain.permission.AuthenticatorFactory;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration("DiscussionContextConfig")
class ContextConfig {

	private final DiscussionDAO dao;
	private final VoteDAO voteDAO;

	private final MessageClient messageClient;

	@PostConstruct
	void setUp() {
		Discussion.messageClient = messageClient;
		Discussion.dao = dao;
		Discussion.voteDAO = voteDAO;
	}

	@Bean("DiscussionAuthenticator")
	Authenticator authenticator(AuthenticatorFactory factory) {
		return factory.create("DISCUSSION");
	}
}
