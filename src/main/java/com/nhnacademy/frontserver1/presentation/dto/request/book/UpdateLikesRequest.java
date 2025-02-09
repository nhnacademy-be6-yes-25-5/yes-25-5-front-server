package com.nhnacademy.frontserver1.presentation.dto.request.book;

import jakarta.validation.constraints.NotNull;

public record UpdateLikesRequest(

        @NotNull(message = "책 아이디는 필수 입력 항목입니다.")
        Long bookId,

        @NotNull(message = "유저 아이디는 필수 입력 항목입니다.")
        Long userId) {

}