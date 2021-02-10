# 项目结构

`com.kaciras.blog.api`包含具体 API 功能的代码放在这个包下，比如文章、评论、友链、限流等。

`com.kaciras.blog.infra` 为了使逻辑更紧凑，与具体 API 无关的代码被放在这个包下，相当于基础设施层。`api`包单向引用`infra`包。

## 命名规则

`com.kaciras.blog.api.*`包下的类根据功能来命名：

- *VO：表示控制器返回的数据的 POJO，被序列化为 JSON，因为没有逻辑所以直接使用了`public`字段，省去无聊的 Getter 和 Setter。

- *DTO：表示请求参数或请求体的 POJO，同样直接使用了`public`字段，而且还加上了`final`。

- *Mapper：负责在 POJO 与领域对象之间转换的工具，通常是抽象类或接口，由 [MapStruct](https://github.com/mapstruct/mapstruct) 自动生成实现代码。

- *DAO：直接操作 SQL 数据库的类，通常是 Mybatis 的 Mapper，注意与上面的 Mapper 区分。

- *Controller、*Service、*Repository：这仨兄弟不用解释都知道。

名字不符合上述后缀的是领域对象或者无法按上述分类的代码。另外为了精简，大部分模块的逻辑直接写在 Controller 里，省去了多余的 Service 层。
