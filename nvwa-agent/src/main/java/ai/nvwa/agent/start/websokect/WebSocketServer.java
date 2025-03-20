package ai.nvwa.agent.start.websokect;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * WebSocket服务
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Component
@Slf4j
public class WebSocketServer {

    @Value("${websocket.port:0}")
    private int port;
    @Value("${websocket.path:null}")
    private String path;
    @Value("${websocket.handler:null}")
    private String handler;

    @PostConstruct
    public void start() throws Exception {
        this.run();
    }

    /**
     * @description 启动WebSocket服务
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private void run() throws Exception {
        if (port <= 0) {
            return;
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketChannelInitializer(path, this.instanceHandler()));
            ChannelFuture f = b.bind(port).sync();
            log.info("WebSocket服务已启动，监听端口：" + port);
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * @description 实例化处理器
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    private SimpleChannelInboundHandler<?> instanceHandler() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        // 1. 获取 Class 对象
        Class<?> clazz = Class.forName(handler);
        // 2. 获取构造方法（假设存在无参构造）
        Constructor<?> constructor = clazz.getDeclaredConstructor();
        // 3. 设置可访问性（可选）
        constructor.setAccessible(true);
        // 4. 实例化对象
        return (SimpleChannelInboundHandler<?>) constructor.newInstance();
//        try {
//        } catch (Exception e) {
//            log.error("{} 实例化异常, {}", handler, e.getMessage(), e);
//        }
//        return null;
    }

}


