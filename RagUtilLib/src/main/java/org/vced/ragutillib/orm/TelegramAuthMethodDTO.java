package org.vced.ragutillib.orm;

public record TelegramAuthMethodDTO(AuthMethod authMethod, String botId){
    public enum AuthMethod {
        LDAP,
        TELEGRAM_WIDGET
    }
}
