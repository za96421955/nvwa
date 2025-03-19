package ai.nvwa.agent.model;

import ai.nvwa.agent.model.chat.mode.ChatRequest;
import org.mapstruct.Mapper;

/**
 * @description 转换器
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
@Mapper(componentModel="spring")
public interface ModelAssembler {

    ChatRequest copy(ChatRequest dto);

    ChatRequest.ChatMessage copy(ChatRequest.ChatMessage dto);

}


