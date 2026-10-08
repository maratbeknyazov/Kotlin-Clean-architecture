# Проверка Laboratory 4

Проверено 8 октября 2026 года.

## Сборка

Выполнено `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`. Результат — BUILD SUCCESSFUL. Debug APK находится в `app/build/outputs/apk/debug/app-debug.apk`; копия для установки — `../Submission/MovieShelf.apk`.

Android Lint: 0 ошибок, 8 предупреждений. Они относятся к target SDK, наличию более новых версий зависимостей и отсутствию отдельной иконки приложения. Использованы версии из уже работающего проекта предыдущей лабораторной.

## Тесты

| Класс | Тестов | Ошибок |
| --- | ---: | ---: |
| GetMoviesUseCaseTest | 2 | 0 |
| MovieRepositoryImplTest | 6 | 0 |
| GetFavoriteMoviesUseCaseTest | 2 | 0 |
| MovieListViewModelTest | 3 | 0 |
| Всего | 13 | 0 |

Отчёт JUnit после запуска: `app/build/reports/tests/testDebugUnitTest/index.html`.

## Эксперименты из задания

- Добавлен временный `import android.util.Log` в domain. `:domain:compileKotlin` завершился с ошибкой `Unresolved reference 'android'`. Импорт удалён.
- Добавлен временный импорт MovieRepositoryImpl из app. Компилятор domain сообщил `Unresolved reference 'data'`. Импорт удалён.
- Временно удалён `.thenBy { it.title }`. Из двух тестов GetMoviesUseCase упал ровно один: ожидался порядок C, A, B, получен C, B, A. Сортировка восстановлена.
- В ветке кеша временно изменено `isFromCache = true` на `false`. Из шести тестов репозитория упал ровно один: offline_after_a_successful_load_returns_cache. Признак восстановлен.
- После восстановления выполнены все 13 тестов, сборка APK и Lint.

Логи этих запусков и XML с результатами сохранены в рабочей папке `../.work/Lab4-checks`. Скрипт `../.work/verify-lab4.ps1` выполняет эксперименты последовательно и восстанавливает файлы через finally.

Проверка исходников не нашла импортов Android или data в domain и импортов data в presentation. История Git показывает три изменения для замены хранилища в Task 11 и отсутствие изменений в data при добавлении фильтра в Task 14.

## Что требует устройства и аккаунта

В момент проверки `adb devices` не показывал подключённых Android-устройств. Поэтому Preview, внешний вид на устройстве, сохранение избранного после завершения процесса, Device Explorer и порядок строк CleanFlow в Logcat ещё нужно проверить по инструкции в LAB_REPORT.md. GitHub-публикация ещё не выполнена.
