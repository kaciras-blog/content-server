package net.kaciras.blog.domain.permission;

import lombok.RequiredArgsConstructor;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

@RequiredArgsConstructor
@Repository
public class PermissionRepository {

	private Map<String, Map<String, Permission>> map;

	@PostConstruct
	private void scan() throws IOException, URISyntaxException, DocumentException {
		URI uri = PermissionRepository.class.getClassLoader().getResource("permissions").toURI();
		Path path;
		FileSystem fileSystem = null;

		if ("jar".equals(uri.getScheme())) {
			fileSystem = FileSystems.newFileSystem(uri, Map.of());
			path = fileSystem.getPath("permissions");
		} else {
			path = Paths.get(uri);
		}


		Iterator<Path> iter = Files.walk(path).iterator();
		iter.next(); //跳过文件夹自身
		PermElementHandler handler = new PermElementHandler();

		while (iter.hasNext()) {
			InputStream stream = iter.next().toUri().toURL().openStream();
			try (stream) {
				SAXReader saxReader = new SAXReader();
				saxReader.setEncoding("UTF-8");
				saxReader.setDefaultHandler(handler);
				saxReader.read(stream);
			}
		}
		map = handler.getMap();

		if (fileSystem != null) {
			fileSystem.close();
		}
	}

	public List<Permission> findAll() {
		List<Permission> result = new ArrayList<>();
		map.values().forEach(map -> result.addAll(map.values()));
		return result;
	}

	public boolean contains(PermissionKey pk) {
		Map<String, Permission> subMap = map.get(pk.getModule());
		return subMap != null && subMap.containsKey(pk.getName());
	}
}
