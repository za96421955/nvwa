package ai.nvwa.agent.start.websokect;

import ai.nvwa.agent.agent.Agent;
import ai.nvwa.agent.agent.agents.ArchitectAgent;
import ai.nvwa.agent.components.Result;
import ai.nvwa.agent.context.SpringContextHolder;
import ai.nvwa.agent.model.chat.mode.ChatRequest;
import ai.nvwa.agent.model.chat.mode.ChatResult;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * WebSocket协议处理
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Slf4j
public class DefaultWebSocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response implements Serializable {
        private static final long serialVersionUID = 6392817584893798535L;
        private int loop;
        private String reasoning;
        private String content;
        private String answer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        JSONObject request;
        try {
            request = JSONObject.parseObject(msg.text());
        } catch (Exception e) {
            log.error("[WebSocket] msg={}, 消息格式错误, {}", msg.text(), e.getMessage(), e);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(Result.fail("消息格式错误"))));
            return;
        }
        log.info("[WebSocket] request={}, 收到消息", request);

        ArchitectAgent agent = SpringContextHolder.getBean(ArchitectAgent.class);
        String clientIp = (String) ctx.channel().attr(AttributeKey.valueOf("clientIp")).get();
        String question = request.getString("question");
        Response response = new Response();
        response.setAnswer(agent.action(clientIp, question, new Agent.Process() {
            @Override
            public void assistantBefore(int loop, ChatRequest request) {}

            @Override
            public void assistant(int loop, ChatResult result) {
                log.info("思考中：" + result.getReasoning());
                log.info("回答中：" + result.getContent());
                response.setLoop(loop);
                response.setReasoning(result.getReasoning().toString());
                response.setContent(result.getContent().toString());
                ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(Result.success(response))));
            }

            @Override
            public void assistantAfter(int loop, ChatResult result) {}
        }));
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(Result.success(response))));
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        log.info("客户端连接：" + ctx.channel().id());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        log.info("客户端断开：" + ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}


