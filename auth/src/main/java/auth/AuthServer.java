package auth;
/**
 * Created by Qzy on 2016/1/28.
 */

import auth.handler.AuthServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.InetSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.ParseRegistryMap;
import protobuf.code.MessageDecoder;
import protobuf.code.MessageEncoder;


public class AuthServer {

    private static final Logger logger = LoggerFactory.getLogger(AuthServer.class);

    public static void startAuthServer(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("MessageDecoder", new MessageDecoder());
                        pipeline.addLast("MessageEncoder", new MessageEncoder());
                        pipeline.addLast("AuthServerHandler", new AuthServerHandler());
                    }
                });
        bindConnectionOptions(bootstrap);
        bootstrap.bind(new InetSocketAddress(port)).addListener(future -> {
            if (future.isSuccess()) {
                //init registry
                ParseRegistryMap.initRegistry();
                HandlerManager.initHandlers();
                logger.info("AuthServer Started Success, port:{}", port);
            } else {
                logger.error("AuthServer Started Failed");
            }
        });
    }

    private static void bindConnectionOptions(ServerBootstrap bootstrap) {
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.childOption(ChannelOption.SO_LINGER, 0);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true); //调试用
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); //心跳机制暂时使用TCP选项，之后再自己实现
    }
}
