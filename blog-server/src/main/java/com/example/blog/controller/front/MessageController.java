package com.example.blog.controller.front;

import com.example.blog.common.Result;
import com.example.blog.dto.MessagePostRequest;
import cn.dev33.satoken.stp.StpUtil;
import com.example.blog.service.MessageService;
import com.example.blog.util.IpUtil;
import com.example.blog.vo.MessageVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("frontMessageController")
@RequestMapping("/api/front/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/list")
    public Result<List<MessageVO>> list() {
        return Result.ok(messageService.tree());
    }

    @PostMapping
    public Result<Void> post(@Valid @RequestBody MessagePostRequest body, HttpServletRequest request) {
        // 与评论保持一致的社区政策：仅登录可留言
        StpUtil.checkLogin();
        messageService.post(body, IpUtil.getIp(request));
        return Result.ok();
    }
}
