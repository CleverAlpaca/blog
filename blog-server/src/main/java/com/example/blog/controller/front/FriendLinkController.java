package com.example.blog.controller.front;

import com.example.blog.common.Result;
import com.example.blog.service.FriendLinkService;
import com.example.blog.vo.FriendLinkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("frontFriendLinkController")
@RequestMapping("/api/front/friend-link")
@RequiredArgsConstructor
public class FriendLinkController {

    private final FriendLinkService friendLinkService;

    @GetMapping("/list")
    public Result<List<FriendLinkVO>> list() {
        return Result.ok(friendLinkService.listVisible());
    }
}
