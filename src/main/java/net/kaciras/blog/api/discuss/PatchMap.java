package net.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Value
final class PatchMap {

	private final Boolean deletion;
}
