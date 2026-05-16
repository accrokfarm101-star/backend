package com.freshmart.serviceImpl;

import com.freshmart.exception.ResourceNotFoundException;
import com.freshmart.model.dto.request.OrderItemRequest;
import com.freshmart.model.dto.request.OrderRequest;
import com.freshmart.model.dto.response.OrderItemResponse;
import com.freshmart.model.dto.response.OrderResponse;
import com.freshmart.model.entity.Order;
import com.freshmart.model.entity.OrderItem;
import com.freshmart.model.entity.Product;
import com.freshmart.model.entity.User;
import com.freshmart.model.enums.OrderStatus;
import com.freshmart.repository.OrderRepository;
import com.freshmart.repository.ProductRepository;
import com.freshmart.repository.UserRepository;
import com.freshmart.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest, Long userId) {
        // Lấy thông tin user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));

        // Tạo order mới
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .paymentMethod(orderRequest.getPaymentMethod())
                .shippingAddress(orderRequest.getShippingAddress())
                .phoneNumber(orderRequest.getPhoneNumber())
                .notes(orderRequest.getNotes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Xử lý các item trong đơn hàng
        if (orderRequest.getOrderItems() != null && !orderRequest.getOrderItems().isEmpty()) {
            for (OrderItemRequest itemRequest : orderRequest.getOrderItems()) {
                // Lấy sản phẩm từ database
                Product product = productRepository.findById(itemRequest.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

                // Kiểm tra số lượng tồn kho
                if (product.getStock() < itemRequest.getQuantity()) {
                    throw new IllegalArgumentException("Sản phẩm " + product.getName() +
                            " không đủ số lượng. Hiện có: " + product.getStock());
                }

                // Tạo order item
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(itemRequest.getQuantity())
                        .unitPrice(product.getPrice())
                        .build();

                orderItem.calculateTotalPrice();
                orderItems.add(orderItem);

                // Cập nhật tổng tiền
                totalAmount = totalAmount.add(orderItem.getTotalPrice());

                // Giảm số lượng tồn kho
                product.setStock(product.getStock() - itemRequest.getQuantity());
                productRepository.save(product);
            }
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        // Lưu order vào database
        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", orderId));
        return mapToResponse(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        // Kiểm tra user tồn tại
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", orderId));

        // Kiểm tra nếu đơn hàng đã bị hủy
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Không thể cập nhật trạng thái của đơn hàng đã bị hủy");
        }

        order.setStatus(status);
        Order updated = orderRepository.save(order);
        return mapToResponse(updated);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", orderId));

        // Chỉ có thể hủy đơn hàng đang ở trạng thái PENDING hoặc CONFIRMED
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng ở trạng thái chờ xác nhận hoặc đã xác nhận");
        }

        // Hoàn lại số lượng sản phẩm
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        Order updated = orderRepository.save(order);
        return mapToResponse(updated);
    }

    @Override
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", orderId));
        orderRepository.delete(order);
    }

    @Override
    public List<OrderResponse> getOrdersByUserIdAndStatus(Long userId, OrderStatus status) {
        // Kiểm tra user tồn tại
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", userId));

        List<Order> orders = orderRepository.findByUserIdAndStatus(userId, status);
        return orders.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Long getOrderCountByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Override
    public Long getOrderCountByUserId(Long userId) {
        return orderRepository.countByUserId(userId);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> orderItemResponses = new ArrayList<>();
        if (order.getOrderItems() != null) {
            orderItemResponses = order.getOrderItems().stream()
                    .map(item -> OrderItemResponse.builder()
                            .id(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getTotalPrice())
                            .build())
                    .collect(Collectors.toList());
        }

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .phoneNumber(order.getPhoneNumber())
                .notes(order.getNotes())
                .orderItems(orderItemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .cancelledAt(order.getCancelledAt())
                .build();
    }
}
