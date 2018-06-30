package cn.timmy.logic.friend.service.impl;

import cn.timmy.common.utils.DateTimeUtils;
import cn.timmy.logic.common.Result;
import cn.timmy.logic.common.ResultCode;
import cn.timmy.logic.friend.bean.model.FriendModel;
import cn.timmy.logic.friend.bean.model.FriendRequestModel;
import cn.timmy.logic.friend.bean.model.FriendRequestMsgModel;
import cn.timmy.logic.friend.bean.model.FriendStateEnum;
import cn.timmy.logic.friend.bean.param.FriendRequestParam;
import cn.timmy.logic.friend.bean.param.FriendUpdateParam;
import cn.timmy.logic.friend.bean.param.FriendsOutParam;
import cn.timmy.logic.friend.bean.param.FriendsOutParam.FriendInfo;
import cn.timmy.logic.friend.bean.param.RequestInfoOutParam;
import cn.timmy.logic.friend.bean.param.RequestInfoOutParam.RequestInfo;
import cn.timmy.logic.friend.bean.param.RequestInfoOutParam.RequestMsgInfo;
import cn.timmy.logic.friend.bean.param.RequestInfoParam;
import cn.timmy.logic.friend.bean.param.RequestReplyParam;
import cn.timmy.logic.friend.mapper.FriendMapper;
import cn.timmy.logic.friend.mapper.FriendRequestMapper;
import cn.timmy.logic.friend.mapper.FriendRequestMsgMapper;
import cn.timmy.logic.friend.service.FriendService;
import cn.timmy.logic.security.SecurityUtils;
import cn.timmy.logic.user.bean.model.UserModel;
import cn.timmy.logic.user.service.UserService;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

@Service
@Transactional
public class FriendServiceImpl implements FriendService {

    private static final Logger logger = LogManager.getLogger(
        FriendServiceImpl.class);

    @Lazy
    @Autowired
    private UserService userService;

    @Autowired
    private FriendMapper friendMapper;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private FriendRequestMsgMapper friendRequestMsgMapper;

    @Override
    public Result friendRequest(FriendRequestParam param) {
        UserModel user = userService.getUserByUid(param.getTo_uid());
        if (null == user) {
            return Result.failure(ResultCode.ERROR, "user not found");
        }
        if (user.getUid().equals(SecurityUtils.getUid())) {
            return Result.failure(ResultCode.ERROR, "can not add self");
        }
        Example example = new Example(FriendRequestModel.class);
        example.createCriteria().andEqualTo("from_uid", SecurityUtils.getUid())
            .andEqualTo("to_uid", param.getTo_uid());
        FriendRequestModel requestModel = friendRequestMapper.selectOneByExample(example);
        if (null != requestModel) {
            return Result.success();
        }
        FriendRequestModel model = new FriendRequestModel();
        model.setFrom_uid(SecurityUtils.getUid());
        model.setTo_uid(param.getTo_uid());
        model.setCreate_dt(DateTimeUtils.currentUTC());
        model.setUpdate_dt(DateTimeUtils.currentUTC());
        friendRequestMapper.insertSelective(model);
        if (!StringUtils.isEmpty(param.getMessage())) {
            FriendRequestMsgModel msgModel = new FriendRequestMsgModel();
            msgModel.setFrom_uid(SecurityUtils.getUid());
            msgModel.setTo_uid(param.getTo_uid());
            msgModel.setMessage(param.getMessage());
            msgModel.setCreate_dt(DateTimeUtils.currentUTC());
            msgModel.setUpdate_dt(DateTimeUtils.currentUTC());
            msgModel.setRequest_id(model.getId());
            friendRequestMsgMapper.insertSelective(msgModel);
        }
        return Result.success();
    }

    @Override
    public Result acceptRequest(Integer requestId) {
        FriendRequestModel requestModel = friendRequestMapper.selectByPrimaryKey(requestId);
        if (null == requestModel) {
            return Result.failure(ResultCode.ERROR, "friend request not found");
        }
        FriendModel friendModel = new FriendModel();
        friendModel.setUid(requestModel.getFrom_uid());
        friendModel.setFriend_uid(requestModel.getTo_uid());
        friendModel.setCreate_dt(DateTimeUtils.currentUTC());
        friendModel.setUpdate_dt(DateTimeUtils.currentUTC());
        friendModel.setState(FriendStateEnum.NORMAL.getState());
        friendMapper.insert(friendModel);
        friendModel.setUid(requestModel.getTo_uid());
        friendModel.setFriend_uid(requestModel.getFrom_uid());
        friendMapper.insert(friendModel);
        Example example = new Example(FriendRequestMsgModel.class);
        example.createCriteria().andEqualTo("request_id", requestId);
        friendRequestMsgMapper.deleteByExample(example);
        friendRequestMapper.deleteByPrimaryKey(requestId);
        return null;
    }

    @Override
    public Result replytRequest(RequestReplyParam param) {
        FriendRequestModel requestModel = friendRequestMapper.selectByPrimaryKey(param.getId());
        if (null == requestModel) {
            return Result.failure(ResultCode.ERROR, "friend request not found");
        }
        if (StringUtils.isEmpty(param.getMessage())) {
            return Result.failure(ResultCode.ERROR, "message can not be null");
        }
        FriendRequestMsgModel msgModel = new FriendRequestMsgModel();
        msgModel.setRequest_id(requestModel.getId());
        msgModel.setFrom_uid(requestModel.getFrom_uid());
        msgModel.setTo_uid(requestModel.getTo_uid());
        msgModel.setMessage(param.getMessage());
        msgModel.setCreate_dt(DateTimeUtils.currentUTC());
        msgModel.setUpdate_dt(DateTimeUtils.currentUTC());
        friendRequestMsgMapper.insertSelective(msgModel);
        requestModel.setUpdate_dt(DateTimeUtils.currentUTC());
        friendRequestMapper.updateByPrimaryKeySelective(requestModel);
        return null;
    }

    @Override
    public Result getRequestsInfo(RequestInfoParam param) {
        if (param.getIds().isEmpty()) {
            return Result.failure(ResultCode.ERROR, "id list can not be empty");
        }
        Example example = new Example(FriendRequestModel.class);
        example.createCriteria().andIn("id", param.getIds());
        example.setOrderByClause("create_dt DESC");
        List<FriendRequestModel> requestModels = friendRequestMapper.selectByExample(example);
        if (requestModels.isEmpty()) {
            return Result.failure(ResultCode.ERROR, "no friend request");
        }
        RequestInfoOutParam outParam = new RequestInfoOutParam();
        requestModels.forEach(requestModel -> {
            RequestInfo requestInfo = new RequestInfo();
            requestInfo.create_dt = requestModel.getCreate_dt();
            requestInfo.update_dt = requestModel.getUpdate_dt();
            requestInfo.from_uid = requestModel.getFrom_uid();
            requestInfo.id = requestModel.getId();
            UserModel user = userService.getUserByUid(requestModel.getFrom_uid());
            requestInfo.name = user.getName();
            requestInfo.portrait_url = user.getPortrait_url();
            Example msgExample = new Example(FriendRequestMsgModel.class);
            msgExample.createCriteria().andEqualTo("request_id", requestModel.getId());
            msgExample.setOrderByClause("create_dt ASC");
            List<FriendRequestMsgModel> msgModels = friendRequestMsgMapper
                .selectByExample(msgExample);
            msgModels.forEach(msgModel -> {
                RequestMsgInfo msgInfo = new RequestMsgInfo();
                msgInfo.create_dt = msgModel.getCreate_dt();
                msgInfo.from_uid = msgModel.getFrom_uid();
                msgInfo.message = msgModel.getMessage();
                msgInfo.to_uid = msgModel.getTo_uid();
                requestInfo.msgInfos.add(msgInfo);
            });
            outParam.getRequestInfos().add(requestInfo);
        });
        return Result.success(outParam);
    }

    @Override
    public Result getAllFriends() {
        Example example = new Example(FriendModel.class);
        example.createCriteria().andEqualTo("uid", SecurityUtils.getUid());
        example.setOrderByClause("alias ASC");
        List<FriendModel> friendModels = friendMapper.selectByExample(example);
        FriendsOutParam outParam = new FriendsOutParam();
        friendModels.forEach(friendModel -> {
            FriendInfo info = new FriendInfo();
            info.alias = friendModel.getAlias();
            info.state = friendModel.getState();
            info.uid = friendModel.getFriend_uid();
            info.portrait_url = userService.getUserByUid(friendModel.getFriend_uid())
                .getPortrait_url();
            info.create_dt = friendModel.getCreate_dt();
            info.update_dt = friendModel.getUpdate_dt();
            outParam.getFriends().add(info);
        });
        return Result.success(outParam);
    }

    @Override
    public Result getFriend(String uid) {
        Example example = new Example(FriendModel.class);
        example.createCriteria().andEqualTo("uid", SecurityUtils.getUid())
            .andEqualTo("friend_uid", uid);
        FriendModel friendModel = friendMapper.selectOneByExample(example);
        FriendInfo outParam = new FriendInfo();
        outParam.portrait_url = userService.getUserByUid(friendModel.getFriend_uid())
            .getPortrait_url();
        outParam.uid = friendModel.getFriend_uid();
        outParam.state = friendModel.getState();
        outParam.alias = friendModel.getAlias();
        outParam.create_dt = friendModel.getCreate_dt();
        outParam.update_dt = friendModel.getUpdate_dt();
        return Result.success(outParam);
    }

    @Override
    public Result deleteFriend(String uid) {
        Example example = new Example(FriendModel.class);
        example.createCriteria().andEqualTo("uid", SecurityUtils.getUid())
            .andEqualTo("friend_uid", uid);
        friendMapper.deleteByExample(example);
        return Result.success();
    }

    @Override
    public Result updateFriend(FriendUpdateParam param) {
        if (StringUtils.isEmpty(param.getAlias())) {
            return Result.failure(ResultCode.ERROR, "alias can not be null");
        }
        Example example = new Example(FriendModel.class);
        example.createCriteria().andEqualTo("uid", SecurityUtils.getUid())
            .andEqualTo("friend_uid", param.getUid());
        FriendModel friendModel = friendMapper.selectOneByExample(example);
        if (null == friendModel) {
            return Result.failure(ResultCode.ERROR, "friend not found");
        }
        if (!StringUtils.isEmpty(param.getAlias())) {
            friendModel.setAlias(param.getAlias());
        }
        if (null != param.getState()) {
            friendModel.setState(param.getState());
        }
        friendModel.setUpdate_dt(DateTimeUtils.currentUTC());
        friendMapper.updateByPrimaryKeySelective(friendModel);
        return Result.success();
    }

    @Override
    public boolean isFriend(String uid) {
        Example example = new Example(FriendModel.class);
        example.createCriteria().andEqualTo("uid", SecurityUtils.getUid())
            .andEqualTo("friend_uid", uid);
        FriendModel friendModel = friendMapper.selectOneByExample(example);
        return null != friendModel;
    }
}
