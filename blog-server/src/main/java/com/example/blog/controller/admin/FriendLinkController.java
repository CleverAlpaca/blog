package com.example.blog.controller.admin;

import com.example.blog.common.Result;
import com.example.blog.dto.FriendLinkSaveRequest;
import com.example.blog.service.FriendLinkService;
import com.example.blog.vo.FriendLinkVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/friend-link")
@RequiredArgsConstructor
public class FriendLinkController {

    private final FriendLinkService friendLinkService;

    @GetMapping("/list")
    public Result<List<FriendLinkVO>> list() {
        return Result.ok(friendLinkService.listAdmin());
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody FriendLinkSaveRequest request) {
        friendLinkService.save(request);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody FriendLinkSaveRequest request) {
        friendLinkService.update(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        friendLinkService.delete(id);
        return Result.ok();
    }
}
