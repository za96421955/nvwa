package ai.nvwa.agent.tool.datastore.milvus.structrue;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.database.request.CreateDatabaseReq;
import io.milvus.v2.service.database.request.DropDatabaseReq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Milvus集合
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
public class MilvusDatabase {

    @Autowired
    private MilvusClientV2 client;

    /**
     * @description 创建数据库
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void create(String dbName) {
        CreateDatabaseReq request = CreateDatabaseReq.builder()
                .databaseName(dbName)
                .build();
        client.createDatabase(request);
    }

    /**
     * @description 查询数据库名称列表
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<String> queryAllNames() {
        return client.listDatabases().getDatabaseNames();
    }

    /**
     * @description 检查数据库名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public boolean check(String dbName) {
        if (StringUtils.isBlank(dbName)) {
            return false;
        }
        return this.queryAllNames().contains(dbName);
    }

    /**
     * @description 删除数据库
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void drop(String dbName) {
        DropDatabaseReq request = DropDatabaseReq.builder()
                .databaseName(dbName)
                .build();
        client.dropDatabase(request);
    }

}


