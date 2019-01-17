package com.ajin.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: ajin
 * @Date: 2019/1/17 18:05
 * @Description:返回消息的实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Msg {
    private String title;
    private String content;
    private String etraInfo;
}
