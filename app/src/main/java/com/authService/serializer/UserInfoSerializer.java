package com.authService.serializer;

import com.authService.models.UserInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class UserInfoSerializer implements Serializer<UserInfoDto> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        Serializer.super.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String s, UserInfoDto userInfoDto) {
        byte[] val = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            val = objectMapper.writeValueAsString(s).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return val;
    }

    @Override
    public byte[] serialize(String topic, Headers headers, UserInfoDto data) {
        return Serializer.super.serialize(topic, headers, data);
    }

    @Override
    public void close() {
        Serializer.super.close();
    }
}
