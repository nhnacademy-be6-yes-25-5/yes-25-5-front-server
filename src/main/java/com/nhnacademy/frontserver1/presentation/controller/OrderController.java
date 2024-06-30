package com.nhnacademy.frontserver1.presentation.controller;

import com.nhnacademy.frontserver1.application.service.OrderService;
import com.nhnacademy.frontserver1.presentation.dto.request.order.CreateOrderRequest;
import com.nhnacademy.frontserver1.presentation.dto.request.order.ReadCartBookResponse;
import com.nhnacademy.frontserver1.presentation.dto.request.order.UpdateOrderRequest;
import com.nhnacademy.frontserver1.presentation.dto.response.order.CreateOrderResponse;
import com.nhnacademy.frontserver1.presentation.dto.response.order.ReadOrderStatusResponse;
import com.nhnacademy.frontserver1.presentation.dto.response.order.ReadOrderUserAddressResponse;
import com.nhnacademy.frontserver1.presentation.dto.response.order.ReadOrderUserInfoResponse;
import com.nhnacademy.frontserver1.presentation.dto.response.order.ReadShippingPolicyResponse;
import com.nhnacademy.frontserver1.presentation.dto.response.order.ReadTakeoutResponse;
import com.nhnacademy.frontserver1.presentation.dto.response.order.UpdateOrderResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private static final String MEMBER = "MEMBER";

    @GetMapping("/checkout")
    public String findAllCheckout(Model model, Pageable pageable) {
        List<ReadCartBookResponse> cartBookResponses = orderService.findAllCartBok();
        ReadOrderUserInfoResponse orderUserInfoResponse = orderService.getUserInfo();
        Integer totalAmount = getTotalAmount(cartBookResponses);

        populateUserInfo(model, orderUserInfoResponse);
        populateOrderDetails(model, pageable, totalAmount, cartBookResponses);

        return "order/checkout";
    }

    private void populateUserInfo(Model model, ReadOrderUserInfoResponse orderUserInfoResponse) {
        model.addAttribute("userInfo", orderUserInfoResponse);

        if (MEMBER.equals(orderUserInfoResponse.role())) {
            model.addAttribute("orderUserName", orderUserInfoResponse.name());
            model.addAttribute("orderUserEmail", orderUserInfoResponse.email());
            model.addAttribute("orderUserPhoneNumber", orderUserInfoResponse.phoneNumber());
            model.addAttribute("points", orderUserInfoResponse.points());
            model.addAttribute("maxDiscountCoupon", orderService.getMaxDiscountCoupon(getTotalAmount(orderService.findAllCartBok())));
        } else {
            model.addAttribute("orderUserName", "");
            model.addAttribute("orderUserEmail", "");
            model.addAttribute("orderUserPhoneNumber", "");
            model.addAttribute("points", 0);
            model.addAttribute("maxDiscountCoupon", null);
        }
    }

    private void populateOrderDetails(Model model, Pageable pageable, Integer totalAmount, List<ReadCartBookResponse> cartBookResponses) {
        ReadShippingPolicyResponse shippingPolicy = orderService.findAllOrderPolicy(pageable, totalAmount);
        ReadShippingPolicyResponse freeShippingPolicy = orderService.findFreePolicy();
        Integer freeShippingAmount = freeShippingPolicy.shippingPolicyMinAmount() - totalAmount;
        List<ReadTakeoutResponse> takeoutResponses = orderService.findAllTakeout();

        model.addAttribute("shippingPolicy", shippingPolicy);
        model.addAttribute("freeShippingPolicy", freeShippingPolicy);
        model.addAttribute("freeShippingAmount", freeShippingAmount);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("cartBooks", cartBookResponses);
        model.addAttribute("takeouts", takeoutResponses);
    }

    private Integer getTotalAmount(List<ReadCartBookResponse> cartBookResponses) {
        BigDecimal totalAmount = cartBookResponses.stream()
            .map(cartBookResponse -> cartBookResponse.bookPrice().multiply(BigDecimal.valueOf(cartBookResponse.cartBookQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAmount.intValue();
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(@RequestBody CreateOrderRequest request, HttpSession session) {
        log.info("주문 요청이 들어왔습니다. {}", request.toString());
        CreateOrderResponse response = orderService.createPreOrder(request);

        session.setAttribute("orderId", response.orderId());
        session.setAttribute("totalAmount", response.totalAmount());
        session.setAttribute("bookIds", response.bookIds());
        session.setAttribute("quantities", response.quantities());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/fail")
    public String getErrorPage() {
        return "error/order-fail";
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<ReadOrderStatusResponse> getOrderStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderStatusByOrderId(orderId));
    }

    @GetMapping("/address")
    public String getUserAddress(Pageable pageable, Model model) {
        Page<ReadOrderUserAddressResponse> responses = orderService.getUserAddresses(pageable);
        model.addAttribute("addresses", responses);

        return "order/address-popup";
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<UpdateOrderResponse> updateOrder(@PathVariable String orderId,
        @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrderByOrderId(orderId, request));
    }

}
