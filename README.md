# MyBatis Plus 代码生成器

这是一个基于MyBatis Plus的代码生成器工具，支持通过配置文件进行灵活配置，并提供REST API接口。

## 功能特性

- 🚀 基于MyBatis Plus 3.5.6版本
- 📝 独立的YAML配置文件
- 🎯 支持单表、多表、全表代码生成
- 🌐 REST API接口
- ⚙️ 灵活的配置选项
- 📦 自动生成Entity、Mapper、Service、Controller等代码

## 项目结构

```
src/main/java/com/example/demothree/
├── config/
│   └── YamlPropertySourceFactory.java    # YAML配置源工厂
├── mybatisUtils/
│   ├── CodeGenerator.java                 # 代码生成器核心类
│   ├── GeneratorController.java           # REST API控制器
│   └── GeneratorProperties.java          # 配置属性类
└── DemoThreeApplication.java             # 主应用类

src/main/resources/
└── generator-config.yml                   # 代码生成器配置文件
```

## 配置文件说明

### generator-config.yml

```yaml
generator:
  # 全局配置
  global:
    author: GoryLee                        # 作者
    output-dir: D:\workCode\demo-three\src\main\java  # 输出目录
    open: false                           # 是否打开输出目录
    enable-swagger: false                 # 是否启用Swagger注解
    date-format: yyyy-MM-dd HH:mm:ss     # 日期格式

  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver

  # 包配置
  package:
    parent: com.example.demothree         # 父包名
    module-name: "customer"               # 模块名
    entity: entity                        # 实体类包名
    mapper: mapper                        # Mapper包名
    service: service                      # Service包名
    service-impl: service.impl            # Service实现类包名
    controller: controller                # Controller包名
    xml: mapper                           # XML文件包名
    pathInfo: D:\workCode\demo-three\src\main\resources  # 资源文件路径

  # 策略配置
  strategy:
    naming: underline_to_camel           # 数据库表映射到实体的命名策略
    column-naming: underline_to_camel    # 数据库表字段映射到实体的命名策略
    entity-lombok: true                  # 实体类是否使用Lombok注解
    entity-chain-model: true             # 实体类是否使用链式模型
    rest-controller-style: true          # 控制器是否使用REST风格
    controller-hyphen-style: true        # 控制器是否使用连字符风格
    logic-delete-field: deleted          # 逻辑删除字段名
    version-field: version               # 乐观锁字段名
    table-prefix:                        # 表前缀
      - crm_
    include:                             # 包含的表名
      - crm_quota
    exclude:                             # 排除的表名
      - test_
      - temp_

  # 模板配置
  template:
    entity: templates/entity.java.vm
    mapper: templates/mapper.java.vm
    service: templates/service.java.vm
    service-impl: templates/serviceImpl.java.vm
    controller: templates/controller.java.vm
    xml: templates/mapper.xml.vm
```

## API接口

### 1. 生成指定表的代码

```http
POST /api/generator/generate/{tableName}
```

**示例：**
```bash
curl -X POST http://localhost:8080/api/generator/generate/user
```

### 2. 批量生成多个表的代码

```http
POST /api/generator/generate/batch
Content-Type: application/json

{
  "tableNames": ["user", "order", "product"]
}
```

### 3. 生成所有表的代码

```http
POST /api/generator/generate/all
```

### 4. 获取生成器配置信息

```http
GET /api/generator/config
```

## 使用示例

### 1. 编程方式使用

```java
@Autowired
private CodeGenerator codeGenerator;

// 生成单个表的代码
codeGenerator.generateByTableName("user");

// 生成多个表的代码
List<String> tableNames = Arrays.asList("user", "order", "product");
codeGenerator.generateByTableNames(tableNames);

// 生成所有表的代码
codeGenerator.generateAllTables();
```

### 2. REST API方式使用

```bash
# 生成用户表代码
curl -X POST http://localhost:8080/api/generator/generate/user

# 批量生成代码
curl -X POST http://localhost:8080/api/generator/generate/batch \
  -H "Content-Type: application/json" \
  -d '{"tableNames": ["user", "order", "product"]}'

# 生成所有表代码
curl -X POST http://localhost:8080/api/generator/generate/all
```

## 生成的文件结构

```
src/main/java/com/example/demothree/
├── entity/
│   └── User.java                    # 实体类
├── mapper/
│   └── UserMapper.java             # Mapper接口
├── service/
│   ├── UserService.java            # Service接口
│   └── impl/
│       └── UserServiceImpl.java    # Service实现类
└── controller/
    └── UserController.java          # Controller类

src/main/resources/mapper/
└── UserMapper.xml                   # MyBatis XML映射文件
```

## 配置说明

### 命名策略

- `underline_to_camel`: 下划线转驼峰命名
- `no_change`: 保持原样

### 表前缀和包含/排除

- `table-prefix`: 指定表前缀，生成时会自动去除
- `include`: 只生成指定的表
- `exclude`: 排除指定的表

### 实体类特性

- `entity-lombok`: 启用Lombok注解
- `entity-chain-model`: 启用链式模型
- `logic-delete-field`: 逻辑删除字段
- `version-field`: 乐观锁字段

## 注意事项

1. 确保数据库连接配置正确
2. 输出目录需要有写入权限
3. 表名和字段名建议使用下划线命名
4. 生成的代码会覆盖已存在的文件
5. 建议在开发环境使用，生产环境谨慎使用

## 依赖版本

- Spring Boot: 3.1.12
- MyBatis Plus: 3.5.6
- Java: 17
- Maven: 3.6+

## 许可证

MIT License
