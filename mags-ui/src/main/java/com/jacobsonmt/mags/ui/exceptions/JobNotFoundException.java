package com.jacobsonmt.mags.ui.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Job Not Found")
public class JobNotFoundException extends RuntimeException {
}