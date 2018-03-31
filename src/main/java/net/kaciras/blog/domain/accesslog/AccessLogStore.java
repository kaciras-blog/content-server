package net.kaciras.blog.domain.accesslog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AccessLogStore {

	private final AccessLogDAO accessLogDAO;

	@Autowired
	public AccessLogStore(AccessLogDAO accessLogDAO) {
		this.accessLogDAO = accessLogDAO;
	}

	public void add(AccessRecord record) {
		accessLogDAO.insert(record);
	}
}
