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
____________________________________________________________________
| Категория          | Технологии                                  |
|--------------------|---------------------------------------------|                                 
| Java               | 17                                          |
| Framework          | Spring Boot 3.1.5                           |
| Persistence        | Spring Data JPA, Hibernate, PostgreSQL      |
| Mapping            | MapStruct                                   |
| Utilities          | Lombok                                      |
| Testing            | JUnit 5, Mockito, AssertJ, Spring Boot Test |
| Containerisation   | Docker, Docker Compose                      |
|__________________________________________________________________|

| Containerisation   | Docker, Docker Compose |

