package ai.nvwa.tool.datastore.milvus;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Milvus客户端
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public abstract class MilvusClient {
//    public static final String CLUSTER_ENDPOINT = "http://localhost:19530";
//    public static final String TOKEN = "root:Milvus";

    private MilvusClient() {}

    public static MilvusClientV2 connect(String uri, String token, String dbName) {
        ConnectConfig config = ConnectConfig.builder()
                .uri(uri)
                .token(token)
                .dbName(dbName)
                .build();
        return new MilvusClientV2(config);
    }

    public static MilvusClientV2 connect(String uri) {
        return connect(uri, null, null);
    }

    public static MilvusClientV2 connect(String uri, String dbName) {
        return connect(uri, null, dbName);
    }

    public static MilvusClientV2 connect(String host, int port, String username, String pwd, String dbName) {
        return connect("http://" + host + ":" + port, username + ":" + pwd, dbName);
    }

}


