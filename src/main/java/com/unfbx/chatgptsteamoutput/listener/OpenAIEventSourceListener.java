package com.unfbx.chatgptsteamoutput.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unfbx.chatgpt.entity.completions.CompletionResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

/**
 * 描述：OpenAIEventSourceListener
 *
 * @author https:www.unfbx.com
 * @date 2023-02-22
 */
@Slf4j
public class OpenAIEventSourceListener extends EventSourceListener {

    private SseEmitter sseEmitter;

    public OpenAIEventSourceListener(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("OpenAI建立sse连接...");
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.info("OpenAI返回数据：{}", data);
        if (data.equals("[DONE]")) {
            log.info("OpenAI返回数据结束了");
            sseEmitter.send(SseEmitter.event()
                    .id("[DONE]")
                    .data("[DONE]")
                    .reconnectTime(3000));
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        CompletionResponse completionResponse = mapper.readValue(data, CompletionResponse.class); // 读取Json
        sseEmitter.send(SseEmitter.event()
                .id(completionResponse.getId())
                .data(completionResponse.getChoices()[0].getText())
                .reconnectTime(3000));
    }


    @Override
    public void onClosed(EventSource eventSource) {
        log.info("OpenAI关闭sse连接...");
    }


    @SneakyThrows
    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        if(Objects.isNull(response)){
            return;
        }
        ResponseBody body = response.body();
        if (Objects.nonNull(body)) {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", body.string(), t);
        } else {
            log.error("OpenAI  sse连接异常data：{}，异常：{}", response, t);
        }
        eventSource.cancel();
    }
}
