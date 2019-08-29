package com.wizeline.meetup.frontendservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(
        value = "messages",
        url = "${messages.endpoint:http://localhost:9000/messages}")
public interface MessagesBackendClient {
    @RequestMapping(method = RequestMethod.GET, path = "/")
    Resources<Map> getMessages(@RequestParam(value = "sort") String sort);

    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    Map getMessage(@PathVariable("id") long messageId);

    @RequestMapping(method = RequestMethod.POST, path = "/")
    Resource<Map> add(@RequestBody Map message);
}

