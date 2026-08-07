package com.wooseok.notifyflow.simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.util.Iterator;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class EventPublishSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8080")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    ScenarioBuilder orderPlaced = scenario("Publish ORDER_PLACED events")
            .feed(new Iterator<Map<String, Object>>() {
                final java.util.concurrent.atomic.AtomicInteger counter =
                        new java.util.concurrent.atomic.AtomicInteger(0);

                public boolean hasNext() {
                    return true;
                }

                public Map<String, Object> next() {
                    int i = counter.incrementAndGet();
                    return Map.of("userId", i, "orderId", i);
                }
            })
            .exec(http("POST /api/events/publish - ORDER_PLACED")
                    .post("/api/events/publish")
                    .body(StringBody("""
                            {
                                "eventType": "ORDER_PLACED",
                                "userId": "load-test-user-#{userId}",
                                "orderId": "ord-#{orderId}",
                                "orderTotal": 49.99
                            }
                            """))
                    .check(status().in(202, 409, 429)));

    ScenarioBuilder passwordReset = scenario("Publish PASSWORD_RESET events")
            .feed(new Iterator<Map<String, Object>>() {
                final java.util.concurrent.atomic.AtomicInteger counter =
                        new java.util.concurrent.atomic.AtomicInteger(0);

                public boolean hasNext() {
                    return true;
                }

                public Map<String, Object> next() {
                    int i = counter.incrementAndGet();
                    return Map.of("userId", i);
                }
            })
            .exec(http("POST /api/events/publish - PASSWORD_RESET")
                    .post("/api/events/publish")
                    .body(StringBody("""
                            {
                                "eventType": "PASSWORD_RESET",
                                "userId": "load-test-user-#{userId}",
                                "resetToken": "token-#{userId}"
                            }
                            """))
                    .check(status().in(202, 409, 429)));

    {
        setUp(
                orderPlaced.injectOpen(
                        rampUsers(50).during(10),
                        constantUsersPerSec(20).during(30)
                ).protocols(httpProtocol),

                passwordReset.injectOpen(
                        rampUsers(30).during(10),
                        constantUsersPerSec(10).during(30)
                ).protocols(httpProtocol)
        ).assertions(
                global().responseTime().percentile(99).lt(2000),
                global().successfulRequests().percent().gt(95.0)
        );
    }
}