package ai.nvwa.agent.tool.datastore.milvus;

import ai.nvwa.agent.tool.datastore.milvus.search.MilvusSearch;
import ai.nvwa.agent.tool.datastore.milvus.structrue.*;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Milvus
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 * @version 1.0
 * @date 2025/3/12
 */
@Component
public class MilvusTemplate {

    @Autowired
    private MilvusClientV2 client;
    @Autowired
    private MilvusDatabase database;
    @Autowired
    private MilvusCollection collection;
    @Autowired
    private MilvusAlias alias;
    @Autowired
    private MilvusPartition partition;
    @Autowired
    private MilvusData data;
    @Autowired
    private MilvusSearch search;

    public MilvusClientV2 client() {
        return client;
    }

    public MilvusDatabase database() {
        return database;
    }

    public MilvusCollection collection() {
        return collection;
    }

    public MilvusAlias alias() {
        return alias;
    }

    public MilvusPartition partition() {
        return partition;
    }

    public MilvusData data() {
        return data;
    }

    public MilvusSearch search() {
        return search;
    }

}


