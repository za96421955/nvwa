package ai.nvwa.agent.tool.datastore.milvus;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;

/**
 * Milvus客户端
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public abstract class MilvusClient {
    String CLUSTER_ENDPOINT = "http://localhost:19530";
    String TOKEN = "root:Milvus";

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


