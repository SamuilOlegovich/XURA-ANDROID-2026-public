package com.samuilolegovich.wallet.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.JsonRpcRequest;
import org.xrpl.xrpl4j.model.client.XrplResult;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;



/**
 * HTTP-клиент для JSON-RPC вызовов к XRPL-ноде: сериализует запрос, отправляет
 * его через OkHttp (с доверием всем сертификатам в testnet или с пиннингом
 * сертификата в mainnet — выбор делает {@link SslUtil}), разбирает ответ и
 * преобразует JSON-ошибку сервера в исключение.
 */
public class ApiClient {
    private static final Logger LOG = LoggerFactory.getLogger(ApiClient.class);

    private static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String OK_HTTP = "OkHttp Headers.java";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String USER_AGENT = "User-Agent";
    private static final String TYPE = "application/json";
    private static final String ACCEPT = "Accept";


    private ObjectMapper objectMapper;
    private OkHttpClient okHttpClient;
    private String url;



    /** Создаёт клиент для указанного RPC-URL, выбирая стратегию проверки SSL-сертификата в зависимости от текущей сети (testnet/mainnet). */
    public ApiClient(String url) {
        this.objectMapper = ObjectMapperFactory.create();
        this.okHttpClient = com.samuilolegovich.config.NetworkConfig.IS_TESTNET
                ? SslUtil.trustAllOkHttpClient()
                : SslUtil.pinnedOkHttpClient(SslUtil.RPC_MAINNET_HOST, SslUtil.RPC_MAINNET_PINS);
        this.url = url;
    }




    /** Отправляет JSON-RPC запрос и десериализует результат в указанный класс. */
    public  <T extends XrplResult> T send(JsonRpcRequest request, Class<T> resultType) throws JsonRpcClientErrorException {
        JavaType javaType = objectMapper.constructType(resultType);
        return send(request, javaType);
    }

    /** Отправляет JSON-RPC запрос и десериализует результат по обобщённому JavaType (для параметризованных типов результата). */
    public  <T extends XrplResult> T send(JsonRpcRequest request, JavaType resultType) throws JsonRpcClientErrorException {
        JsonNode response = postRpcRequest(request);
        JsonNode result = response.get("result");
        checkForError(response);

        try {
            return objectMapper.readValue(result.toString(), resultType);
        } catch (JsonProcessingException e) {
            throw new JsonRpcClientErrorException(e);
        }
    }



    /** Сериализует запрос в JSON, выполняет POST-запрос к ноде через OkHttp и разбирает тело ответа в JsonNode. */
    private JsonNode postRpcRequest(JsonRpcRequest rpcRequest) throws JsonRpcClientErrorException {
        String rpcRequestString = null;

        try {
            rpcRequestString = objectMapper.writeValueAsString(rpcRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(rpcRequestString, MEDIA_TYPE);
        Request request = new Request.Builder()
                .header(USER_AGENT, OK_HTTP)
                .addHeader(CONTENT_TYPE, TYPE)
                .addHeader(ACCEPT, TYPE)
                .post(body)
                .url(url)
                .build();

        try (okhttp3.Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) { throw new IOException("Unexpected code " + response); }
            String stringResponseBody = Objects.requireNonNull(Objects.requireNonNull(response.body()).string());
            JsonNode responseNode = objectMapper.readTree(stringResponseBody);
            return responseNode;
        } catch (IOException e) {
            throw new JsonRpcClientErrorException(e);
        }
    }

    /** Проверяет JSON-ответ на наличие поля ошибки и выбрасывает исключение с текстом ошибки сервера, если оно найдено. */
    private void checkForError(JsonNode response) throws JsonRpcClientErrorException {
        if (response.has("result")) {
            JsonNode result = response.get("result");
            if (result.has("error")) {
                String errorMessage = Optional.ofNullable(result.get("error_exception"))
                        .map(JsonNode::asText)
                        .orElseGet(() -> result.get("error_message").asText());
                throw new JsonRpcClientErrorException(errorMessage);
            }
        }
    }

}
