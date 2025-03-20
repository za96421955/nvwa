package ai.nvwa.agent.start.websokect;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 通道初始化
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Slf4j
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    /** 连接地址 */
    private final String path;
    /** 处理类 */
    private final SimpleChannelInboundHandler<?> handler;

    public WebSocketChannelInitializer(String path, SimpleChannelInboundHandler<?> handler) {
        super();
        this.path = path;
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        // 1. HTTP编解码器（处理协议升级）
        // * HttpServerCodec 必须在 WebSocketServerProtocolHandler 之前
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpHeadersHandler());
        // 2. 分块传输处理器（支持大文件传输）
        pipeline.addLast(new ChunkedWriteHandler());
        // 3. 心跳检查（读空闲60秒触发）
        pipeline.addLast(new IdleStateHandler(60, 30, 0));
        pipeline.addLast(new HeartbeatHandler());
        // 4. WebSocket协议处理器（握手、帧解析）
        pipeline.addLast(new WebSocketServerProtocolHandler(path));
        // 5. 业务处理器
        pipeline.addLast(handler);
    }

}


