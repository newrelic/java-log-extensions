/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.testapps.dropwizard;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/hello/{name}")
public class Hello {
    @GET
    public String sayHello(@PathParam("name") String name) {
        return "Hello, " + name + "!";
    }

    @POST
    public String includePostdata(String body) {
        return "{\"request_length\":" + body.length() + "}";
    }
}
