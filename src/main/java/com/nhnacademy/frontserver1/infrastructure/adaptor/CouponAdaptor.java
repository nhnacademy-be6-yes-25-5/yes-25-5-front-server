package com.nhnacademy.frontserver1.infrastructure.adaptor;

import com.nhnacademy.frontserver1.presentation.dto.request.coupon.CouponPolicyRequestDTO;
import com.nhnacademy.frontserver1.presentation.dto.response.coupon.CouponPolicyResponseDTO;
import com.nhnacademy.frontserver1.presentation.dto.response.coupon.CouponUserListResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "couponAdaptor", url = "http://localhost:8085")
public interface CouponAdaptor {

    @GetMapping("/admin-policy")
    List<CouponPolicyResponseDTO> findAll();

    @GetMapping("/user-coupons/user")
    List<CouponUserListResponseDTO> findUserCoupons(@RequestParam("userId") Long userId);

    @PostMapping("/admin-policy/coupon")
    void create(@RequestBody CouponPolicyRequestDTO createCouponRequest);

    @DeleteMapping("/admin-policy/coupon/{id}")
    void delete(@PathVariable Long id);
}
