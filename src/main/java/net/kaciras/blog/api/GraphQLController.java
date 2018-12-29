package net.kaciras.blog.api;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import net.kaciras.blog.api.article.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/graphql")
public class GraphQLController {

	private GraphQLSchema graphQLSchema;

	private final ArticleRepository articleRepository;

	@Autowired
	public GraphQLController(ArticleRepository articleRepository) throws IOException {
		this.articleRepository = articleRepository;

		var schemaParser = new SchemaParser();
		var stream = GraphQLController.class.getClassLoader().getResourceAsStream("schema.graphqls");

		TypeDefinitionRegistry registry;
		try(var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			registry = schemaParser.parse(reader);
		}

		var wiring = RuntimeWiring.newRuntimeWiring()
				.type("Query", builder -> builder.dataFetcher("article",
					env -> articleRepository.get(Integer.parseInt(env.getArgument("id"))))).build();

		var schemaGenerator = new SchemaGenerator();
		graphQLSchema = schemaGenerator.makeExecutableSchema(registry, wiring);
	}

	@GetMapping
	public Object query(@RequestParam String query) {
		var build = GraphQL.newGraphQL(graphQLSchema).build();
		var executionResult = build.execute(query);
		return executionResult.getData();
	}
}
