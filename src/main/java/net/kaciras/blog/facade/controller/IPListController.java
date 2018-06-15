package net.kaciras.blog.facade.controller;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.domain.defense.IPFilterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/defense/iplist")
public class IPListController {

	private final IPFilterService ipFilterService;

	@PostMapping("/")
	public ResponseEntity addRange(@RequestParam String start, @RequestParam String end) {
		ipFilterService.addRange(start, end);
		return ResponseEntity.ok().build();
	}
}
