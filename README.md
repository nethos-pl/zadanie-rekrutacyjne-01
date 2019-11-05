### Wymagania przed rozpoczęciem pracy nad projektem

1. Zainstalowane JDK w wersji 8 lub wyższej.
1. Apache Maven w wersji 3.5 lub wyższej. Instalacja sprowadza się do rozpakowania zipa. Istotnym plikiem nas interesującym jest mvn.cmd - przyda się później. (https://maven.apache.org/download.cgi#)
1. Ustawiona zmienna środowiskowa JAVA_HOME na wartość katalogu w którym zainstalowana została java z punktu 1 (https://confluence.atlassian.com/doc/setting-the-java_home-variable-in-windows-8895.html)
1. Instalacja Nodejs w wersji 10+ (https://nodejs.org/en/)

### Wykorzystane w projekcie technologie

1. Java 8
1. Vaadin 14 (https://vaadin.com/)
1. Spring Boot 2.1 (https://spring.io/projects/spring-boot)
1. Baza H2 Embedded w trybie in-memory (https://www.h2database.com)

Do projektu możesz dodawać dowolne inne technologie, które miałyby Tobie pomóc w rozwiązaniu zadań.

### Uruchomienie aplikacji

1. Uruchom aplikację poleceniem `mvn spring-boot:run` lub z poziomu Twojego IDE uruchamiając klasę Application. 
1. Przechodzimy na adres http://127.0.0.1:8080 w przeglądarce

### Dostęp do bazy danych

1. Po uruchomieniu aplikacji dostęp do konsoli bazy danych dostępny będzie pod adresem http://127.0.0.1:8080/h2-console
1. Parametry połączenia
    - Driver Class: `org.h2.Driver`
    - JDBC URL: `jdbc:h2:mem:testdb`
    - User Name: `sa`
    - Pole Password zostawić puste
    
    
## Opis zadania
Proszę zaimplementować program, który pozwoli wyświetlić listę kontrahentów i po wyborze któregoś z nich, podejrzeć jego listę kont bankowych. Każde konto może podlegać weryfikacji na tzw. Białej Liście.

### Opis szczegółowy

W bazie danych projektu są dwie tabele kontrahenci oraz ich konta bankowe. Tabele są tworzone za każdym razem od nowa w momencie 
uruchomienia aplikacji. To samo dotyczy danych. Schemat oraz dane są przechowywane w pliku 'data.sql'. Plik w razie konieczności można modyfikować. Wyświetlanie danych ma być zrealizowane przy pomocy frameworka Vaadin.

### Zadanie 1
Stwórz obiekty encyjne, reprezentujące podany schemat bazy danych.

### Zadanie 2
Po starcie aplikacji powinna wyświetlić się lista kontrahentów pobrana z bazy danych.

Tabela kontrahentów powinna zawierać kolumny:
- nazwa
- nip

Na kliknięcie na wiersz kontrahenta ma się otworzyć lista jego kont bankowych.
Tabela kont bankowych powinna zwierać kolumny:
- numer (numer konta sformatowany do postaci xx xxxx xxxx xxxx xxxx xxxx xxxx), 
- aktywne, 
- domyślne, 
- wirtualne
- pole z zadania 3 (stan_weryfiacji)

Prosimy zwrócić uwagę estetykę wyświetlania tj. szerokości, wyrównania, zwijanie się tekstu itp. 
Sposób wyświetlenia listy kont bankowych może być dowolny.

### Zadanie 3
Na liście kont bankowych, w każdym wierszu, trzeba udostępnić przycisk w osobnej kolumnie, który służy do weryfikacji istnienia konta bankowego na ogólno-dostępnym API Białej Listy. 

Więcej: https://www.gov.pl/web/kas/api-wykazu-podatnikow-vat

Przycisk ma służyć też jako wyświetlenie samego stanu. Po najechaniu myszką na przycisk powinna pojawić w chmurce informacja o dacie weryfikacji. Dostępne stany to: nieokreślony, błędne konto, zweryfikowany. Kliknięcie przycisku pobiera info z API i ustawia odpowiedni stan w bazie danych i w interfejsie.

W bazie danych należy ustawić odpowiednio pole stan_weryfikacji, data_weryfikacji.
Czynność weryfikacji konta może odbywać się wielokrotnie.

### Podsumowanie 
Proszę założyć, że zadanie ma być wykonane w realnych warunkach pracy. Kod powinien być jakości produkcyjnej. Aplikacja powinna być odporna na wszelkie wyjątki związane z bazą danych i API.
Proszę zrobić clone tego repozytorium, stworzyć własny projekt, bazującym na tym klonie i wysłać do nas link do Twojego repozytorium.
