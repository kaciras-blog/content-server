package net.kaciras.blog.api.discuss;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class PatchMap {

	private final Boolean deletion;
}
