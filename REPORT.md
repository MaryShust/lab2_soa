# Отчёт по проекту Lab2 SOA

## 1. Общее описание

Проект представляет собой реализацию лабораторной работы №2 по предмету
«Информационные системы» (SOA). На основе спецификации OpenAPI
(`src/docs/openapi.yaml`) реализованы **два веб-сервиса** и **клиентское
веб-приложение**, работающее поверх их API.

Домен предметной области — коллекция лабораторных работ (класс `LabWork`),
у каждой из которых есть координаты (`Coordinates`), автор (`Person`) и
атрибуты сложности (`Difficulty`).

### Состав проекта (многомодульный Maven-проект)

| Модуль             | Назначение                                   | Технологии                          | Сервер / порт          |
|--------------------|----------------------------------------------|-------------------------------------|------------------------|
| `labwork-service`  | «Вызываемый» сервис: CRUD + фильтрация/сортировка/пагинация | Spring MVC REST + JPA (Hibernate) + PostgreSQL | Payara, HTTPS :8181 |
| `bars-service`     | «Вызывающий» сервис: операции поверх API первого | JAX-RS (Jakarta EE) + CDI + Jackson | WildFly, HTTPS :8443/bars |
| `client`           | Веб-интерфейс для пользователя               | Spring Boot + Thymeleaf + `HttpClient` | Helios / локально :8082 |

Корневой `pom.xml` — агрегатор (`packaging=pom`, версия `2.2.0`), управляет
зависимостями (BOM Spring Boot 3.5.6, PostgreSQL 42.7.7, Jakarta EE 10, Jackson 2.19.2)
и модулями. Java 17.

---

## 2. Архитектура высокого уровня

```
                HTTPS (self-signed)
  ┌────────────┐  ─────────────────────►  ┌──────────────────┐
  │   client   │                          │ labwork-service  │
  │ Spring Boot│                          │  Spring MVC REST │
  │ Thymeleaf  │                          │  Payara :8181    │
  │  :8082     │                          │  + PostgreSQL    │
  └────────────┘                          └──────────────────┘
        │                                          ▲
        │  HTTPS                                   │ REST (HTTPS)
        ▼                                          │
  ┌────────────┐  ─────────────────────────────────┘
  │bars-service│  (JAX-RS, WildFly :8443/bars)
  │  CDI/Jackson│  вызывает GET /labworks/{id} и PUT /labworks/{id}
  └────────────┘
```

- **labwork-service** не знает о других сервисах — это «источник истины»
  (хранит данные в PostgreSQL).
- **bars-service** не имеет собственной БД (кроме in-memory реестра дисциплин),
  а проксирует вызовы к `labwork-service`.
- **client** обращается к обоим сервисам по HTTPS, преобразует JSON в
  человеко-читаемые HTML-страницы (таблицы, параграфы) и отображает ошибки.

Все межсервисные вызовы идут по **HTTPS с самоподписанным сертификатом**;
HTTP запрещён (в `bars-service` через `web.xml` с `transport-guarantee=CONFIDENTIAL`).
Оба Java-клиента (`LabWorkClient`, `client.GatewayService`) используют
`HttpClient` с custom `SSLContext` (TrustManager, доверяющий всем сертификатам) —
это необходимо для работы с самоподписанным сертификатом серверов.

---

## 3. labwork-service (Spring MVC REST, Payara)

Каталог: `labwork-service/src/main/java/inf_system/lab2/labwork/`

### 3.1. Точка входа и развёртывание

- `LabWorkServiceApplication` — `@SpringBootApplication`, наследует
  `SpringBootServletInitializer` (`configure(...)`), что позволяет
  разворачивать WAR (`finalName=ROOT`) в контейнере (Payara).
- `pom.xml` исключает встроенный Tomcat из `starter-web` и подключает его со
  `scope=provided` — приложение работает как WAR, но сохраняет возможность
  локального запуска `SpringApplication.run`.
- `config/JpaProxyConfig` — `BeanPostProcessor`, который принудительно задаёт
  `entityManagerFactoryInterface = EntityManagerFactory` для
  `LocalContainerEntityManagerFactoryBean`. Это обходной путь для корректной
  работы JPA/Spring в окружении Payara (issues с проксированием интерфейсов).

### 3.2. Слои

```
web/          LabWorkController, GlobalExceptionHandler
service/      LabWorkService
repository/   LabWorkRepository (JpaRepository + JpaSpecificationExecutor)
model/        LabWork, Coordinates, Person, Difficulty
dto/          LabWorkDto, CoordinatesDto, PersonDto, ErrorDto
query/        QueryParser, CriteriaBuilderSupport, SortField, FilterField,
              FilterOperator, SortRule, FilterCondition
exception/    BadRequestException, NotFoundException
config/       JacksonConfig, JpaProxyConfig
```

### 3.3. Модель данных (JPA)

- `LabWork` (`@Entity`, таблица `lab_work`):
  - `id` — `@GeneratedValue(IDENTITY)`, `Long`;
  - `name` — `nullable=false`;
  - `coordinates` — `@Embedded` (колонки `coord_x`, `coord_y`);
  - `creationDate` — `ZonedDateTime`, генерируется в сервисе при создании;
  - `minimalPoint`, `personalQualitiesMinimum`, `averagePoint` — `Double`;
  - `difficulty` — `@Enumerated(STRING)`;
  - `author` — `@OneToOne(cascade=ALL, orphanRemoval=true)`, ссылка `author_id`.
- `Person` (`@Entity`) — `implements Comparable<Person>` с «nulls-first»
  сравнением по `name → birthday → height → weight → passportID` (используется
  для операции «минимальный автор»).
- `Coordinates` — `@Embeddable` (x, y).
- `Difficulty` — enum `NORMAL, INSANE, TERRIBLE`.

Параметры БД в `application.properties`:
`jdbc:postgresql://localhost:5432/soa`, пользователь `postgres`,
`ddl-auto=update`, диалект PostgreSQL, `open-in-view=false`.

### 3.4. REST-контроллер (`LabWorkController`)

Базовый путь `/labworks`. Все операции соответствуют спецификации OpenAPI,
данные передаются **через query-параметры** (тело запроса не используется).

| Метод | Путь | operationId | Назначение |
|-------|------|-------------|------------|
| GET  | `/labworks` | getLabWorks | Пагинация + сортировка + фильтрация |
| POST | `/labworks` | addLabWork | Создание (201 Created) |
| GET  | `/labworks/{id}` | getLabWorkById | Получение по id |
| PUT  | `/labworks/{id}` | updateLabWork | Полное обновление |
| DELETE | `/labworks/{id}` | deleteLabWork | Удаление (204 No Content) |
| GET  | `/labworks/by-max-person-min` | getLabWorkWithMinimalAuthor | Объект с минимальным автором |
| GET  | `/labworks/by-difficulty` | countLabWorksByDifficulty | Количество по сложности |
| GET  | `/labworks/by-minimal-point-greater-than` | ...GreaterThan | Объекты с minimalPoint > X |

Контроллер `@Validated`, параметры замаплены через `@RequestParam`
(в т.ч. `author-birthday` как `LocalDate` через `@DateTimeFormat`).

### 3.5. Валидация

В контроллере `validateCreateRequest(...)` проверяет ограничения целостности
(совпадают с OpenAPI-схемами):

- `name` — непустое;
- `x ≤ 678`, `y > -963`;
- `minimalPoint`, `personalQualitiesMinimum`, `averagePoint` > 0;
- `authorName` непустое (если указано), `authorHeight/Weight` > 0,
  `passportID` длиной ≤ 34.

Нарушение → `BadRequestException` → HTTP 400 с телом `{"status":400,"message":...}`.

### 3.6. Сервисный слой (`LabWorkService`)

- `list(sort, filter, page, size)` — ключевой метод:
  - значения по умолчанию `page=1`, `size=50`; проверки `page≥1`, `size∈[1,1000]`;
  - парсит `sort` и `filter` через `QueryParser`;
  - строит динамический запрос через **JPA Criteria API**:
    `CriteriaBuilderSupport.buildPredicates(...)` (AND всех условий) и
    `buildOrders(...)`; если сортировка не задана — по `id asc`;
  - пагинация через `setFirstResult((page-1)*size)` и `setMaxResults(size)`;
  - если `page` вне диапазона — возвращается пустой список.
- `create/update` — применяют команду (`LabWorkCreateCommand`), при создании
  выставляется `creationDate = ZonedDateTime.now()`.
- `getById/delete` — валидируют id (`≥1`), при отсутствии → `NotFoundException` (404).
- `getWithMinimalAuthor()` — линейный поиск объекта с минимальным `Person`
  (`compareTo`); если авторов нет → 404.
- `countByDifficulty(...)` и `getWithMinimalPointGreaterThan(...)` — агрегации
  в памяти.

Присутствуют методы `findEntity/saveEntity` — публичный API для работы с
сущностями (используются потенциальными интеграциями).

### 3.7. Механизм фильтрации и сортировки (`query/`)

- `SortField` — enum допустимых полей сортировки с разбором на
  `rootAttribute` + `nestedAttribute` (например, `coordinates.x` →
  `coordinates.x`; `author.passportID` → `author.passportID`).
- `FilterField` — enum полей фильтрации с типом (`NUMBER`, `STRING`, `DATE`,
  `ENUM`, `OBJECT`) и флагом `nullable`. Поле `author` (OBJECT) допускает
  только `isnull/notnull`; остальные — `eq/ne/gt/ge/lt/le/contains`.
- `FilterOperator` — `eq, ne, gt, ge, lt, le, contains, isnull, notnull`.
- `QueryParser`:
  - `parseSort("field:dir,field2:dir2")` → список `SortRule`;
  - `parseFilter("field:op:value,...")` → список `FilterCondition`
    (значение разбирается максимум на 3 части, чтобы поддержать `:` в значении);
  - проверяет допустимость поля/оператора и применимость оператора к типу поля.
- `CriteriaBuilderSupport`:
  - `resolvePath` — строит вложенный путь (`root.get(attribute).get(nested)`);
  - `buildPredicate` — для `contains` использует `LIKE lower(...)`, для
    `isnull/notnull` — `isNull/isNotNull`;
  - `convertValue` — приводит значение к типу поля (`Double`, `Difficulty`,
    дата через `parseDate`, строка).

### 3.8. Обработка ошибок (`GlobalExceptionHandler`)

`@RestControllerAdvice` переводит исключения в JSON `ErrorDto`:

| Исключение | HTTP |
|------------|------|
| `NotFoundException` | 404 |
| `BadRequestException` | 400 |
| `MissingServletRequestParameterException`, `MethodArgumentTypeMismatchException`, `IllegalArgumentException` | 400 («Данные нарушают ограничения целостности») |
| любое другое `Exception` | 500 («Внутренняя ошибка сервера») |

`config/JacksonConfig` регистрирует `JavaTimeModule` и отключает
`WRITE_DATES_AS_TIMESTAMPS` — даты сериализуются в ISO-строки.

---

## 4. bars-service (JAX-RS, WildFly)

Каталог: `bars-service/src/main/java/inf_system/lab2/bars/`

### 4.1. Развёртывание

- `BarsApplication` — `@ApplicationPath("/")`, `extends Application`
  (JAX-RS entrypoint). WAR собирается как `bars.war`, доступен по
  `https://localhost:8443/bars/...`.
- `webapp/WEB-INF/web.xml` — `security-constraint` с
  `transport-guarantee=CONFIDENTIAL` на `/*` (принудительный HTTPS).
- `webapp/WEB-INF/beans.xml` — CDI `bean-discovery-mode="all"`.
- `JacksonProvider` — `@Provider`, наследует `JacksonJsonProvider` и реализует
  `ContextResolver<ObjectMapper>` (JavaTimeModule, ISO-даты).

### 4.2. Ресурсы (JAX-RS endpoints)

`resource/DisciplineResource` — `@Path("/discipline/{discipline-id}/labwork")`,
`POST /{labwork-id}/add` → `BarsService.addLabWorkToDiscipline`.

`resource/DifficultyResource` — `@Path("/labwork/{labwork-id}/difficulty")`,
`POST /increase?steps-count=N` → `BarsService.increaseDifficulty`.

### 4.3. Бизнес-логика (`BarsService`)

- `addLabWorkToDiscipline(disciplineId, labWorkId)`:
  1. валидация id (≥1) → иначе `BadRequestException` (400);
  2. проверка существования дисциплины в `DisciplineStore` → иначе 404;
  3. `labWorkClient.getById(labWorkId)` — проверка существования работы
     (404, если нет);
  4. `disciplineStore.add(...)`; повторное добавление → `ConflictException` (409).
- `increaseDifficulty(labWorkId, stepsCount)`:
  - валидация `stepsCount ≥ 1`;
  - читает работу из первого сервиса;
  - повышает сложность по шкале `NORMAL → INSANE → TERRIBLE`
    (`SCALE[ordinal + stepsCount]`); выход за пределы → 400;
  - сохраняет изменение обратно в первый сервис через `PUT /labworks/{id}`.
- `countByDifficulty(...)` — делегирует в `labWorkClient.countByDifficulty`.

### 4.4. Клиент первого сервиса (`client/LabWorkClient`)

- Использует `java.net.http.HttpClient` с **insecure** `SSLContext` (TrustManager
  доверяет всем сертификатам) и без проверки hostname — для самоподписанного
  сертификата. Адрес по умолчанию `https://localhost:8181`.
- `getById` — GET `/labworks/{id}` (200 → `LabWork`, 404 → `empty`, 400 → 400).
- `update` — PUT `/labworks/{id}?<query>`; query собирается `LabWorkQueryBuilder`.
- `countByDifficulty` — GET `/labworks/by-difficulty?difficulty=...`.
- Исключения: `UpstreamFailureException` (status>=500/сбой), 
  `BadUpstreamRequestException`, `NotFoundUpstreamException`.

`client/LabWorkQueryBuilder` — собирает query-строку из объекта `LabWork`
(все поля, включая автора), URL-кодируя значения.

`config/ServiceConfig.labWorkBaseUrl()` — разрешает адрес первого сервиса:
системное свойство `labwork.service.url` → переменная окружения
`LABWORK_SERVICE_URL` → значение по умолчанию `https://localhost:8181`.

`service/LabWorkClientProducer` — CDI-продюсер (`@Produces @Singleton`) для
`LabWorkClient` (double-checked locking).

### 4.5. Хранилище дисциплин (`DisciplineStore`)

`@ApplicationScoped`, хранит в памяти (`ConcurrentHashMap<Long, Set<Long>>`)
соответствие «дисциплина → набор labwork-id». Набор известных дисциплин
конфигурируется свойством `bars.disciplines` / env `BARS_DISCIPLINES`
(по умолчанию `1,2,3`). Данные не персистентны (in-memory).

### 4.6. Обработка ошибок

- `BarsExceptionMapper` (`ExceptionMapper<RuntimeException>`) — `BadRequest→400`,
  `NotFound→404`, `Conflict→409`, иначе 500.
- `UpstreamExceptionMapper` (`ExceptionMapper<UpstreamFailureException>`) — 500
  с сообщением о сбое вызова первого сервиса.

Тело ошибки — `model/ErrorResponse` (`status`, `message`).

---

## 5. client (Spring Boot + Thymeleaf)

Каталог: `client/src/main/java/inf_system/lab2/client/`

### 5.1. Конфигурация

- `ClientApplication` — `@SpringBootApplication`.
- `application.properties`: порт `8082`; адреса сервисов
  `services.labwork.url=https://localhost:8181`,
  `services.bars.url=https://localhost:8443/bars`.

### 5.2. Слой шлюза (`service/GatewayService`)

Единая точка обращения к обоим сервисам. Использует `java.net.http.HttpClient`
с insecure `SSLContext` (self-signed). Методы:

- `getLabWorks(page, size, sort, filter)` — с фильтрацией/сортировкой/пагинацией;
- `getLabWork(id)`, `createLabWork(form)`, `updateLabWork(id, form)`,
  `deleteLabWork(id)`;
- `getMinimalAuthor()`, `countByDifficulty(difficulty)`,
  `greaterThanMinimalPoint(value)`;
- `addToDiscipline(disciplineId, labWorkId)` (bars), `increaseDifficulty(id, steps)` (bars).

`requireSuccess(...)` разбирает тело ошибки сервиса (`message`) и бросает
`GatewayException(status, message)`. При недоступности — `502`.

`buildFormQuery(...)` собирает query-строку из `LabWorkForm` в точном
соответствии с именами параметров API.

### 5.3. Веб-слой (`web/ClientController`)

| Метод | URL | Шаблон | Назначение |
|-------|-----|--------|------------|
| GET | `/` | index | Список с формой sort/filter/page/size |
| GET | `/labworks/new` | form | Форма создания |
| GET | `/labworks/{id}` | view | Просмотр объекта |
| GET | `/labworks/{id}/edit` | form | Форма редактирования |
| POST | `/labworks` | → redirect | Создание |
| POST | `/labworks/{id}` | → redirect | Обновление |
| POST | `/labworks/{id}/delete` | → redirect | Удаление |
| GET | `/special/min-author` | view | Минимальный автор |
| GET | `/special/by-difficulty` | byDifficulty | Подсчёт по сложности |
| GET | `/special/greater-than` | greaterThan | minimalPoint > X |
| POST | `/bars/discipline/{d}/labwork/{id}/add` | view | Добавить в дисциплину (Bars) |
| POST | `/bars/labwork/{id}/increase` | view | Повысить сложность (Bars) |

Все операции обёрнуты в `try/catch (GatewayException)`: при ошибке сервиса
пользователю показывается сообщение (`error`-блок или страница `error.html`).

### 5.4. Модели и шаблоны

- Модели: `LabWork`, `Coordinates`, `Person`, `Difficulty`, `LabWorkForm`
  (форма с `@DateTimeFormat` для `authorBirthday`), `ApiError`.
- Thymeleaf-шаблоны (`src/main/resources/templates/`):
  `index.html` (таблица коллекции + фильтры), `form.html` (создание/редактирование),
  `view.html` (детали + операции Bars), `byDifficulty.html`, `greaterThan.html`,
  `error.html`. Стиль — `static/css/style.css`.

Данные преобразуются в человеко-читаемый вид: координаты формата `x=.., y=..`,
даты через `#temporals.format(...)`, автор разворачивается в набор параграфов.

---

## 6. Потоки выполнения (примеры сценариев)

**Просмотр коллекции с фильтром:**
`client GET /?sort=name:asc&filter=difficulty:eq:TERRIBLE`
→ `GatewayService.getLabWorks` → HTTPS `GET /labworks?...` на labwork-service
→ `LabWorkService.list` → JPA Criteria (predicates + orders + пагинация)
→ JSON-массив → Thymeleaf → HTML-таблица.

**Повышение сложности через Bars:**
`client POST /bars/labwork/5/increase?steps=1`
→ `GatewayService.increaseDifficulty` → HTTPS `POST /bars/labwork/5/difficulty/increase`
на bars-service → `BarsService.increaseDifficulty` → `LabWorkClient.getById(5)`
(HTTPS к labwork-service) → повышение enum → `LabWorkClient.update`
(HTTPS `PUT /labworks/5?name=...`) → обновлённый `LabWork` возвращается клиенту.

**Добавление в дисциплину:**
`client POST /bars/discipline/1/labwork/5/add`
→ bars-service `DisciplineStore.disciplineExists(1)` → `getById(5)` →
`DisciplineStore.add(1,5)`. Повтор → 409 Conflict.

---

## 7. Соответствие требованиям задания

| Требование | Реализация |
|------------|------------|
| Первый сервис — Spring MVC REST, Payara | `labwork-service` (WAR `ROOT`, `SpringBootServletInitializer`) |
| Второй сервис — JAX-RS, WildFly, вызывает API первого | `bars-service` (`Application`, `@Path`, CDI, `LabWorkClient`) |
| Все функции API строго по спецификации | контроллеры/ресурсы реализуют все `operationId` из `openapi.yaml` |
| HTTPS с самоподписанным сертификатом, HTTP запрещён | insecure `HttpClient` в клиентах; `web.xml` `CONFIDENTIAL` в bars |
| Клиент на любом веб-фреймворке (Helios) | Spring Boot + Thymeleaf (`client`, jar) |
| Сортировка, фильтрация, пагинация | параметры `sort`, `filter`, `page`, `size` в API и UI |
| Человеко-читаемый вид | HTML-таблицы/параграфы через Thymeleaf |
| Информирование об ошибках | `GatewayException` + `error`-блок/`error.html`, JSON `Error` |

---

## 8. Сборка и запуск (кратко)

1. `mvn clean package` — собирает все модули
   (`labwork-service/target/ROOT.war`, `bars-service/target/bars.war`,
   `client/target/client.jar`).
2. Развернуть `ROOT.war` в Payara (HTTPS :8181), `bars.war` в WildFly
   (HTTPS :8443/bars).
3. Настроить PostgreSQL: БД `soa`, пользователь `postgres`/`test`
   (см. `labwork-service/src/main/resources/application.properties`).
4. Запустить клиент: `java -jar client/target/client.jar` → http://localhost:8082.

Адрес первого сервиса для bars-service задаётся свойством
`labwork.service.url` / env `LABWORK_SERVICE_URL`; список дисциплин —
`bars.disciplines` / env `BARS_DISCIPLINES`.

---

## 9. Замечания и потенциальные улучшения

- `bars-service` использует in-memory `DisciplineStore` — данные не
  переживают перезапуск; при необходимости можно вынести в БД.
- Агрегации `countByDifficulty` и `getWithMinimalPointGreaterThan` в
  `labwork-service` загружают весь набор (`findAll`) — для больших коллекций
  стоит заменить на SQL-агрегаты/`count`-запросы.
- Валидация ограничений целостности дублируется в контроллере и JPA — при
  развитии имеет смысл централизовать через Bean Validation.
- Клиенты намеренно «доверяют всем» сертификатам (требование self-signed);
  для продакшена нужен `truststore` с конкретным сертификатом.
