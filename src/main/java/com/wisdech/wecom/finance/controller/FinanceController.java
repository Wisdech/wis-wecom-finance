package com.wisdech.wecom.finance.controller;

import cn.hutool.json.JSONObject;
import com.wisdech.utils4j.response.ARShowType;
import com.wisdech.utils4j.response.ActionResult;
import com.wisdech.wecom.finance.request.CredentialRequest;
import com.wisdech.wecom.finance.service.FinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FinanceController {

    @Autowired
    FinanceService financeService;

    @PostMapping("/messages/{seq}")
    public ActionResult<?> getMessages(@RequestBody CredentialRequest request, @PathVariable Integer seq, @RequestParam(defaultValue = "50") String limit) {

        try {
            financeService.init(
                    request.getCorpId(),
                    request.getSecret(),
                    request.getPrivateKey()
            );

            List<JSONObject> messagesList = financeService.getMessage(seq, Integer.parseInt(limit));

            return ActionResult.defaultOk(messagesList);

        } catch (Exception e) {
            return ActionResult.defaultFailed("5000", e.getMessage(), ARShowType.MESSAGE_ERROR);
        } finally {
            financeService.destroySdk();
        }
    }

    @PostMapping("/media/{id}")
    public ActionResult<?> getMedia(@RequestBody CredentialRequest request, @PathVariable String id, @RequestParam String md5, @RequestParam String name) {

        try {
            financeService.init(
                    request.getCorpId(),
                    request.getSecret(),
                    request.getPrivateKey()
            );

            String filename = financeService.getMedia(id, md5, name);

            return ActionResult.defaultOk(filename);

        } catch (Exception e) {
            return ActionResult.defaultFailed("5000", e.getMessage(), ARShowType.MESSAGE_ERROR);
        } finally {
            financeService.destroySdk();
        }
    }

}
