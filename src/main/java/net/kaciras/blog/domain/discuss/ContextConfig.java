package net.kaciras.blog.domain.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.Authenticator;
import net.kaciras.blog.domain.AuthenticatorFactory;
import net.kaciras.blog.infrastructure.message.MessageClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration("DiscussionContextConfig")
class ContextConfig {

	private final DiscussionDAO dao;
	private final VoteDAO voteDAO;

	private final MessageClient messageClient;


	@Bean("DiscussionAuthenticator")
	Authenticator authenticator(AuthenticatorFactory factory) {
		return factory.create("DISCUSSION");
	}
}
