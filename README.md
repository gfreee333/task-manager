# Task Manager API
REST API для управления задачами, проектами и пользователями. Проект реализован на Spring Boot с использованием PostgreSQL, JPA/Hibernate, MapStruct, Lombok, покрыт модульными тестами (JUnit 5, Mockito, Spring Boot Test) и упакован в Docker.
---
## 🚀 Возможности
- **Пользователи**: регистрация, CRUD, фильтрация по роли, поиск по имени/фамилии.
- **Проекты**: CRUD, статусы (`ACTIVE`, `COMPLETED`, `ARCHIVED`), управление участниками (Many‑to‑Many), получение проектов по владельцу/участнику.
- **Задачи**: CRUD, статусы (`TODO`, `IN_PROGRESS`, `DONE`), приоритеты (`LOW`, `MED`, `HIGH`), фильтрация, назначение исполнителя.  
  *Бизнес-правило:* исполнитель должен быть участником проекта.
- **Обработка ошибок**: централизованный `GlobalExceptionHandler` с корректными HTTP‑статусами (400, 404, 409, 500).
- **Валидация**: Bean Validation для входных DTO и параметров (`@Positive`, `@Pattern`, `@Email`).
- **Тесты**: модульные тесты для сервисов (Mockito) и контроллеров (MockMvc).
---

## 🛠 Технологический стек
| Категория          | Технологии                                  |
|--------------------|---------------------------------------------|                                 
| Java               | 17                                          |
| Framework          | Spring Boot 3.1.5                           |
| Persistence        | Spring Data JPA, Hibernate, PostgreSQL      |
| Mapping            | MapStruct                                   |
| Utilities          | Lombok                                      |
| Testing            | JUnit 5, Mockito, AssertJ, Spring Boot Test |
| Containerisation   | Docker, Docker Compose                      |
| Containerisation   | Docker, Docker Compose |
---

## 🗄️ Структура базы данных

Схема состоит из четырёх таблиц: `users`, `projects`, `tasks`, `user_projects`. Связи построены с помощью внешних ключей, каскадное удаление и индексы оптимизированы под частые запросы.

### Таблица `users`
Хранит информацию о пользователях.

| Поле | Тип | Описание |
|------|-----|----------|
| `id` | bigserial | Первичный ключ |
| `email` | varchar(100) | Уникальный, обязательный |
| `password` | varchar(255) | Хеш пароля |
| `first_name` | varchar(50) | Имя |
| `last_name` | varchar(50) | Фамилия |
| `role` | varchar(20) | Роль (ADMIN, USER, MANAGER) |
| `created_at` | timestamp | Дата создания |
| `updated_at` | timestamp | Дата обновления |

Индексы: `idx_users_email`, `idx_users_role`.

### Таблица `projects`
Проекты, которыми управляют пользователи.

| Поле | Тип | Описание |
|------|-----|----------|
| `id` | bigserial | Первичный ключ |
| `name` | varchar(100) | Название проекта |
| `description` | text | Описание |
| `status` | varchar(20) | Статус (ACTIVE, COMPLETED, ARCHIVED) |
| `owner_id` | bigint | Внешний ключ на `users.id` (владелец) |
| `created_at` | timestamp | Дата создания |
| `updated_at` | timestamp | Дата обновления |

Индексы: `idx_projects_owner`, `idx_projects_status`.

### Таблица `tasks`
Задачи, привязанные к проектам и пользователям.

| Поле | Тип | Описание |
|------|-----|----------|
| `id` | bigserial | Первичный ключ |
| `title` | varchar(200) | Заголовок задачи |
| `description` | text | Описание |
| `status` | varchar(20) | Статус (TODO, IN_PROGRESS, DONE) |
| `priority` | varchar(20) | Приоритет (LOW, MED, HIGH) |
| `due_date` | date | Срок выполнения |
| `assignee_id` | bigint | Внешний ключ на `users.id` (исполнитель) |
| `project_id` | bigint | Внешний ключ на `projects.id` |
| `created_by_id` | bigint | Внешний ключ на `users.id` (создатель) |
| `created_at` | timestamp | Дата создания |
| `updated_at` | timestamp | Дата обновления |

Индексы: `idx_tasks_project`, `idx_tasks_assignee`, `idx_tasks_status`, `idx_tasks_due_date`.

### Таблица `user_projects`
Связующая таблица для отношения Many‑to‑Many между пользователями и проектами (участники).

| Поле | Тип | Описание |
|------|-----|----------|
| `user_id` | bigint | Внешний ключ на `users.id` |
| `project_id` | bigint | Внешний ключ на `projects.id` |

Первичный ключ составной `(user_id, project_id)`.  
Индексы: `idx_user_projects_user`, `idx_user_projects_project`.

### Связи
- **Пользователь – задачи**: один пользователь может быть исполнителем многих задач (`assignee_id`) и создателем многих задач (`created_by_id`).
- **Проект – задачи**: один проект содержит много задач (`project_id`), при удалении проекта задачи удаляются каскадно.
- **Пользователь – проекты**: один пользователь может владеть многими проектами (`owner_id`). При удалении владельца проектов поле `owner_id` обнуляется (`ON DELETE SET NULL`).
- **Пользователь – проекты (участие)**: многие пользователи могут участвовать во многих проектах (таблица `user_projects`).

Миграции схемы управляются Hibernate (при профиле `dev` используется `create-drop`, при `prod` — `update`). Для продакшена рекомендуется использовать инструменты миграции (Flyway/Liquibase).
---

## 📦 Установка и запуск

### Требования
- JDK 17+
- Maven 3.6+
- PostgreSQL 15+ (если запуск без Docker)
- Docker и Docker Compose (для контейнеризированного запуска)
## Запуск через Docker (рекомендуется)

1. Склонируйте репозиторий:
   ```bash
   git clone https://github.com/your-username/task-manager.git
   cd task-manager
2. Запустите контейнеры:
   ```bash
   docker-compose up --build -d
3. Приложение будет доступно по адресу:
   ```bash
   http://localhost:8080/manager/api/
4. Просмотр логов:
   ```bash
   docker-compose logs -f app
5. Остановка
   ```bash
   docker-compose down
---

## 🧪 Тестирование
Проект покрыт модульными тестами, которые проверяют корректность работы сервисов и контроллеров. Для тестирования используются:

- **JUnit 5** – основа тестов
- **Mockito** – мокирование зависимостей (репозитории, мапперы)
- **Spring Boot Test** – интеграция с контекстом (`@WebMvcTest`, `MockMvc`)
- **AssertJ** – удобные fluent-проверки

### Запуск тестов
Выполните в корне проекта:
```bash
mvn clean test
```
Если Maven не установлен глобально, используйте обёртку:
* Windows: ``` .\mvnw clean test ```
* Linux/macOS: ``` ./mvnw clean test ```

Структура тестов
* ```UsersControllerTest``` – проверяет все эндпоинты для пользователей: успешные ответы, валидацию, обработку ситуаций «пользователь не найден» (404), дубликат email (409) и неверные данные (400).
* ```ProjectsControllerTest``` – тесты для проектов: создание, получение, обновление, удаление, фильтрация по статусу, добавление и удаление участников.
* ```TasksControllerTest``` – покрывает создание задач, получение по ID, фильтрацию по статусу/приоритету, обновление, удаление и бизнес-правило «исполнитель должен быть участником проекта».
* ```Сервисные тесты (UsersServiceTest, ProjectsServiceTest, TasksServiceTest)``` – изолированно проверяют бизнес-логику с моками репозиториев. Каждый метод сервиса имеет позитивные и негативные сценарии.




   
