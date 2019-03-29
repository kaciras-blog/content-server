package net.kaciras.blog.api.graphql;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
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

	@Autowired
	public GraphQLController(ArticleDataFetcher articleDataFetcher,
							 CategoryDataFetcher categoryDataFetcher,
							 UserDataFetcher userDataFetcher) throws IOException {

		var stream = GraphQLController.class.getClassLoader().getResourceAsStream("schema.graphql");
		TypeDefinitionRegistry registry;
		try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			registry = new SchemaParser().parse(reader);
		}

		var wiring = RuntimeWiring.newRuntimeWiring()
				.type("Query", builder -> builder
						.dataFetcher("category", categoryDataFetcher::getById)
						.dataFetcher("article", articleDataFetcher::getById))
				.type("Category", builder -> builder
						.dataFetcher("banner", categoryDataFetcher::getBanner))
				.type("Mutation", builder -> builder
						.dataFetcher("updateUserHead", userDataFetcher::updateHead))
				.build();

		var schemaGenerator = new SchemaGenerator();
		graphQLSchema = schemaGenerator.makeExecutableSchema(registry, wiring);
	}

	@GetMapping
	public Object query(@RequestParam String query) {
		var build = GraphQL.newGraphQL(graphQLSchema).build();
		var executionResult = build.execute(query);
		var errors = executionResult.getErrors();

		if (errors.isEmpty()) {
			return executionResult.getData();
		} else {
			return errors.get(0);
		}
	}
}
