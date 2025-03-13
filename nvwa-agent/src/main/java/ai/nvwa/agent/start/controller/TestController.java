package ai.nvwa.agent.start.controller;

import ai.nvwa.agent.tool.datastore.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description 测试
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@RestController
@RequestMapping("/nvwa/test")
@Slf4j
public class TestController {

    @Autowired
    private Document document;

    @PostMapping("/insert")
    public Object insert(String title, String content) {
        return document.insert(title, content);
    }

    @PostMapping("/queryByTitle")
    public Object queryByTitle(String title) {
        return document.queryByTitle(title);
    }

    @PostMapping("/queryByContent")
    public Object queryByContent(String content) {
        return document.queryByContent(content);
    }

}


