package net.kaciras.blog.api;

import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.user.UserRepository;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserDataFetcher {

	private final UserRepository repository;

	public Void updateHead(DataFetchingEnvironment environment) {
		System.out.println((String)environment.getArgument("head"));
		return null;
	}
}
