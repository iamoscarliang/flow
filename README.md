<div align="center">
<img src="https://github.com/iamoscarliang/flow/blob/master/screenshots/feature_graph.png" width="600">

[![Build](https://github.com/iamoscarliang/flow/workflows/Build/badge.svg)](https://github.com/iamoscarliang/flow/actions)
![minSdk](https://img.shields.io/badge/minSdk-27-brightgreen)
[![license](https://img.shields.io/badge/license-MIT-brightgreen)](https://github.com/iamoscarliang/flow/blob/master/LICENSE)

[<img src="https://github.com/iamoscarliang/flow/blob/master/screenshots/download.png" width="180">](https://github.com/iamoscarliang/flow/tree/master/app/release/app.apk)

**News app made with [newsapi](https://www.newsapi.ai)**
</div>

---

## :pushpin: Screenshots
<img src="https://github.com/iamoscarliang/flow/blob/master/screenshots/screenshot.png" width="800">

## :pencil: Requirements
### newsapi API Key
Flow uses [newsapi](https://www.newsapi.ai) to load latest news. To use the API, you will need to obtain a free developer API key. See the [newsapi Documentation](https://www.newsapi.ai/documentation) for instructions.
Once you have the key, add this line to the [Constants](https://github.com/iamoscarliang/flow/blob/master/app/src/main/java/com/oscarliang/flow/util/Constants.kt) file.
```java
const val API_KEY = "Your API key"
```

## :books: Tech Stack
- :wrench: Architecture Component [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) & [Livedata](https://developer.android.com/topic/libraries/architecture/livedata)
- :rocket: [Coroutines](https://developer.android.com/kotlin/coroutines) for asynchronous task
- :reminder_ribbon: [Data Binding](https://developer.android.com/topic/libraries/data-binding) to make declarative layouts
- :ship: [Navigation](https://developer.android.com/guide/navigation) for scene navigation
- :cloud: [Retrofit](https://square.github.io/retrofit) for remote data client
- :floppy_disk: [Room](https://developer.android.com/training/data-storage/room) for local data cache
- :framed_picture: [Glide](https://github.com/bumptech/glide) for image loading
- :syringe: [Koin](https://github.com/InsertKoinIO/koin) for dependency injection
- :bar_chart: [Junit](https://developer.android.com/training/testing/local-tests) & [Mockk](https://mockk.io) for Unit test

## :gear: Architecture
<img src="https://github.com/iamoscarliang/flow/blob/master/screenshots/mvvm.png" width="400">
