package net.kaciras.blog.api.graphql;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/graphql")
public class GraphQLController {

	private final GraphQLSchema graphQLSchema;

	public GraphQLController(ArticleDataFetcher articleDataFetcher,
							 CategoryDataFetcher categoryDataFetcher,
							 UserDataFetcher userDataFetcher) throws IOException {

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
		graphQLSchema = schemaGenerator.makeExecutableSchema(loadTypeDefinition(), wiring);
	}

	private TypeDefinitionRegistry loadTypeDefinition() throws IOException {
		var schema = GraphQLController.class.getClassLoader().getResource("schema.graphql");
		if (schema == null) {
			throw new FileNotFoundException("Can't find GraphQL schema file");
		}
		try (var reader = new InputStreamReader(schema.openStream(), StandardCharsets.UTF_8)) {
			return new SchemaParser().parse(reader);
		}
	}

	@GetMapping
	public Object handleRequest(@RequestBody String query) {
		var executionResult = GraphQL
				.newGraphQL(graphQLSchema)
				.build()
				.execute(query);

		var errors = executionResult.getErrors();
		if (!errors.isEmpty()) {
			return errors.get(0);
		}
		return executionResult.getData();
	}
}
