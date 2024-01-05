package com.edu.orders.service;

import com.edu.orders.model.dto.AddOrderDto;
import com.edu.orders.model.dto.PayRecordDto;
import com.edu.orders.model.dto.PayStatusDto;
import com.edu.orders.model.po.XcPayRecord;

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

    /**
     * 查询支付交易记录
     * @param payNo 交易记录号
     * @return
     */
    public XcPayRecord getPayRecordByPayno(String payNo);

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    public PayRecordDto queryPayResult(String payNo);

    /**
     * 保存支付宝支付结果
     * @param payStatusDto 支付结果信息
     */
    public void saveAliPayStatus(PayStatusDto payStatusDto);
}
