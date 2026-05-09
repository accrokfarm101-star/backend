package com.greenfood.service;

import com.greenfood.model.dto.request.OrderRequest;
import com.greenfood.model.dto.response.OrderResponse;
import com.greenfood.model.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest, Long userId);

    OrderResponse getOrderById(Long orderId);

    List<OrderResponse> getOrdersByUserId(Long userId);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);

    OrderResponse cancelOrder(Long orderId);

    void deleteOrder(Long orderId);

    List<OrderResponse> getOrdersByUserIdAndStatus(Long userId, OrderStatus status);

    Long getOrderCountByStatus(OrderStatus status);

    Long getOrderCountByUserId(Long userId);
}
