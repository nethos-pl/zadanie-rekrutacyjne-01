### Wymagania przed rozpoczęciem pracy nad projektem

1. Zainstalowane JDK w wersji 8 lub wyższej.
1. Apache Maven w wersji 3.5 lub wyższej. Instalacja sprowadza się do rozpakowania zipa. Istotnym plikiem nas interesującym jest mvn.cmd - przyda się później. (https://maven.apache.org/download.cgi#)
1. Ustawiona zmienna środowiskowa JAVA_HOME na wartość katalogu w którym zainstalowana została java z punktu 1 (https://confluence.atlassian.com/doc/setting-the-java_home-variable-in-windows-8895.html)
1. Instalacja Nodejs w wersji 10+ (https://nodejs.org/en/)

### Wykorzystane w projekcie technologie

1. Java 8
1. Vaadin 14
1. Spring Boot 2.1
1. Baza H2 Embedded w trybie in-memory

Do projektu możesz dodawać dowolne inne technologie, które miałyby Tobie pomóc w rozwiązaniu zadań.

### Uruchomienie aplikacji

1. Uruchom aplikację poleceniem `mvn spring-boot:run` lub z poziomu Twojego IDE uruchamiając klasę Application. 
1. Przechodziny na adres http://localhost:8080/ w przeglądarce

### Dostęp do bazy danych

1. Po uruchomieniu aplikacji dostęp do konsoli bazy danych dostępny będzie pod adresem http://127.0.0.1:8080/h2-console
1. Parametry połączenia
    - Driver Class: `org.h2.Driver`
    - JDBC URL: `jdbc:h2:mem:testdb`
    - User Name: `sa`
    - Pole Password zostawić puste
