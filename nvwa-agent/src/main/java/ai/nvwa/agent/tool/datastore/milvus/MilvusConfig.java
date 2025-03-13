package ai.nvwa.agent.tool.datastore.milvus;

import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus配置
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Configuration
public class MilvusConfig {

    @Value("${tools.datastores.milvus.uri:null}")
    private String uri;
    @Value("${tools.datastores.milvus.token:null}")
    private String token;
    @Value("${tools.datastores.milvus.dbName:null}")
    private String dbName;

    @Bean
    public MilvusClientV2 init() {
        return MilvusClient.connect(uri, token, dbName);
    }

}


