package com.bjca.fabricsdkdsvsrest.controller;

import com.bjca.fabricsdkdsvsrest.entity.Response;
import com.bjca.fabricsdkdsvsrest.pojo.TxArgsPojo;
import com.bjca.fabricsdkdsvsrest.service.FabricService;
import com.bjca.fabricsdkdsvsrest.tools.ExecuteResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class FabricController {

    @Resource
    private FabricService fabricService;

    @PostMapping("/query")
    public Response query(@RequestBody TxArgsPojo txArgsPojo) throws Exception {
        ExecuteResult er = fabricService.query(txArgsPojo);
        if(er == null || er.getResult().equals("") || er.getResult() == null) {
            return Response.fail(500, "query failed", null);
        }
        return Response.success(er.getResult());
    }

    @PostMapping("/invoke")
    public Response invoke(@RequestBody TxArgsPojo txArgsPojo) throws Exception {
        ExecuteResult er = fabricService.invoke(txArgsPojo);
        if(er == null || er.getResult() == null) {
            return Response.fail(500, "query failed", null);
        }
        return Response.success("success", er.getResult());
    }



}
