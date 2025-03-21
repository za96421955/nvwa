package ai.nvwa.tool.extension.extensions;

import ai.nvwa.tool.datastore.extract.Extractor;
import ai.nvwa.tool.extension.Extension;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 扩展: 百度百科
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component("baikeExtension")
public class BaikeExtension implements Extension {

    @Autowired
    private Extractor extractor;

    @Override
    public String action() {
        return "baikeExtension";
    }

    @Override
    public String name() {
        return "百度百科";
    }

    @Override
    public String desc() {
        return "中国最大在线百科平台，百度2006年创建，专业审核与用户共创确保权威性，涵盖多领域知识，支持多媒体及20余种语言，日均访问量超亿次。";
    }

    @Override
    public JSONObject inputFormat() {
        JSONObject input = new JSONObject();
        input.put("wd", "XXXXXX");
        return input;
    }

    @Override
    public String call(JSONObject input) {
        String url = "https://baike.baidu.com/item/{wd}?fromModule=lemma_search-box"
                .replaceFirst("\\{wd\\}", (String) input.get("wd"));
        return extractor.getAbstract(extractor.urlExtractor(url), 1024,0,1);
    }

    public static void main(String[] args) {
        JSONObject input = new JSONObject();
        input.put("wd", "曾国藩");
        String url = "https://baike.baidu.com/item/{wd}?fromModule=lemma_search-box"
                .replaceFirst("\\{wd\\}", (String) input.get("wd"));

        Extractor extractor = new Extractor();
        String content = extractor.urlExtractor(url);
        String abs = extractor.getAbstract(content, 1024,128,5);
        System.out.println(abs.length());
        abs = extractor.getAbstract(content, 1024,64,3);
        System.out.println(abs.length());
        abs = extractor.getAbstract(content, 1024,0,3);
        System.out.println(abs.length());
        abs = extractor.getAbstract(content, 1024,0,1);
        System.out.println(abs.length());
        System.out.println(abs);
    }

}


