package ai.nvwa.agent.components.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @description HTTP工具
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Slf4j
public class HttpClient {

    private final CloseableHttpClient client;
    private HttpRequestBase request;
    private String url;
    private String body;
    private int status;
    private HttpEntity entity;
    private String result;

    private HttpClient() {
        this.client = HttpClients.createDefault();
    }

    private HttpClient(String url) {
        this();
        this.url = url;
    }

    /**
     * @description HttpPost 请求
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static HttpClient post(String url) {
        return new HttpClient(url).request(new HttpPost(url));
    }

    /**
     * @description HttpGet 请求
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static HttpClient get(String url) {
        return new HttpClient(url).request(new HttpGet(url));
    }

    /**
     * @description HttpPut 请求
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static HttpClient put(String url) {
        return new HttpClient(url).request(new HttpPut(url));
    }

    /**
     * @description HttpDelete 请求
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static HttpClient delete(String url) {
        return new HttpClient(url).request(new HttpDelete(url));
    }

    /**
     * @description 设置请求对象
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private HttpClient request(HttpRequestBase request) {
        this.request = request;
        this.request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        this.request.addHeader(HttpHeaders.ACCEPT, ContentType.WILDCARD.getMimeType());
        return this;
    }

    /**
     * @description 设置请求头
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public HttpClient header(String name, String value) {
        request.addHeader(name, value);
        return this;
    }

    public HttpClient contentType(String value) {
        this.header(HttpHeaders.CONTENT_TYPE, value);
        return this;
    }

    public HttpClient accept(String value) {
        this.header(HttpHeaders.ACCEPT, value);
        return this;
    }

    public HttpClient authorization(String value) {
        this.header(HttpHeaders.AUTHORIZATION, value);
        return this;
    }

    /**
     * @description 设置请求体
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public HttpClient body(String body) {
        if (request instanceof HttpEntityEnclosingRequestBase) {
            ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(body, ContentType.getByMimeType(
                    request.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue())));
        }
        this.body = body;
        return this;
    }

    /**
     * @description 执行请求
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private HttpClient execute() {
        try (CloseableHttpResponse response = client.execute(request)) {
            status = response.getStatusLine().getStatusCode();
            entity = response.getEntity();
            result = EntityUtils.toString(entity);
            log.debug("[Http请求] url={}, body={}, response={}, status={}, 请求完成",
                    url, body, result, status);
        } catch (IOException e) {
            log.debug("[Http请求] url={}, body={}, 请求异常: {}",
                    url, body, e.getMessage(), e);
        }
        return this;
    }

    /**
     * @description 获取响应状态
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public int asStatus() {
        if (status > 0) {
            return status;
        }
        return this.execute().status;
    }

    /**
     * @description 获取字符串结果
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public String asString() {
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        return this.execute().result;
    }

    /**
     * @description 表单请求
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public String asFromData() {
        return this.header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
                .asString();
    }

//    /**
//     * @description 获取响应流
//     * <p> <功能详细描述> </p>
//     *
//     * @author 陈晨
//     */
//    public InputStream asStream() {
//        /**
//         * setConnectTimeout：设置连接超时时间，单位毫秒。
//         * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
//         * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
//         * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
//         */
////        RequestConfig requestConfig = RequestConfig.custom()
////                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
////                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
////                .build();
////        httpPost.setConfig(requestConfig);
//    }

    /**
     * @description 流处理
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public interface StreamHandle {
        void handle(String input);
    }

    /**
     * @description 响应流处理
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public HttpClient asStreamHandle(StreamHandle handle) {
        this.header("Accept-Encoding", "gzip, deflate, br");
        this.header("Connection", "keep-alive");
        try (
                CloseableHttpResponse response = client.execute(request);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
            ) {
            status = response.getStatusLine().getStatusCode();
            if (status < 200 || status >= 300) {
                log.error("[Http请求] 请求失败，状态码: {}", status);
                return this;
            }
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                handle.handle(line);
            }
        } catch (Exception e) {
            log.error("[Http请求] 异常: {}", e.getMessage(), e);
        }
        return this;
    }

}


