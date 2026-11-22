package com.zhangmei.booking.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProfileView {

    private Long id;
    private String nickname;
    private String mobile;
    private String accountNo;
    private String gender;
    private LocalDate birthday;
    private String address;
    private String memberLevel;
    private List<String> tags;
}
