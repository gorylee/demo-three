package com.example.demothree.mybatisUtils;

import com.example.demothree.config.CustomYamlConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "generator")
@PropertySource(value = "classpath:generator-config.yml", factory = CustomYamlConfig.class)
public class GeneratorProperties {

    private GlobalConfig globalConfig;
    private DataSourceConfig dataSource;
    private PackageConfig packageConfig;
    private StrategyConfig strategyConfig;

    @Data
    public static class GlobalConfig {
        private String outputDir;
        private String author;
        private Boolean open;
        private Boolean enableCache;
        private Boolean kotlin;
        private Boolean enableSwagger;
        private String dateFormat;
    }

    @Data
    public static class DataSourceConfig {
        private String url;
        private String driverName;
        private String username;
        private String password;
    }

    @Data
    public static class PackageConfig {
        private String parent;
        private String moduleName;
        private String entity;
        private String service;
        private String serviceImpl;
        private String mapper;
        private String controller;
        private String xml;
        private String pathInfo;
    }

    @Data
    public static class StrategyConfig {
        private String naming;
        private String columnNaming;
        private Boolean entityLombok;
        private Boolean entityChainModel;
        private Boolean restControllerStyle;
        private Boolean controllerMappingHyphenStyle;
        private List<String> tablePrefix;
        private List<String> fieldPrefix;
        private List<String> include;
        private List<String> exclude;
        private Boolean autoScanTables;
    }
}