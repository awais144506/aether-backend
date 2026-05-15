package com.ocp.aether.utility;

import java.net.http.HttpClient;
import java.time.Duration;

public class HttpClientUtility {
    public static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

}
