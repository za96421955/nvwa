package ai.nvwa.agent.start.controller;

import ai.nvwa.agent.arrange.Agent;
import ai.nvwa.agent.arrange.agents.BaikeAgent;
import ai.nvwa.agent.arrange.agents.WeatherAgent;
import ai.nvwa.agent.components.util.HttpClient;
import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResult;
import ai.nvwa.agent.tool.datastore.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private WeatherAgent weatherAgent;
    @Autowired
    private BaikeAgent baikeAgent;

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
        return document.queryByContent(content, 5);
    }

    @PostMapping("/queryByContents")
    public Object queryByContents(@RequestParam List<String> contents) {
        return document.queryByContents(contents);
    }

    @PostMapping("/weatherAgent")
    public Object weatherAgent(HttpServletRequest request, @RequestParam String content) {
        return weatherAgent.action(HttpClient.getRemoteAddr(request), content, new Agent.Process() {
            @Override
            public void assistantBefore(int loop, ChatRequest request) {}

            @Override
            public void reasoning(int loop, String reasoning) {
                System.out.println("思考中：" + reasoning);
            }

            @Override
            public void content(int loop, String content) {
                System.out.println("回答中：" + content);
            }

            @Override
            public void assistantAfter(int loop, ChatResult result) {}
        });
    }

    @PostMapping("/baikeAgent")
    public Object baikeAgent(HttpServletRequest request, @RequestParam String content) {
        return baikeAgent.action(HttpClient.getRemoteAddr(request), content, new Agent.Process() {
            @Override
            public void assistantBefore(int loop, ChatRequest request) {}

            @Override
            public void reasoning(int loop, String reasoning) {
                System.out.println("思考中：" + reasoning);
            }

            @Override
            public void content(int loop, String content) {
                System.out.println("回答中：" + content);
            }

            @Override
            public void assistantAfter(int loop, ChatResult result) {}
        });
    }

}


