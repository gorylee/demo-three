package com.example.demothree.mybatisUtils;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CodeGeneratorService {

    @Autowired
    private GeneratorProperties properties;

    /**
     * 生成指定表的代码
     *
     * @param tableNames 表名数组
     */
    public void generateCode(String... tableNames) {
        if (tableNames == null || tableNames.length == 0) {
            throw new RuntimeException("请指定要生成的表名");
        }

        generateCodeInternal(tableNames);
    }

    /**
     * 根据配置文件生成代码
     * 优先使用配置文件中指定的表，如果没有配置则自动扫描数据库所有表
     */
    public void generateCodeFromConfig() {
        List<String> tablesToGenerate = new ArrayList<>();

        // 1. 首先检查配置文件中是否指定了要生成的表
        if (properties.getStrategyConfig().getInclude() != null &&
                !properties.getStrategyConfig().getInclude().isEmpty()) {
            tablesToGenerate.addAll(properties.getStrategyConfig().getInclude());
        }
        // 2. 如果配置了自动扫描且配置文件中没有指定表，则扫描所有表
        else if (Boolean.TRUE.equals(properties.getStrategyConfig().getAutoScanTables())) {
            tablesToGenerate.addAll(getAllTablesFromDatabase());
        }
        // 3. 如果都没有配置，抛出异常
        else {
            throw new RuntimeException("请在配置文件中指定要生成的表或启用自动扫描");
        }

        // 排除不需要的表
        if (properties.getStrategyConfig().getExclude() != null) {
            tablesToGenerate.removeAll(properties.getStrategyConfig().getExclude());
        }

        if (tablesToGenerate.isEmpty()) {
            throw new RuntimeException("没有找到要生成的表");
        }

        generateCodeInternal(tablesToGenerate.toArray(new String[0]));
    }

    /**
     * 从数据库获取所有表名
     */
    private List<String> getAllTablesFromDatabase() {
        List<String> tables = new ArrayList<>();
        String url = properties.getDataSource().getUrl();
        String username = properties.getDataSource().getUsername();
        String password = properties.getDataSource().getPassword();

        // 从JDBC URL中提取数据库名
        String databaseName = extractDatabaseNameFromUrl(url);

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet resultSet = metaData.getTables(databaseName, null, "%", new String[]{"TABLE"});

            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                tables.add(tableName);
            }
        } catch (SQLException e) {
            throw new RuntimeException("获取数据库表失败: " + e.getMessage(), e);
        }

        return tables;
    }

    /**
     * 从JDBC URL中提取数据库名
     */
    private String extractDatabaseNameFromUrl(String url) {
        // 简单的解析逻辑，实际可能需要更复杂的处理
        int start = url.lastIndexOf("/") + 1;
        int end = url.indexOf("?");
        if (end == -1) {
            end = url.length();
        }
        return url.substring(start, end);
    }

    /**
     * 内部代码生成方法
     */
    private void generateCodeInternal(String[] tableNames) {
        // 包配置
        Map<OutputFile, String> pathInfo = new HashMap<>();
        String outputDir = properties.getGlobalConfig().getOutputDir();
        String xmlPath = properties.getPackageConfig().getPathInfo() + "/"  + properties.getPackageConfig().getXml();
        pathInfo.put(OutputFile.xml, xmlPath);

        // 使用 FastAutoGenerator
        FastAutoGenerator.create(properties.getDataSource().getUrl(),
                        properties.getDataSource().getUsername(),
                        properties.getDataSource().getPassword())

                // 全局配置
                .globalConfig(builder -> {
                    builder.author(properties.getGlobalConfig().getAuthor()) // 设置作者
                            .outputDir(outputDir) // 指定输出目录
                            .disableOpenDir() // 禁止打开输出目录
                            .commentDate(properties.getGlobalConfig().getDateFormat()); // 注释日期

                    if (Boolean.TRUE.equals(properties.getGlobalConfig().getEnableSwagger())) { // 开启swagger
                        builder.enableSwagger();
                    }
                    if (Boolean.TRUE.equals(properties.getGlobalConfig().getKotlin())) {
                        builder.enableKotlin();
                    }
                })
                // 包配置
                .packageConfig(builder -> {
                    builder.parent(properties.getPackageConfig().getParent()) // 父包名
                            .moduleName(properties.getPackageConfig().getModuleName()) // 父包模块名
                            .entity(properties.getPackageConfig().getEntity()) // Entity包名
                            .service(properties.getPackageConfig().getService()) // Service包名
                            .serviceImpl(properties.getPackageConfig().getServiceImpl()) // Service Impl包名
                            .mapper(properties.getPackageConfig().getMapper()) // Mapper包名
                            .controller(properties.getPackageConfig().getController()) // Controller包名
                            .xml(properties.getPackageConfig().getXml()) // mapper xml文件包名
                            .pathInfo(pathInfo); // 设置mapperXml生成路径
                })
                // 策略配置
                .strategyConfig(builder -> {
                    // 设置需要生成的表名
                    builder.addInclude(tableNames);

                    // 设置过滤表前缀
                    if (properties.getStrategyConfig().getTablePrefix() != null && !properties.getStrategyConfig().getTablePrefix().isEmpty()) {
                        builder.addTablePrefix(properties.getStrategyConfig().getTablePrefix().toArray(new String[0]));
                    }

                    // 设置排除表
                    if (properties.getStrategyConfig().getExclude() != null && !properties.getStrategyConfig().getExclude().isEmpty()) {
                        builder.addExclude(properties.getStrategyConfig().getExclude());
                    }

                    // Entity 策略配置
                    builder.entityBuilder()
                            .enableTableFieldAnnotation() // 开启字段注解
                            .naming(NamingStrategy.underline_to_camel) // 数据库表映射到实体的命名策略
                            .columnNaming(NamingStrategy.underline_to_camel); // 数据库表字段映射到实体的命名策略

                    if (properties.getStrategyConfig().getEntityLombok()) {
                        builder.entityBuilder().enableLombok();
                    }

                    if (properties.getStrategyConfig().getEntityChainModel()) {
                        builder.entityBuilder().enableChainModel();
                    }

                    builder.entityBuilder()
                            .addTableFills(
                                    new Column("create_time", FieldFill.INSERT),
                                    new Column("modify_time", FieldFill.INSERT_UPDATE)
                            ); // 添加表字段填充

                    // Controller 策略配置
                    builder.controllerBuilder();
                    if (Boolean.TRUE.equals(properties.getStrategyConfig().getRestControllerStyle())) {
                        builder.controllerBuilder().enableRestStyle(); // 开启生成@RestController控制器
                    }
                    if (Boolean.TRUE.equals(properties.getStrategyConfig().getControllerMappingHyphenStyle())) {
                        builder.controllerBuilder().enableHyphenStyle();
                    }

                    // Service 策略配置
                    builder.serviceBuilder()
                            .formatServiceFileName("%sService") // 服务接口文件名称
                            .formatServiceImplFileName("%sServiceImpl"); // 服务实现类文件名称

                    // Mapper 策略配置
                    builder.mapperBuilder()
                            .formatMapperFileName("%sMapper")
                            .formatXmlFileName("%sMapper");

                    builder.mapperBuilder()
                            .enableMapperAnnotation() // 开启 @Mapper 注解
                            .enableBaseResultMap() // 启用 BaseResultMap 生成
                            .enableBaseColumnList(); // 启用 BaseColumnList

                })
                .templateEngine(new VelocityTemplateEngine()) // 使用Velocity引擎模板
                .execute();
    }
}