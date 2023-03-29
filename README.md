[![CI pipeline](https://github.com/iartr/AccessibilityPlayer/actions/workflows/build-pipeline.yml/badge.svg)](https://github.com/iartr/AccessibilityPlayer/actions/workflows/build-pipeline.yml)

### Установка
Для проверки решения достаточно [скачать](https://disk.yandex.ru/d/YDsyLZ614AQqRQ) установочный APK файл и запустить на любом Android устройстве.

### Архитектура
Приложение использует классическую клиент-серверную архитектуру.    
![image](https://sun9-52.userapi.com/impg/gpjuQc9kQnsmcutTg8MHqz77ouMMsOQnBUh0uQ/f6DGYNJWDis.jpg?size=1346x992&quality=96&sign=779eeaa686b5bce44d1f36de98e49d7a&type=album)


Сервер отдает ссылку на видео в формате **dash** и конфиг для установки визуальных эффектов конкретно для этого видео.     
Пример отдаваемого конфига:
```json
{
  "video_name": "output",
  "accessibility_config": [
    {
      "startTime": 12,
      "endTime": 16,
      "actions": [
        "lowerContrast",
        "blur"
      ]
    },
    {
      "startTime": 16,
      "endTime": 18,
      "actions": [
        "lowerContrast"
      ]
    },
    {
      "startTime": 18,
      "endTime": 21,
      "actions": [
        "lowerContrast",
        "screamer"
      ]
    },
    {
      "startTime": 26,
      "endTime": 30,
      "actions": [
        "lowerContrast",
        "blur"
      ]
    },
    {
      "startTime": 32,
      "endTime": 38,
      "actions": [
        "lowerContrast"
      ]
    }
  ]
}
```

### Open GL
Визуальные эффекты применяются в real-time за счет шейдеров на базе **OpenGL**.
![opengl](https://sun9-47.userapi.com/impg/U23ypb3_uUHGRnoF0-TNUojrrjofFDXDcCQ4lA/KXxetdEzyO4.jpg?size=336x482&quality=96&sign=907ece3c1f165c28993df0f83ae4556f&type=album)
