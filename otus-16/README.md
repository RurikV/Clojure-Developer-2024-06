## Clojure Developer. Урок 16

Домашнее задание

- распаковать архив [logs.7z](logs.7z) (нужно загрузить реальный файл с помощью `git lfs checkout`, см. ниже)
- написать функцию для чтения логов из файлов архива
- написать функцию для парсинга логов согласно стандарту https://httpd.apache.org/docs/2.4/logs.html#combined
- реализовать функционал для агрегации метрик из записей логов в многопоточном стиле
  - подсчитать общее количество байт
  - количество байт, суммарно отданных по заданному URL (или по всем URLам)
  - количество URL, запрошенных с заданным полем Referer

### Git LFS

- установите Git LFS согласно [руководству](https://docs.github.com/en/repositories/working-with-files/managing-large-files/installing-git-large-file-storage)
- подключите к репозиторию командой `git lfs install`
- актуализируйте большие файлы командой `git lfs checkout`
