# Kaciras Blog content server

[![Build Status](https://travis-ci.com/kaciras-blog/content-server.svg?branch=master)](https://travis-ci.com/kaciras-blog/content-server)
[![codecov](https://codecov.io/gh/kaciras-blog/content-server/branch/master/graph/badge.svg?token=TRY6P2X75U)](https://codecov.io/gh/kaciras-blog/content-server)

Kaciras 博客的内容服务器，管理用户、文章、评论、分类等数据。

# 启动

本服务需要 Mariadb 和 Redis 数据库，请先修改 [配置文件](https://github.com/kaciras-blog/content-server/blob/master/application-EXAMPLE.yml) ，填写数据库连接信息，然后使用以下命令运行：

```shell script
mvn spring-boot:run
```

初始管理员用户名`admin`，密码`12345678`。

运行测试：

```shell
mvn test
```

# 打包和运行

```shell
mvn -Dmaven.test.skip=true package
java -javaagent:target/spring-instrument-5.3.3.jar -jar target/content-server-$VERSION.jar
```

## Microbenchmark

部分方法有相关的微性能测试（Microbenchmark），位于`src/test/perf`目录下，这些测试可以被打包：

```shell
mvn -Pbenchmarks package
java -jar target/content-server-$VERSION-benchmarks.jar
```
