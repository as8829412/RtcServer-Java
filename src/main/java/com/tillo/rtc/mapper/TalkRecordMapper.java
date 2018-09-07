package com.tillo.rtc.mapper;

import com.tillo.rtc.model.TalkRecord;

public interface TalkRecordMapper {
    int deleteByPrimaryKey(Integer roomId);

    int insert(TalkRecord record);

    int insertSelective(TalkRecord record);

    TalkRecord selectByPrimaryKey(Integer roomId);

    int updateByPrimaryKeySelective(TalkRecord record);

    int updateByPrimaryKey(TalkRecord record);
}