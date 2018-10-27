package net.kaciras.blog.api.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Value
final class PatchMap {

	private final ImageRefrence head;
}
