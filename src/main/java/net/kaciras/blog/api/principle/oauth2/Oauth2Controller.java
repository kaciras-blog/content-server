package net.kaciras.blog.api.principle.oauth2;

import lombok.RequiredArgsConstructor;
import net.kaciras.blog.api.Utils;
import net.kaciras.blog.api.principle.AuthType;
import net.kaciras.blog.api.principle.SessionService;
import net.kaciras.blog.api.principle.oauth2.Oauth2Client.UserInfo;
import net.kaciras.blog.api.user.UserManager;
import net.kaciras.blog.infrastructure.func.Lambdas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/connect")
public class Oauth2Controller {

	private final SessionService sessionService;
	private final OauthDAO oauthDAO;
	private final UserManager userManager;

	private Map<String, Oauth2Client> clientMap;

	@Autowired
	void initClientMap(Collection<Oauth2Client> beans) {
		clientMap = beans.stream()
				.collect(Collectors.toMap(b -> b.authType().name().toLowerCase(), Lambdas.keepIntact()));
	}

	@GetMapping("/{type}")
	public ResponseEntity<Void> redirect(@PathVariable String type, HttpServletRequest request) {
		var client = clientMap.get(type);
		if (client == null) {
			return ResponseEntity.badRequest().build();
		}

		var redirect = UriComponentsBuilder.fromUriString(request.getRequestURL().toString())
				.replaceQuery(null)
				.path("/callback")
				.queryParam("ret", request.getParameter("ret"));

		var authUri = client.authUri()
				.queryParam("redirect_uri", redirect.toUriString())
				.build().toUri();

		return ResponseEntity.status(302).location(authUri).build();
	}

	@GetMapping("/{type}/callback")
	public ResponseEntity<Void> callback(@PathVariable String type, HttpServletRequest request, HttpServletResponse response) throws Exception {
		var client = clientMap.get(type);
		if (client == null) {
			return ResponseEntity.badRequest().build();
		}
		var info = client.getUserInfo(request.getParameter("code"), request.getParameter("state"));
		var localId = getLocalId(info, request, client.authType());
		sessionService.putUser(request, response, localId, true);

		// 返回跳转必须验证域名，防止跳转到其他网站
		var retParam = Optional
				.ofNullable(request.getParameter("ret"))
				.orElse("/");
		var returnUri = UriComponentsBuilder.fromUriString(retParam);
		returnUri.scheme("https").host("localhost");
		return ResponseEntity.status(302).location(returnUri.build().toUri()).build();
	}

	@Transactional
	protected int getLocalId(UserInfo profile, HttpServletRequest request, AuthType authType) {
		var localId = oauthDAO.select(profile.id(), authType);

		if (localId != null) {
			return localId;
		}
		var regIP = Utils.AddressFromRequest(request);
		var newId = userManager.createNew(profile.name(), authType, regIP);
		oauthDAO.insert(profile.id(), authType, newId);

		return newId;
	}
}
