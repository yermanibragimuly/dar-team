package com.example.demo.service.shop;

import com.example.demo.entity.shop.Order;
import com.example.demo.repository.shop.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderScheduleService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    // Фоновая проверка каждые 10 минут
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void cancelExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> expiredOrders = orderRepository.findAllByOrderStatusAndReservedUntilBefore("new", now);

        for (Order order : expiredOrders) {
            if ("pending".equalsIgnoreCase(order.getPaymentStatus())) {
                log.info("Автоматическое снятие брони (24 часа истекли) для заказа: {}", order.getOrderNumber());
                try {
                    orderService.cancelOrder(order.getId());
                } catch (Exception e) {
                    log.error("Ошибка автоотмены заказа #{}: {}", order.getId(), e.getMessage());
                }
            }
        }
    }
}