package net.kaciras.blog.domain.permission;

import net.kaciras.blog.domain.Utils;
import net.kaciras.blog.infrastructure.exception.ResourceNotFoundException;
import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.message.event.*;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static net.kaciras.blog.domain.permission.Role.*;

@Configuration("PermissionContextConfig")
public class PermissionConfig {

	private final RoleRepository repository;
	private final MessageClient messageClient;

	private final RolePermissionDAO rolePermissionDAO;
	private final UserRoleDAO userRoleDAO;

	public PermissionConfig(RoleRepository repository,
							MessageClient messageClient,
							RolePermissionDAO rolePermissionDAO,
							UserRoleDAO userRoleDAO) {
		this.repository = repository;
		this.messageClient = messageClient;
		this.rolePermissionDAO = rolePermissionDAO;
		this.userRoleDAO = userRoleDAO;
	}

	/**
	 * 检查默认的3个角色是否存在，以及内置管理员角色无需显示定义权限。
	 */
	@PostConstruct
	void checkInternalRoles() {
		Role.messageClient = messageClient;

		createIfAbsent(ANYONE_ROLE_ID, "任何访问者");
		createIfAbsent(DEFAULT_USER_ROLE_ID, "登录用户");
		createIfAbsent(ADMIN_ROLE_ID, "内置管理员");
		repository.removeAllFromRole(ADMIN_ROLE_ID);


		messageClient.subscribe(RolePermissionAddedEvent.class,
				e -> repository.addPermissionToRole(e.getRoleId(), e.getGroup(), e.getName()));

		messageClient.subscribe(RolePermissionRemovedEvent.class,
				e -> repository.deletePermissionFromRole(e.getRoleId(), e.getGroup(), e.getName()));

		messageClient.subscribe(RoleIncludeChangedEvent.class,
				e -> repository.changeIncludes(e.getRoleId(), e.getNewList()));

		messageClient.subscribe(UserRoleAddedEvent.class,
				e -> userRoleDAO.insertUserRole(e.getUserId(), e.getRoleId()));

		messageClient.subscribe(UserRoleDeletedEvent.class,
				e -> Utils.checkEffective(userRoleDAO.deleteUserRole(e.getUserId(), e.getRoleId())));
	}

	private void createIfAbsent(int id, String name) {
		try {
			repository.get(id);
		} catch (ResourceNotFoundException ex) {
			repository.add(new Role(id, name));
		}
	}

	@Bean
	public Cache userPermissionCache() {
		ConcurrentMapCache cache = new ConcurrentMapCache("userPermissionCache", false);
		Role.cache = cache;
		messageClient.subscribe(RoleEvent.class, e -> cache.clear());
		return cache;
	}
}
