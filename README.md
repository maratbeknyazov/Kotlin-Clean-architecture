# Laboratory 4 Clean architecture

MovieShelf — учебное приложение на Kotlin и Jetpack Compose. Пакет: `uz.ttpu.movieshelf`, Minimum SDK: 24. В файле задания указан заголовок Laboratory Work 9; поэтому ветка решения называется `lab-9`.

Открой папку `Laboratory4` через File → Open в Android Studio. Для сборки нужны JDK 17 или новее и Android SDK 35. После Gradle Sync запусти `app` на устройстве API 24+.

## Что делает приложение

- Показывает восемь фильмов: рейтинг по убыванию, при одинаковом рейтинге — название по алфавиту.
- Позволяет добавить фильм в избранное. Избранное сохраняется в SharedPreferences.
- При отключённой имитации сети показывает ранее загруженный список и сообщение об офлайн-режиме.
- Если кеша ещё нет, показывает ошибку и кнопку Retry.
- Фильтр Favorites only оставляет только избранное. При снятии сердца фильм сразу исчезает из этого списка.

`Fake network online` — переключатель учебного источника. После переключения нажми Refresh. Настоящий сервер не используется.

## Где находится код

```text
domain/src/main/java/uz/ttpu/movieshelf/domain/
  model/       Movie, MoviesResult
  repository/  MovieRepository
  usecase/     GetMoviesUseCase, ToggleFavoriteUseCase, GetFavoriteMoviesUseCase

app/src/main/java/uz/ttpu/movieshelf/
  data/remote/       MovieDto и имитация сети
  data/local/        хранилища в памяти и SharedPreferences
  data/mapper/       перевод DTO в Movie
  data/repository/   объединение сети, кеша и избранного
  presentation/movies/  состояние, ViewModel, экран и четыре Preview
  di/                ручное создание зависимостей
  MovieShelfApp.kt    контейнер на время жизни приложения
  MainActivity.kt     соединение ViewModel и экрана
```

`domain` — отдельный Kotlin-модуль. Он не зависит от `app` и Android. Use case знает интерфейс `MovieRepository`, а класс `MovieRepositoryImpl` реализует его в `data`. В `presentation` нет импортов `data`.

Код использует небольшие классы, `if`, `when`, `map`, `filter` и `copy`. Корутины, StateFlow и sealed interface нужны по условию лабораторной. В [LAB_REPORT.md](LAB_REPORT.md) есть короткие объяснения всех заданий и шаги проверки.

## Сборка и тесты

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Тесты находятся в `app/src/test/java/uz/ttpu/movieshelf/`. Их можно запускать зелёной кнопкой рядом с классом в Android Studio. Результаты выполненных проверок записаны в отчёте.

Кеш списка хранится в памяти и пропадает при завершении процесса. Избранное хранится на диске и остаётся после перезапуска. Это поведение соответствует Task 11.

## Git

В локальном репозитории сохранены `main` и `lab-9`. Коммиты замены хранилища и добавления фильтра позволяют сравнить изменения по слоям. `local.properties`, `.gradle`, `.idea` и папки `build` исключены из Git.

Для публикации в своём аккаунте: Git → GitHub → Share Project on GitHub. После создания remote отправь ветки `main` и `lab-9`. Публикация в GitHub ещё не выполнена.
