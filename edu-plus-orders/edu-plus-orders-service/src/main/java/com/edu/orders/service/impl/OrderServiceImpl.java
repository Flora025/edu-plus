package com.edu.orders.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.base.exception.EduPlusException;
import com.edu.base.utils.IdWorkerUtils;
import com.edu.base.utils.QRCodeUtil;
import com.edu.orders.mapper.XcOrdersGoodsMapper;
import com.edu.orders.mapper.XcOrdersMapper;
import com.edu.orders.mapper.XcPayRecordMapper;
import com.edu.orders.model.dto.AddOrderDto;
import com.edu.orders.model.dto.PayRecordDto;
import com.edu.orders.model.po.XcOrders;
import com.edu.orders.model.po.XcOrdersGoods;
import com.edu.orders.model.po.XcPayRecord;
import com.edu.orders.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Value("${pay.qrcodeurl}")
    String qrcodeurl;

    @Autowired
    XcOrdersMapper ordersMapper;

    @Autowired
    XcOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    XcPayRecordMapper payRecordMapper;

    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {
        // 保存商品订单
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);

        // 添加支付交易记录
        XcPayRecord payRecord = createPayRecord(xcOrders);

        // 生成二维码
        String qrCode = null;
        try {
            // url要可以被模拟器访问到 url为下单接口(稍后定义)
            String url = String.format(qrcodeurl, payRecord.getPayNo());
            qrCode = new QRCodeUtil().createQRCode(url, 200, 200);
        } catch (IOException e) {
            EduPlusException.cast("生成二维码出错");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord,payRecordDto);
        payRecordDto.setQrcode(qrCode);

        return payRecordDto;

    }

    @Transactional
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) {
        // 幂等性处理
        XcOrders order = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if (order != null) {
            return order;
        }
        order = new XcOrders();
        // 生成订单号
        long orderId = IdWorkerUtils.getInstance().nextId();

        order.setId(orderId);
        order.setTotalPrice(addOrderDto.getTotalPrice());
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("600001");//未支付
        order.setUserId(userId);
        order.setOrderType(addOrderDto.getOrderType());
        order.setOrderName(addOrderDto.getOrderName());
        order.setOrderDetail(addOrderDto.getOrderDetail());
        order.setOrderDescrip(addOrderDto.getOrderDescrip());
        order.setOutBusinessId(addOrderDto.getOutBusinessId());//选课记录id
        ordersMapper.insert(order); // 插入订单表

        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        xcOrdersGoodsList.forEach(goods -> {
            XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
            BeanUtils.copyProperties(goods, xcOrdersGoods);
            xcOrdersGoods.setOrderId(orderId);// 设定当前商品的订单号（统一的）
            ordersGoodsMapper.insert(xcOrdersGoods); // 订单明细表中插入该商品
        });

        return order;
    }

    //根据业务id查询订单
    public XcOrders getOrderByBusinessId(String businessId) {
        XcOrders orders = ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
        return orders;
    }

    public XcPayRecord createPayRecord(XcOrders orders){
        // validation
        if (orders == null) {
            EduPlusException.cast("订单不存在");
        }
        if (orders.getStatus().equals("600002")) {
            EduPlusException.cast("订单已支付");
        }

        XcPayRecord payRecord = new XcPayRecord();
        //生成支付交易流水号
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(orders.getId());// 商品订单号
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001");// 未支付
        payRecord.setUserId(orders.getUserId());

        payRecordMapper.insert(payRecord);
        return payRecord;
    }


}
