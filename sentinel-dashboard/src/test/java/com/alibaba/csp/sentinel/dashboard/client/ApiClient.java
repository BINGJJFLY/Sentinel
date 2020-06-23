package com.alibaba.csp.sentinel.dashboard.client;

import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.alibaba.csp.sentinel.dashboard.client.ApiClient.CommandConstants.MSG_SUCCESS;

public class ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private CloseableHttpAsyncClient httpAsyncClient;

    public ApiClient() {
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setConnectTimeout(3000)
                .setSoTimeout(10000)
                .setIoThreadCount(Runtime.getRuntime().availableProcessors() * 2)
                .build();
        httpAsyncClient = HttpAsyncClients.custom()
                .setRedirectStrategy(new DefaultRedirectStrategy() {
                    @Override
                    protected boolean isRedirectable(String method) {
                        return false;
                    }
                })
                .setMaxConnTotal(4000)
                .setMaxConnPerRoute(1000)
                .setDefaultIOReactorConfig(ioReactorConfig)
                .build();
        httpAsyncClient.start();
    }

    public CompletableFuture<String> getAsync(String url, Map<String, String> params) {
        CompletableFuture<String> future = null;
        try {
            future = executeCommand(url, params)
                    .thenCompose(r -> {
                        if (MSG_SUCCESS.equalsIgnoreCase(r.trim())) {
                            return CompletableFuture.completedFuture(r);
                        }
                        return ApiClient.AsyncUtils.newFailedFuture(new CommandFailedException(r));
                    });
        } catch (Exception e) {
            future = ApiClient.AsyncUtils.newFailedFuture(e);
        }
        return future;
    }

    private CompletableFuture<String> executeCommand(String url, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder(url);
        if (params == null) {
            params = Collections.emptyMap();
        }
        if (!params.isEmpty()) {
            if (urlBuilder.indexOf("?") == -1) {
                urlBuilder.append('?');
            } else {
                urlBuilder.append('&');
            }
            urlBuilder.append(queryString(params));
        }
        return executeCommand(new HttpGet(urlBuilder.toString()));
    }

    private CompletableFuture<String> executeCommand(HttpUriRequest request) {
        CompletableFuture<String> future = new CompletableFuture<>();
        httpAsyncClient.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse response) {
                int statusCode = response.getStatusLine().getStatusCode();
                try {
                    String value = getBody(response);
                    if (isSuccess(statusCode)) {
                        future.complete(value);
                    } else {
                        if (isCommandNotFound(statusCode, value)) {
                            future.completeExceptionally(new CommandNotFoundException(request.getURI().getPath()));
                        } else {
                            future.completeExceptionally(new CommandFailedException(value));
                        }
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e);
                    logger.error("HTTP request failed: {}", request.getURI().toString(), e);
                }
            }

            @Override
            public void failed(Exception e) {
                future.completeExceptionally(e);
                logger.error("HTTP request failed: {}", request.getURI().toString(), e);
            }

            @Override
            public void cancelled() {
                future.complete(null);
            }
        });
        return future;
    }

    private String getBody(HttpResponse response) throws Exception {
        Charset charset = null;
        try {
            String contentTypeStr = response.getFirstHeader(HTTP_HEADER_CONTENT_TYPE).getValue();
            if (StringUtil.isNotEmpty(contentTypeStr)) {
                ContentType contentType = ContentType.parse(contentTypeStr);
                charset = contentType.getCharset();
            }
        } catch (Exception ignore) {
        }
        return EntityUtils.toString(response.getEntity(), charset != null ? charset : DEFAULT_CHARSET);
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    private boolean isCommandNotFound(int statusCode, String body) {
        return statusCode == 400 && StringUtil.isNotEmpty(body) && body.contains(CommandConstants.MSG_UNKNOWN_COMMAND_PREFIX);
    }

    private StringBuilder queryString(Map<String, String> params) {
        StringBuilder queryStringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (StringUtil.isEmpty(entry.getValue())) {
                continue;
            }
            String name = urlEncode(entry.getKey());
            String value = urlEncode(entry.getValue());
            if (name != null && value != null) {
                if (queryStringBuilder.length() > 0) {
                    queryStringBuilder.append('&');
                }
                queryStringBuilder.append(name).append('=').append(value);
            }
        }
        return queryStringBuilder;
    }

    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, DEFAULT_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            logger.info("encode string error: {}", str, e);
            return null;
        }
    }

    public void close() throws Exception {
        httpAsyncClient.close();
    }

    public class CommandNotFoundException extends Exception {

        public CommandNotFoundException() {
        }

        public CommandNotFoundException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    public class CommandFailedException extends RuntimeException {

        public CommandFailedException() {
        }

        public CommandFailedException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    public static final class AsyncUtils {

        private static final Logger LOG = LoggerFactory.getLogger(com.alibaba.csp.sentinel.dashboard.util.AsyncUtils.class);

        public static <R> CompletableFuture<R> newFailedFuture(Throwable ex) {
            CompletableFuture<R> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }

        public static <R> CompletableFuture<List<R>> sequenceFuture(List<CompletableFuture<R>> futures) {
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(com.alibaba.csp.sentinel.dashboard.util.AsyncUtils::getValue)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
                    );
        }

        public static <R> CompletableFuture<List<R>> sequenceSuccessFuture(List<CompletableFuture<R>> futures) {
            return CompletableFuture.supplyAsync(() -> futures.parallelStream()
                    .map(com.alibaba.csp.sentinel.dashboard.util.AsyncUtils::getValue)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
            );
        }

        public static <T> T getValue(CompletableFuture<T> future) {
            try {
                return future.get(10, TimeUnit.SECONDS);
            } catch (Exception ex) {
                LOG.error("getValue for async result failed", ex);
            }
            return null;
        }

        public static boolean isSuccessFuture(CompletableFuture future) {
            return future.isDone() && !future.isCompletedExceptionally() && !future.isCancelled();
        }

        private AsyncUtils() {
        }
    }

    public final class CommandConstants {

        public static final String MSG_INVALID_COMMAND = "Invalid command";
        public static final String MSG_UNKNOWN_COMMAND_PREFIX = "Unknown command";

        public static final String MSG_SUCCESS = "success";
        public static final String MSG_FAIL = "failed";

        private CommandConstants() {
        }
    }
}
