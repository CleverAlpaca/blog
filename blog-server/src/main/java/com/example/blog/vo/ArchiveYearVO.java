package com.example.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ArchiveYearVO {

    private Integer year;

    private List<ArchiveItemVO> articles;
}
