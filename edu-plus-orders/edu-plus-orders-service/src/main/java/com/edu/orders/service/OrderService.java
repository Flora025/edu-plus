package com.edu.orders.service;

import com.edu.orders.model.dto.AddOrderDto;
import com.edu.orders.model.dto.PayRecordDto;

/**
 * 订单相关的service接口
 */
public interface OrderService {
    /**
     * 创建商品订单
     * @param userId 订单信息
     * @param addOrderDto 支付交易记录
     * @return
     */
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);
}
