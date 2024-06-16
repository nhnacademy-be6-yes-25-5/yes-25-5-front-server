package com.nhnacademy.frontserver1.application.service.impl;

import com.nhnacademy.frontserver1.application.service.OrderService;
import com.nhnacademy.frontserver1.infrastructure.adaptor.OrderAdaptor;
import com.nhnacademy.frontserver1.presentation.dto.request.order.request.CreateOrderRequest;
import com.nhnacademy.frontserver1.presentation.dto.response.order.CreateOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderAdaptor orderAdaptor;

    @Override
    public CreateOrderResponse createPreOrder(CreateOrderRequest request, Long userId) {
        return orderAdaptor.createPreOrder(request, userId);
    }
}
