package com.chukong.apwebauthentication.dns;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import android.util.Log;

/**
 * @author yihua.huang@dianping.com <br>
 * @date: 13-7-14 <br>
 * Time: 下午4:36 <br>
 */
public class AnswerHandler {

    // b._dns-sd._udp.0.129.37.10.in-addr.arpa.
    private final Pattern filterPTRPattern = Pattern
            .compile(".*\\.(\\d+\\.\\d+\\.\\d+\\.\\d+\\.in-addr\\.arpa\\.)");

    private String filterPTRQuery(String query) {
        Matcher matcher = filterPTRPattern.matcher(query);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return query;
        }
    }

    public boolean handle(MessageWrapper request, MessageWrapper response) {
        Record question = request.getMessage().getQuestion();
        String query = question.getName().toString();
        int type = question.getType();
        if (type == Type.PTR) {
            query = filterPTRQuery(query);
        }
        // some client will query with any
        if (type == Type.ANY) {
            type = Type.A;
        }
        String answer = "192.168.23.52";
        if (type == Type.AAAA) {
            try {
                answer = Inet6Address.getByName("192.168.23.52").getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        Log.d("taugin1", "type = " + type + " , answer = " + answer);
        if (answer != null) {
            try {
                /*
                Log.d("taugin1", "query = " + query);
                Log.d("taugin1", "question.getDClass() = " + question.getDClass());
                Log.d("taugin1", "question.getName() = " + question.getName());
                Log.d("taugin1", "answer = " + answer);
                Log.d("taugin1", "type = " + type);
                */
                response.getMessage().addRecord(question, Section.QUESTION);
                RecordBuilder builder = new RecordBuilder();
                builder.dclass(question.getDClass());
                builder.name(question.getName());
                builder.answer(answer);
                builder.type(/*type*/1);
                Record record = builder.toRecord();
                response.getMessage().addRecord(record, Section.ANSWER);
                //Log.d("taugin1", "answer\t" + Type.string(type) + "\t"
                //            + DClass.string(question.getDClass()) + "\t"
                //            + answer + "\n");
                response.setHasRecord(true);
                Log.d("taugin1", "res = " + response.getMessage().toString());
                return false;
            } catch (Exception e) {
                Log.d("taugin1", "AnswerHandler handling exception " + e.getMessage());
                e.printStackTrace();
            }
        }
        return true;
    }
}