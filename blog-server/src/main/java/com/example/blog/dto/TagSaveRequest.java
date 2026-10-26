package com.example.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagSaveRequest {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称最长 50 字")
    private String name;
}
