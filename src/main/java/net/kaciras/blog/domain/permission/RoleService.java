package net.kaciras.blog.domain.permission;

import net.kaciras.blog.infrastructure.message.MessageClient;
import net.kaciras.blog.infrastructure.message.event.RoleRemovedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

	private final RoleRepository repository;
	private final MessageClient messageClient;

	@Autowired
	RoleService(RoleRepository repository, MessageClient messageClient) {
		this.repository = repository;
		this.messageClient = messageClient;
	}

	public boolean accept(Integer userId, PermissionKey pk) {
		return repository.findAll(userId).stream().anyMatch(role -> role.accept(pk));
	}

	public void deleteRole(int id) {
		repository.remove(id);
		RoleRemovedEvent event = new RoleRemovedEvent();
		event.setRoleId(id);
		messageClient.send(event).blockingGet();
	}

	public void addRoleToUser(int roleId, int userId) {

	}

	public void deleteRoleFromUser(int roleId, int userId) {

	}


}
