package com.nhnacademy.frontserver1.infrastructure.adaptor;

import com.nhnacademy.frontserver1.presentation.dto.request.coupon.CouponPolicyRequestDTO;
import com.nhnacademy.frontserver1.presentation.dto.response.coupon.CouponPolicyResponseDTO;
//import com.nhnacademy.frontserver1.presentation.dto.response.coupon.CouponUserListResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "couponAdaptor", url = "=http://172.17.0.8:8080/coupons")
public interface CouponAdaptor {

    @GetMapping("/policy")
    List<CouponPolicyResponseDTO> findAll();

    @PostMapping("/policy/create")
    void create(@RequestBody CouponPolicyRequestDTO createCouponRequest);

//    @GetMapping("/user-coupons/user")
//    List<CouponUserListResponseDTO> findUserCoupons(@RequestParam("userId") Long userId);

//
//    @DeleteMapping("/admin-policy/coupon/{id}")
//    void delete(@PathVariable Long id);
}
