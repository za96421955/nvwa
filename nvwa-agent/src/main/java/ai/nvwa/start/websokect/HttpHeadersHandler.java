package ai.nvwa.start.websokect;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;

/**
 * 连接头处理
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public class HttpHeadersHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof DefaultHttpRequest) {
            String clientIp = this.getRemoteAddr(ctx, (DefaultHttpRequest) msg);
            ctx.channel().attr(AttributeKey.valueOf("clientIp")).set(clientIp);
        }
        ctx.fireChannelRead(msg);
    }

    /**
     * @description 获取客户端真实IP
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public String getRemoteAddr(ChannelHandlerContext ctx, DefaultHttpRequest request) {
        String clientIp;
        // X-Forwarded-For
        String xff = request.headers().get("X-Forwarded-For");
        clientIp = (xff == null ? request.headers().get("x-forwarded-for") : xff);
        // Proxy-Client-IP
        if (StringUtils.isBlank(clientIp)) {
            String pci = request.headers().get("Proxy-Client-IP");
            clientIp = (pci == null ? request.headers().get("proxy-client-ip") : pci);
        }
        // WL-Proxy-Client-IP
        if (StringUtils.isBlank(clientIp)) {
            String wpci = request.headers().get("WL-Proxy-Client-IP");
            clientIp = (wpci == null ? request.headers().get("wl-proxy-client-ip") : wpci);
        }
        // RemoteAddr
        if (StringUtils.isBlank(clientIp)) {
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            clientIp = address.getAddress().getHostAddress();
        }
        return clientIp.split(",")[0].trim();
    }

}


