package com.rota.notification.application.port;

import com.rota.notification.application.EmailMessage;

public interface EmailSender {

    void send(EmailMessage message);
}
