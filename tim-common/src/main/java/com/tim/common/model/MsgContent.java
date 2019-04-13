package com.tim.common.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class MsgContent implements Serializable {

    private Long id;

    private String uid;

    private int type;

    private String content;

    private Long time;


    public MsgContent setId(Long id) {
        this.id = id;
        return this;
    }

    public MsgContent setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public MsgContent setType(int type) {
        this.type = type;
        return this;
    }

    public MsgContent setContent(String content) {
        this.content = content;
        return this;
    }

    public MsgContent setTime(Long time) {
        this.time = time;
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public Long getTime() {
        return time;
    }


}
