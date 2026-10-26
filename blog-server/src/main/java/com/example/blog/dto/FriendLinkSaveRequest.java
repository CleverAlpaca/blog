package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FriendLinkSaveRequest {

    @NotBlank(message = "站点名称不能为空")
    @Size(max = 50, message = "站点名称最长 50 字")
    private String name;

    private String logo;

    @NotBlank(message = "站点地址不能为空")
    @Size(max = 512, message = "站点地址过长")
    private String url;

    @Size(max = 255, message = "描述最长 255 字")
    private String description;

    private Integer sort;

    private Integer status;
}
