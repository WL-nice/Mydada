package com.wanglei.Mydadabackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wanglei.Mydadabackend.model.domain.UserAnswer;
import com.wanglei.Mydadabackend.model.dto.UserAnswerCountDTO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author admin
* @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
* @createDate 2024-05-28 16:18:55
* @Entity com.wanglei.Mydadabackend.model.domain.UserAnswer
*/
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    @Select("select appId, count(appId) as answerCount from user_answer group by appId order by answerCount desc limit 10;")
    List<UserAnswerCountDTO> getUserAnswerCount();



}




