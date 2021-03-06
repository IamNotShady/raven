package com.raven.client.group;

import com.raven.client.common.Utils;
import com.raven.client.group.bean.GroupOutParam;
import com.raven.common.protos.Message;
import com.raven.common.utils.SnowFlake;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.Arrays;
import java.util.List;

/**
 * Author zxx Description Simple client for module test Date Created on 2018/5/25
 */
public class ClientOwner {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 7010;
    public static SnowFlake snowFlake = new SnowFlake(1, 2);

    public static void main(String[] args) throws Exception {
        List<String> members = Arrays.asList("owner", "invitee1", "invitee2");
        GroupOutParam groupInfo = Utils.newGroup(members);
        loginAndSendMessage(groupInfo.getGroupId());
    }

    private static void loginAndSendMessage(String groupId) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new IdleStateHandler(10, 10, 15));
                    pipeline.addLast(new ProtobufVarint32FrameDecoder());
                    pipeline
                        .addLast(new ProtobufDecoder(Message.RavenMessage.getDefaultInstance()));
                    // 对protobuf协议的消息头上加上一个长度为32的整形字段
                    pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                    pipeline.addLast(new ProtobufEncoder());
                    pipeline.addLast(new ClientOwnerHandler("owner", groupId));
                }
            });
        b.connect(HOST, PORT);
    }

}

