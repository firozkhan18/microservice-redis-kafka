package com.springboot.microservice.server.service;

import com.jayway.jsonpath.internal.Utils;
import com.springboot.microservice.adapters.filters.HeaderManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: Firoz Khan
 * @version:
 * @date:
 */
@Component
public class MyService1 {

    @Autowired
    private EchoService echoService;

    @Autowired
    private EchoSessionService echoSessionService;

    @Autowired
    private EchoAppService echoAppService;

    public void printData() {
        System.out.println("MyService1:Request-Scope: " + Utils.toString(echoService.getEchoData()));
        System.out.println("MyService1:Session-Scope: " + Utils.toString(echoSessionService.getEchoData()));
        System.out.println("MyService1:Apps----Scope: " + Utils.toString(echoAppService.getEchoData()));
    }
}
