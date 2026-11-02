package com.example.blog.controller.front;

import com.example.blog.common.PageResult;
import com.example.blog.common.Result;
import com.example.blog.service.NotificationService;
import com.example.blog.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内通知中心（登录态）：分页 / 未读数 / 单条已读 / 全部已读
 */
@RestController
@RequestMapping("/api/front/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/page")
    public Result<PageResult<NotificationVO>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.ok(notificationService.pageMine(pageNum, Math.min(pageSize, 50)));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(notificationService.unreadCount());
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.ok();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead();
        return Result.ok();
    }
}
