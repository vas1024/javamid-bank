# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.5/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.5/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.5/reference/web/servlet.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.5.5/reference/data/sql.html#data.sql.jpa-and-spring-data)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.


```mermaid
flowchart LR

%% Микросервисы
    FRONT[Front UI<br>Веб-интерфейс]
    ACCOUNTS[Accounts Service<br>Управление счетами]
    CASH[Cash Service<br>Обналичивание денег]
    TRANSFER[Transfer Service<br>Переводы между счетами]
    EXCHANGE[Exchange Service<br>Конвертация валют]
    EX_GEN[Exchange Generator<br>Генерация курсов валют]
    BLOCKER[Blocker Service<br>Блокировка операций]
    NOTIFY[Notifications Service<br>Уведомления]

%% Взаимодействия
    FRONT -- "REST/JSON<br> GET /api/accounts" --> ACCOUNTS
    FRONT -- "REST/JSON<br>new client reg" --> ACCOUNTS
    FRONT --> CASH
    FRONT  --> TRANSFER
    FRONT  --> EXCHANGE
    ACCOUNTS  --> NOTIFY
    CASH  --> ACCOUNTS
    CASH  --> BLOCKER
    CASH  --> NOTIFY
    TRANSFER  --> ACCOUNTS
    TRANSFER  --> EXCHANGE
    TRANSFER  --> BLOCKER
    TRANSFER  --> NOTIFY
    EX_GEN  --> EXCHANGE
```



```mermaid
flowchart TD
A[Christmas] -->|Get money| B(Go shopping)
B --> C{Let me think}
C -->|One| D[Laptop]
C -->|Two| E[iPhone]
C -->|Three| F[fa:fa-car Car]
``` 