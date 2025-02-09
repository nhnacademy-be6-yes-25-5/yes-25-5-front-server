package com.nhnacademy.frontserver1.presentation.controller;

import com.nhnacademy.frontserver1.application.service.BookService;
import com.nhnacademy.frontserver1.application.service.CategoryService;
import com.nhnacademy.frontserver1.application.service.TagService;
import com.nhnacademy.frontserver1.application.service.UploadImageService;
import com.nhnacademy.frontserver1.common.exception.BookException;
import com.nhnacademy.frontserver1.presentation.dto.request.book.CreateBookRequest;
import com.nhnacademy.frontserver1.presentation.dto.request.book.UpdateBookRequest;
import com.nhnacademy.frontserver1.presentation.dto.response.book.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.print.Book;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminBookController {

    private final BookService bookService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final UploadImageService uploadimageService;

    @GetMapping("/admin/product")
    public String adminProduct(Model model, @PageableDefault(page = 0, size = 10) Pageable pageable) {

        List<CategoryResponse> categoryList = categoryService.findAllCategories();
        List<TagResponse> tagList = tagService.findAllTags();
        Page<BookResponse> bookResponseList = bookService.findAllBooks(pageable);

        int nowPage = bookResponseList.getPageable().getPageNumber();
        int startPage = Math.max(nowPage - 4, 0);
        int endPage = Math.min(nowPage + 5, bookResponseList.getTotalPages() -1);

        if(bookResponseList.getTotalPages() <= 10) {
            startPage = 0;
            endPage = bookResponseList.getTotalPages() - 1;
        } else {
            if (startPage == 0) {
                endPage = 9;
            } else if (endPage == bookResponseList.getTotalPages() -1) {
                startPage = bookResponseList.getTotalPages() - 10;
            }
        }

        model.addAttribute("categoryList", categoryList);
        model.addAttribute("tagList", tagList);
        model.addAttribute("productList", bookResponseList);
        model.addAttribute("nowPage", nowPage + 1);
        model.addAttribute("startPage", startPage + 1);
        model.addAttribute("endPage", endPage + 1);

        return "admin/product/admin-product";
    }

    @PostMapping("/admin/product")
    public String adminProduct(@ModelAttribute @Valid CreateBookRequest request, @RequestParam(value = "categoryIdList") List<Long> categoryIdList,
                               @RequestParam(value = "tagIdList", required = false) List<Long> tagIdList, @RequestParam(value = "bookImage", required = false) MultipartFile file) {

        UploadImageResponse uploadImageResponse = uploadimageService.imageUpload(file);
        CreateBookRequest createBookRequest = CreateBookRequest.updateImageURL(request, uploadImageResponse.imageUrl());
        bookService.createBook(createBookRequest, categoryIdList, tagIdList);

        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/{bookId}/delete")
    public String adminDeleteBook(@PathVariable Long bookId) {

        bookService.deleteBook(bookId);

        return "redirect:/admin/product";
    }

    @PostMapping("/admin/product/{bookId}/update")
    public String adminUpdateBook(@ModelAttribute @Valid UpdateBookRequest request, @RequestParam(value = "categoryIdList") List<Long> categoryIdList,
                                  @RequestParam(value = "tagIdList", required = false) List<Long> tagIdList, @RequestParam(value = "bookImage", required = false) MultipartFile file) {

        BookResponse book = bookService.getBook(request.bookId());

        if(!file.isEmpty()) {
            UploadImageResponse uploadImageResponse = uploadimageService.imageUpload(file);
            UpdateBookRequest updateBookRequest = UpdateBookRequest.updateImageURL(request, uploadImageResponse.imageUrl());
        } else {
            UpdateBookRequest updateBookRequest = UpdateBookRequest.updateImageURL(request, book.bookImage());
            bookService.updateBook(updateBookRequest, categoryIdList, tagIdList);
        }

        return "redirect:/admin/product";
    }

    @GetMapping("/admin/bookSearch")
    public String adminBookSearch(Model model) {

        String keyword = "";
        model.addAttribute("keyword", keyword);

        return "admin/product/admin-book-search";
    }

    @PostMapping("/admin/bookSearch")
    public String adminBookSearch(@ModelAttribute("keyword") String keyword, Model model) {

        List<BookAPIResponse> bookList = bookService.searchBooks(keyword);
        model.addAttribute("bookList", bookList);

        return "admin/product/admin-book-search";
    }

    @ExceptionHandler(BookException.class)
    public String bookException(Model model) {

        model.addAttribute("message", "이미 존재하는 책입니다.");
        model.addAttribute("url", "/admin/product");

        return "message";
    }
}
