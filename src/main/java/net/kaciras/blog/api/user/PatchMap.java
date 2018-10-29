package net.kaciras.blog.api.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kaciras.blog.infrastructure.codec.ImageRefrence;

@RequiredArgsConstructor(onConstructor_ = @JsonCreator)
@Getter
final class PatchMap {

	private final ImageRefrence head;
}
