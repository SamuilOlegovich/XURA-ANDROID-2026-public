package com.samuilolegovich.wallet.model.sockets.exceptions;

/** Исключение, сигнализирующее о попытке выполнить операцию с WebSocket-соединением в некорректном для неё состоянии (например, отправка команды до подключения). */
public class InvalidStateException extends Exception {
    /** Создаёт исключение без дополнительного сообщения. */
    public InvalidStateException() {
    }
}
