package com.example.demothree.customer.controller;

import com.example.demothree.customer.entity.Quota;
import com.example.demothree.customer.service.QuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 客户额度表 前端控制器
 * </p>
 *
 * @author GoryLee
 * @since 2025-09-12 15:02:07
 */
@RestController
@RequestMapping("/customer/quota")
public class QuotaController {

    @Autowired
    private QuotaService quotaService;

    @RequestMapping("/list")
    public List<Quota> list() {
        return quotaService.list();
    }
}
