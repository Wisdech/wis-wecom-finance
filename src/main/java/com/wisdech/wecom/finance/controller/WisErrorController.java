package com.wisdech.wecom.finance.controller;

import com.wisdech.utils4j.response.ARShowType;
import com.wisdech.utils4j.response.ActionResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WisErrorController implements ErrorController {

    @RequestMapping("/error")
    public ActionResult<?> error(HttpServletRequest request) {

        return ActionResult.defaultFailed(String.valueOf(getStatus(request).value()), getStatus(request).name(), ARShowType.MESSAGE_ERROR);
    }

    HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            try {
                return HttpStatus.valueOf(statusCode);
            } catch (Exception var4) {
                return HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
    }
}
