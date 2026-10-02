package com.rota.notification.application;

public record EmailMessage(String to, String subject, String body) {
}
