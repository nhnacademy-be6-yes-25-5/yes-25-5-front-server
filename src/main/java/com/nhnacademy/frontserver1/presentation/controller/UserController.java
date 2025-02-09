package com.nhnacademy.frontserver1.presentation.controller;

import com.nhnacademy.frontserver1.application.service.UserService;
import com.nhnacademy.frontserver1.common.utils.CookieUtils;
import com.nhnacademy.frontserver1.common.exception.FeignClientException;
import com.nhnacademy.frontserver1.presentation.dto.request.user.*;
import com.nhnacademy.frontserver1.presentation.dto.response.point.PointLogResponse;
import com.nhnacademy.frontserver1.presentation.dto.response.user.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.*;
import com.nhnacademy.frontserver1.presentation.dto.request.user.UpdatePasswordRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import com.nhnacademy.frontserver1.presentation.dto.request.user.FindPasswordRequest;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 가입 페이지
    @GetMapping("/sign-up")
    public String signUp(HttpServletRequest request) {
        CookieUtils.validateAccessToken(request);

        return "register";
    }

    // 회원 가입
    @PostMapping("/sign-up")
    public String signUp(@RequestParam String userName,
                         @RequestParam LocalDate userBirth,
                         @RequestParam String userEmail,
                         @RequestParam String userPhone,
                         @RequestParam String userPassword,
                         @RequestParam String userConfirmPassword,
                         Model model) {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .userName(userName)
                .userBirth(userBirth)
                .userEmail(userEmail)
                .userPhone(userPhone)
                .userPassword(userPassword)
                .userConfirmPassword(userConfirmPassword)
                .providerName("LOCAL")
                .build();

        UserResponse userResponse = userService.signUp(userRequest);

        model.addAttribute("userResponse", userResponse);

        return "redirect:/auth/login";
    }

    // 회원 정보 수정 페이지
    @GetMapping("/mypage/info/verify")
    public String userInfo() {
        return "mypage/mypage-info-verify";
    }

    @PostMapping("/mypage/info/verify")
    public ResponseEntity<Boolean> userInfoCheck(@RequestBody CheckPasswordRequest passwordRequest,
                                                 HttpSession session) {
        Boolean isPasswordValid = userService.checkPassword(passwordRequest);

        if (isPasswordValid) {
            session.setAttribute("checkPassword", true);
            return ResponseEntity.ok(true);
        }
        else {
            return ResponseEntity.badRequest().body(false);
        }
    }

    // 회원 정보 수정 페이지
    @GetMapping("/mypage/info")
    public String userInfo(Model model, HttpSession session) {

        Boolean isPasswordValid = (Boolean) session.getAttribute("checkPassword");

        if (isPasswordValid == null || !isPasswordValid) {
            return "redirect:/mypage/info/verify";
        }

        UserResponse user = userService.findByUser();

        model.addAttribute("user", user);

        session.removeAttribute("checkPassword");

        return "mypage/mypage-info";
    }

    // 회원 정보 수정
    @PutMapping("/mypage/info")
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserRequest userRequest,
                                           Model model) {

        UpdateUserResponse user = userService.updateUser(userRequest);

        model.addAttribute("user", user);

        return ResponseEntity.ok().build();
    }

    // 회원 탈퇴 페이지
    @GetMapping("/mypage/delete")
    public String userDelete(Model model) {

        return "mypage/mypage-delete";
    }

    // 회원 탈퇴
    @DeleteMapping("/mypage/delete")
    public ResponseEntity<Void> deleteUser(@RequestBody DeleteUserRequest userRequest) {
        userService.deleteUser(userRequest);

        return ResponseEntity.ok().build();
    }

    // 회원 등급 페이지
    @GetMapping("/mypage/grades")
    public String getUserGrades(Model model) {

        // todo : 회원 등급 가져오는 로직

        UserGradeResponse userGrade = userService.getUserGrade();

        model.addAttribute("userGrade", userGrade);

        return "mypage/mypage-grade";
    }

    // 회원 현재 포인트 조회
    @GetMapping("/mypage/points")
    public ResponseEntity<PointResponse> getUserPoints() {
        return ResponseEntity.ok(userService.getPoints());
    }

    // 회원 포인트 이력 조회
    @GetMapping("/mypage/point-logs")
    public String getUserPoints(@PageableDefault(size = 15) Pageable pageable,
                                Model model) {

        Page<PointLogResponse> pointLogs = userService.getPointLogs(pageable);

        model.addAttribute("pointLogs", pointLogs);

        return "mypage/mypage-pointLogs";
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.isEmailDuplicate(email));
    }



    // 배송지 목록 조회
    @GetMapping("/mypage/addresses")
    public String getUserAddresses(@PageableDefault Pageable pageable,
                                   Model model) {

        Page<UserAddressResponse> userAddresses = userService.getAllUserAddresses(pageable);
        Optional<UserAddressResponse> defaultAddress = userAddresses.stream()
                .filter(UserAddressResponse::addressBased)
                .findFirst();


        model.addAttribute("addresses", userAddresses);
        model.addAttribute("defaultAddress", defaultAddress.orElse(null));


        return "mypage/mypage-address";
    }

    // 주소 기본 배송지 지정
    @PatchMapping("/mypage/addresses/{userAddressId}")
    public ResponseEntity<Void> updateAddressBased(@PathVariable Long userAddressId,
                                                   @RequestBody UpdateAddressBasedRequest request) {

        userService.updateAddressBased(userAddressId, request);

        return ResponseEntity.ok().build();
    }

    // 배송지 추가
    @PostMapping("/mypage/addresses")
    public ResponseEntity<CreateUserAddressResponse> createUserAddresses(@ModelAttribute CreateUserAddressRequest request) {
        return ResponseEntity.ok(userService.createUserAddresses(request));
    }

    // 배송지 수정
    @PutMapping("/mypage/addresses/{userAddressId}")
    public ResponseEntity<UpdateUserAddressResponse> updateUserAddress(@PathVariable Long userAddressId,
                                                                       @RequestBody UpdateUserAddressRequest request) {
        return ResponseEntity.ok(userService.updateUserAddress(userAddressId, request));
    }

    // 배송지 단건 조회
    @GetMapping("/mypage/addresses/{userAddressId}")
    public ResponseEntity<UserAddressResponse> findUserAddressById(@PathVariable Long userAddressId) {
        return ResponseEntity.ok(userService.findUserAddressById(userAddressId));
    }

    @DeleteMapping("/mypage/addresses/{userAddressId}")
    public ResponseEntity<Void> deleteUserAddressById(@PathVariable Long userAddressId) {

        userService.deleteUserAddress(userAddressId);

        return ResponseEntity.ok().build();
    }

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    @GetMapping("/find-email")
    public String showFindEmailForm() {
        return "findMail/find-mail";
    }

    @PostMapping("/find-email")
    public String findEmail(@RequestParam String name, @RequestParam String phone, Model model) {

        List<FindUserResponse> emails = userService.findAllUserEmailByUserNameByUserPhone(name, phone);

        model.addAttribute("name", name);
        model.addAttribute("phone", phone);

        if (emails.isEmpty()) {
            return "findMail/find-mail-fail";
        } else {
            model.addAttribute("emails", emails);
            return "findMail/find-mail-success";
        }
    }

    @GetMapping("/mypage/coupons")
    public String getActiveUserCoupons(@PageableDefault Pageable pageable,
                                       Model model) {

        Page<CouponBoxResponse> activeCoupons = userService.getStateCouponBox("ACTIVE", pageable);
        Page<CouponBoxResponse> usedCoupons = userService.getStateCouponBox("USED", pageable);
        Page<CouponBoxResponse> expiredCoupons = userService.getStateCouponBox("EXPIRED", pageable);

        model.addAttribute("activeCoupons", activeCoupons);
        model.addAttribute("usedCoupons", usedCoupons);
        model.addAttribute("expiredCoupons", expiredCoupons);

        return "mypage/mypage-coupon";
    }

    // 페이코 회원 가입 페이지
    @GetMapping("/sign-up/payco")
    public String signUpPayco() {
        return "register-payco";
    }

    // 페이코 회원 가입
    @PostMapping("/sign-up/payco")
    public String signUpPayco(@RequestParam String userName,
                         @RequestParam LocalDate userBirth,
                         @RequestParam String userEmail,
                         @RequestParam String userPhone,
                         Model model) {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .userName(userName)
                .userBirth(userBirth)
                .userEmail(userEmail)
                .userPhone(userPhone)
                .providerName("PAYCO")
                .build();

        UserResponse userResponse = userService.signUp(userRequest);

        model.addAttribute("userResponse", userResponse);

        return "redirect:/auth/login";
    }

    @GetMapping("/users/find/password")
    public String showFindPasswordForm(){
        return "findPassword/find-password";
    }

    @PostMapping("/users/find/password")
    public String findPassword(@RequestParam String email, @RequestParam String name, Model model) {
        FindPasswordRequest request = new FindPasswordRequest(email, name);
        boolean isUserFound = userService.findUserPasswordByEmailByName(request);

        if (isUserFound) {
            userService.sendEmail(email);
            return "findPassword/send-mail-success";
        } else {
            return "findPassword/send-mail-fail";
        }
    }



    @GetMapping("/reset-password/{email}")
    public String showResetPasswordForm(@PathVariable("email") String email, Model model){
        model.addAttribute("email", email);
        return "setNewPassword/set-new-password";

    }

    @PutMapping("/reset-password/[{email}")
    public String resetPassword(@RequestBody String email, @RequestBody String newPassword, @RequestBody String confirmPassword, Model model) {
       // UpdatePasswordRequest request = new UpdatePasswordRequest(newPassword, confirmPassword);
        //boolean isPasswordReset = userService.setUserPasswordByEmail(email, request);

        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .userPassword(newPassword)
                .confirmPassword(confirmPassword)
                .build();

       // boolean isPasswordReset = userService.UpdateUserPasswordByEmail(email, request);


        if (newPassword.equals(confirmPassword)) {
            return "setNewPassword/set-new-password-success";
        } else {
            return "setNewPassword/set-new-password-fail";
        }
//@Builder
//public record UpdatePasswordRequest(String userPassword, String confirmPassword) {}
//        if (isPasswordReset) {
//            return "setNewPassword/set-new-password-success";
//        } else {
//            return "setNewPassword/set-new-password-fail";
//        }
    }

//    @PostMapping("/reset-password/{email}")
//        public String resetPassword(@PathVariable String email, @RequestParam String newPassword, @RequestParam String confirmPassword, Model model) {
//        if (!newPassword.equals(confirmPassword)) {
//            model.addAttribute("error", "Passwords do not match");
//            return "setNewPassword/set-new-password";
//        }
//        //UpdateUserRequest(String userName, String userPhone, LocalDate userBirth, String userPassword,
//        //                                String newUserPassword, String newUserConfirmPassword)
//        UpdateUserRequest request = UpdateUserRequest.builder()
//                .userName(null)  // 유저 이름을 업데이트하지 않으므로 null
//                .userPhone(null)  // 유저 전화번호를 업데이트하지 않으므로 null
//                .userBirth(null)  // 유저 생일을 업데이트하지 않으므로 null
//                .userPassword(newPassword)  // 현재 비밀번호가 필요하지 않으므로 null
//                .newUserConfirmPassword(confirmPassword)
//                .build();
//
//
////            boolean isPasswordReset;
////            try {
////                UpdatePasswordResponse response = userService.updateUser(request);
////
////
////                isPasswordReset = response != null;
////            } catch (Exception e) {
////                isPasswordReset = false;
////            }
////
////            if (isPasswordReset) {
////                return "setNewPassword/set-new-password-success";
////            } else {
////                model.addAttribute("error", "Password update failed");
////                return "setNewPassword/set-new-password-fail";
////            }
////            return null; }
//
//
//    }
        //@PutMapping("/info")
    //    UpdateUserResponse updateUser(@RequestBody UpdateUserRequest userRequest);
}