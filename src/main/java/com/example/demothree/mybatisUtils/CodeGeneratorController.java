package com.example.demothree.mybatisUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/generator")
public class CodeGeneratorController {

    @Autowired
    private CodeGeneratorService codeGeneratorService;

    /**
     * 生成指定表的代码（通过接口参数）
     */
    @PostMapping("/generate")
    public String generateCode(@RequestParam String tables) {
        try {
            String[] tableArray = tables.split(",");
            codeGeneratorService.generateCode(tableArray);
            return "代码生成成功! 表: " + Arrays.toString(tableArray);
        } catch (Exception e) {
            return "代码生成失败: " + e.getMessage();
        }
    }

    /**
     * 根据配置文件生成代码
     */
    @PostMapping("/run")
    public String run() {
        try {
            codeGeneratorService.generateCodeFromConfig();
            return "代码生成成功! 使用配置文件中的表配置";
        } catch (Exception e) {
            return "代码生成失败: " + e.getMessage();
        }
    }

    /**
     * 灵活的代码生成（可选择使用参数或配置）
     */
    @PostMapping("/generate-flexible")
    public String generateFlexible(@RequestParam(required = false) String tables) {
        try {
            if (tables != null && !tables.trim().isEmpty()) {
                // 使用接口参数
                String[] tableArray = tables.split(",");
                codeGeneratorService.generateCode(tableArray);
                return "代码生成成功! 表: " + Arrays.toString(tableArray);
            } else {
                // 使用配置文件
                codeGeneratorService.generateCodeFromConfig();
                return "代码生成成功! 使用配置文件中的表配置";
            }
        } catch (Exception e) {
            return "代码生成失败: " + e.getMessage();
        }
    }
}