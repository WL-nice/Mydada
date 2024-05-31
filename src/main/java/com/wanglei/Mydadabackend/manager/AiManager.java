package com.wanglei.Mydadabackend.manager;

import com.wanglei.Mydadabackend.commmon.ErrorCode;
import com.wanglei.Mydadabackend.exception.BusinessException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.ChatCompletionRequest;
import com.zhipu.oapi.service.v4.model.ChatMessage;
import com.zhipu.oapi.service.v4.model.ChatMessageRole;
import com.zhipu.oapi.service.v4.model.ModelApiResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用AI能力
 */
@Component
public class AiManager {
    @Resource
    private ClientV4 clientV4;


    /**
     * 同步请求
     *
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doSyncRequest(String systemMessage, String userMessage) {
        return doRequest(systemMessage, userMessage, Boolean.FALSE);
    }
    /**
     * 通用请求（简化消息传递）
     *
     * @param systemMessage
     * @param userMessage
     * @param stream
     * @return
     */
    public String doRequest(String systemMessage, String userMessage, Boolean stream) {
        List<ChatMessage> chatMessageList = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        chatMessageList.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        chatMessageList.add(userChatMessage);
        return doRequest(chatMessageList, stream);
    }

    /**
     * 通用请求
     * @param messages
     * @param stream
     *
     * @return
     */
    public String doRequest(List<ChatMessage> messages, Boolean stream) {
        // 构建请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM3TURBO)
                .stream(stream)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        try {
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getData().getChoices().get(0).toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}
