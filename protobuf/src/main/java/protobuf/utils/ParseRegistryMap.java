package protobuf.utils;

import protobuf.protos.Auth;
import protobuf.protos.PrivateMessageProto;

/**
 * Author zxx
 * Description 注册消息协议号与消息转化方法和类型
 * Date Created on 2018/5/12
 */
public class ParseRegistryMap {

    public static void initRegistry() {
        ParseMap.register(ProtoConstants.LOGIN, Auth.Login::parseFrom, Auth.Login.class);
        ParseMap.register(ProtoConstants.RESPONSE, Auth.Response::parseFrom, Auth.Response.class);
        ParseMap.register(ProtoConstants.UPPRIVATEMESSAGE,
                PrivateMessageProto.UpStreamMessageProto::parseFrom,
                PrivateMessageProto.UpStreamMessageProto.class);
        ParseMap.register(ProtoConstants.DOWNPRIVATEMESSAGE,
                PrivateMessageProto.DownStreamMessageProto::parseFrom,
                PrivateMessageProto.DownStreamMessageProto.class);
    }
}
