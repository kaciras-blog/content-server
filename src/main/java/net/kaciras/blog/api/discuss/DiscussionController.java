package net.kaciras.blog.api.discuss;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.SecurtyContext;
import net.kaciras.blog.api.user.UserService;
import net.kaciras.blog.infrastructure.exception.ResourceStateException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/discussions")
final class DiscussionController {

	private final DiscussionService discussionService;
	private final UserService userService;
	private final DiscussMapper mapper;

	@GetMapping
	public Map<String, ?> getList(DiscussionQuery query, Pageable pageable) {
		query.setPageable(pageable);

		var size = discussionService.count(query);
		if (query.isMetaonly()) {
			return Map.of("total", size);
		}
		var list = discussionService.getList(query);
		var result = new ArrayList<DiscussionVo>(list.size());

		for (var discuz : list) {
			var vo = convert(discuz);
			vo.setReplyCount(discuz.getReplyList().size());
			vo.setReplies(convert(discuz.getReplyList().select(0, 5)));

			var user = SecurtyContext.getUserId();
			vo.setVoted(user > 0 && discuz.getVoterList().contains(user));
			result.add(vo);
		}
		return Map.of("total", size, "items", result);
	}

	private List<DiscussionVo> convert(List<Discussion> discussion) {
		var res = new ArrayList<DiscussionVo>(discussion.size());
		for (var d : discussion) {
			res.add(convert(d));
		}
		return res;
	}

	private DiscussionVo convert(Discussion discussion) {
		var vo = mapper.toView(discussion);
		vo.setUser(userService.getUser(discussion.getUserId()));
		return vo;
	}

	@PostMapping
	public ResponseEntity post(@RequestBody AddDiscussionRequest request) {
		var id = discussionService.add(request.getObjectId(), request.getType(), request.getContent());
		return ResponseEntity.created(URI.create("/discussions/" + id)).build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		discussionService.delete(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 查询指定评论的回复（楼中楼）。
	 *
	 * 楼中楼的URL主要使用子资源的形式，虽然getList()方法通过设置请求参数
	 * 也能做到相同的功能，但使用子资源更加语义化。
	 *
	 * @param id 评论ID
	 * @param pageable 分页参数
	 * @return 回复列表
	 */
	@GetMapping("/{id}/replies")
	public List<DiscussionVo> getReplies(@PathVariable long id, Pageable pageable) {
		var query = DiscussionQuery.byParent(id);
		query.setPageable(pageable);
		return convert(discussionService.getList(query));
	}

	@PostMapping("/{id}/replies")
	public ResponseEntity<Void> addReply(@PathVariable long id, @RequestBody String content) {
		var newId = discussionService.addReply(id, content);
		return ResponseEntity.created(URI.create("/discussions/" + newId)).build();
	}

	/**
	 * 点赞功能，每个用户对每个评论只能点赞一次，若重复点赞则返回409.
	 *
	 * @param id 要点赞的评论ID
	 * @return 响应
	 */
	@PostMapping("/{id}/votes")
	public ResponseEntity<Void> postVote(@PathVariable int id) {
		SecurtyContext.requireLogin();
		discussionService.voteUp(id);
		return ResponseEntity.created(URI.create("discussions/" + id + "/votes")).build();
	}

	@DeleteMapping("/{id}/votes")
	public ResponseEntity<Void> revokeVote(@PathVariable int id) {
		SecurtyContext.requireLogin();
		discussionService.revokeVote(id);
		return ResponseEntity.noContent().build();
	}

	@ExceptionHandler(ResourceStateException.class)
	public ResponseEntity<Void> handleException(ResourceStateException ex) {
		return ResponseEntity.status(409).build();
	}
}
