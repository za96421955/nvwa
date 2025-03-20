package ai.nvwa.agent.start.websokect;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 心跳检查
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            ctx.writeAndFlush(new PingWebSocketFrame());
        }
    }

}


