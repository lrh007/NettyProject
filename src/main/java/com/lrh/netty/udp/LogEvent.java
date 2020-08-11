package com.lrh.netty.udp;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * 日志实体类
 *
 * @Author lrh 2020/8/11 10:00
 */
public class LogEvent {
    public static final byte SEPARATOR = (byte)'|';
    private InetSocketAddress source;
    private String logfile;
    private String msg;
    private long received;

    public LogEvent(String logfile,String msg){
        this(null,-1,logfile,msg);
    }
    public LogEvent(InetSocketAddress source,long received,String logfile,String msg){
        this.source = source;
        this.logfile = logfile;
        this.msg = msg;
        this.received = received;
    }

    public InetSocketAddress getSource() {
        return source;
    }

    public String getLogfile() {
        return logfile;
    }

    public String getMsg() {
        return msg;
    }

    public long getReceived() {
        return received;
    }
}
