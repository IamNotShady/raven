package com.tim.access.handler.server;

import com.tim.access.util.IpUtil;
import com.tim.common.loadbalance.Server;
import com.tim.common.netty.IdChannelManager;
import com.tim.common.netty.NettyAttrUtil;
import com.tim.common.protos.Message.Code;
import com.tim.common.protos.Message.Login;
import com.tim.common.protos.Message.LoginAck;
import com.tim.common.protos.Message.TimMessage;
import com.tim.common.protos.Message.TimMessage.Type;
import com.tim.common.utils.Constants;
import com.tim.storage.route.RouteManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.SocketException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Sharable
@Slf4j
public class LoginAuthHandler extends SimpleChannelInboundHandler<TimMessage> {

    @Autowired
    private IdChannelManager uidChannelManager;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RouteManager routeManager;

    @Value("${netty.tcp.port}")
    private int nettyTcpPort;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("client connected remote address:{}", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TimMessage message) throws Exception {
        if (message.getType() == Type.Login) {
            Login loginMesaage = message.getLogin();
            log.info("login msg:{}", loginMesaage);
//            String token = loginMesaage.getToken();
//            if (!verifyToken(token)) {
//                LoginAck loginAck = LoginAck.newBuilder()
//                    .setId(loginMesaage.getId())
//                    .setCode(Code.FAIL)
//                    .setTime(System.currentTimeMillis())
//                    .build();
//                ctx.writeAndFlush(loginAck);
//            }
            routeManager.addUser2Server(loginMesaage.getUid(), getLocalServer());
            uidChannelManager.addId2Channel(loginMesaage.getUid(), ctx.channel());
            sendLoginAck(ctx, loginMesaage.getId(), Code.SUCCESS);
        } else {
            if (null == uidChannelManager.getIdByChannel(ctx.channel())) {
                ctx.close();
            }
            NettyAttrUtil
                .updateReaderTime(ctx.channel(), System.currentTimeMillis());
            ctx.fireChannelRead(message);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String uid = uidChannelManager.getIdByChannel(ctx.channel());
        if (null != uid) {
            log.info("client disconnected uid:{}", uid);
            uidChannelManager.removeChannel(ctx.channel());
            // 最后一台设备下线才清除路由
            if (CollectionUtils.isEmpty(uidChannelManager.getChannelsById(uid))) {
                routeManager.removerUserFromServer(uid, getLocalServer());
            }
        }
    }

    private Server getLocalServer() {
        return new Server(IpUtil.getIp(), nettyTcpPort);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if ("Connection reset by peer".equals(cause.getMessage())) {
            return;
        }
        log.error(cause.getMessage(), cause);
    }

    private boolean verifyToken(String token) {
        return redisTemplate.hasKey(token);
    }

    private void sendLoginAck(ChannelHandlerContext ctx, long id, Code code) {
        LoginAck loginAck = LoginAck.newBuilder()
            .setId(id)
            .setCode(code)
            .setTime(System.currentTimeMillis())
            .build();
        TimMessage timMessage = TimMessage.newBuilder().setType(Type.LoginAck)
            .setLoginAck(loginAck).build();
        ctx.writeAndFlush(timMessage);
    }


}

