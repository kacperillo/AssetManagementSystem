package com.assetmanagement.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ErrorDetails {

    private LocalDateTime timeStamp;
    private String code;
    private String message;
}
