package ai.nvwa.tool.datastore.milvus.structrue;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.utility.request.AlterAliasReq;
import io.milvus.v2.service.utility.request.CreateAliasReq;
import io.milvus.v2.service.utility.request.DescribeAliasReq;
import io.milvus.v2.service.utility.request.ListAliasesReq;
import io.milvus.v2.service.utility.response.DescribeAliasResp;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Milvus集合别名
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
public class MilvusAlias {

    @Autowired
    private MilvusClientV2 client;
//    @Autowired
//    private MilvusCollection collection;

    /**
     * @description 创建别名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void create(String collectionName, String alias) {
//        if (StringUtils.isBlank(alias)) {
//            return;
//        }
//        if (!collection.check(collectionName)) {
//            return;
//        }
        // 9. Manage aliases
        // 9.1 Create alias
        CreateAliasReq createAliasReq = CreateAliasReq.builder()
                .collectionName(collectionName)
                .alias(alias)
                .build();
        client.createAlias(createAliasReq);
    }

    /**
     * @description 更改别名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public void alter(String collectionName, String newAlias) {
//        if (StringUtils.isBlank(newAlias)) {
//            return;
//        }
//        if (!collection.check(collectionName)) {
//            return;
//        }
        // 9.4 Reassign alias to other collections
        AlterAliasReq alterAliasReq = AlterAliasReq.builder()
                .collectionName(collectionName)
                .alias(newAlias)
                .build();
        client.alterAlias(alterAliasReq);
    }

    /**
     * @description 查询别名列表
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public List<String> queryAllNames(String collectionName) {
//        if (!collection.check(collectionName)) {
//            return Collections.emptyList();
//        }
        // 9.2 List alises
        ListAliasesReq listAliasesReq = ListAliasesReq.builder()
                .collectionName(collectionName)
                .build();
        return client.listAliases(listAliasesReq).getAlias();
    }

    /**
     * @description 查询指定别名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public DescribeAliasResp queryAlias(String alias) {
        if (StringUtils.isBlank(alias)) {
            return null;
        }
        // 9.3 Describe alias
        DescribeAliasReq describeAliasReq = DescribeAliasReq.builder()
                .alias(alias)
                .build();
        return client.describeAlias(describeAliasReq);
    }

}


