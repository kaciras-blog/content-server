# Kaciras Blog content server

Kaciras博客的内容服务器，管理用户、文章、评论、分类等数据。

# 启动

本服务需要Mariadb和Redis数据库，请先修改[配置文件](https://github.com/kaciras-blog/content-server/blob/master/application-EXAMPLE.yml) ，填写数据库连接信息。

```shell script
mvn spring-boot:run
```

初始管理员用户名`admin`，密码`12345678`。
