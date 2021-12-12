package com.ssk.binlog.config;

import com.ssk.binlog.event.handler.IEventHandler;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/**
 * 同步机器
 * @author 惊云
 * @date 2021/12/12 10:14
 */
public class SyncConfig {
    String host;
    Integer port;
    String userName;
    String password;
    List<IEventHandler> eventHandlerList = new ArrayList<>();
    public SyncConfig() {
    }

    public SyncConfig(String host, Integer port, String userName, String password) {
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<IEventHandler> getEventHandlerList() {
        return eventHandlerList;
    }

    public void setEventHandlerList(List<IEventHandler> eventHandlerList) {
        this.eventHandlerList = eventHandlerList;
    }

    public void addEventHandlerList(IEventHandler eventHandler) {
        eventHandlerList.add(eventHandler);
    }

    @Override
    public String toString() {
        return "SyncConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

}
