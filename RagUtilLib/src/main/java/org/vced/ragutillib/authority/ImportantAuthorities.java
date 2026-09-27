package org.vced.ragutillib.authority;

import java.util.Arrays;

/**
 Обращение к индексу в БД через метод getId() (порядковый номер экземпляра в enum, начиная с 1)
 Получить название роли через toString()
 **/
public enum ImportantAuthorities {
    DOMAIN_USERS("Domain Users"),
    UNLIMITED_VISIBILITY("UnlimitedVisibility"),
    ADMIN_ROLE("ElectronHelper - admins");
    private final String name;
    private final static String[] idValues = Arrays
            .stream(values())
            .map(ImportantAuthorities::getId)
            .toArray(String[]::new);

    ImportantAuthorities(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name;
    }

    public String getId() {
        return String.valueOf(ordinal() + 1); // в БД отсчёт с 1; тип String как в AuthenticationDataORM
    }

    // то же, что и values() в Enum, но возвращает список Id
    public static String[] idValues() {
        return idValues;
    }
}
