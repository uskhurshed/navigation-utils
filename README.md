# 📦 Библеотека навигации от Khurshed Usmonov.

Удобный утилитный класс для управления фрагментами в Android-приложениях с анимациями и стеком.

---

## 🚀 Подключение

### 1. Добавьте JitPack в `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Добавьте зависимость в `build.gradle.kts`

```kotlin
dependencies {
    implementation("com.github.uskhurshed:navigation-utils:2.0.0")
}
```

> Также можно использовать `master-SNAPSHOT` или commit hash.

---

## 🧩 Использование

### ✅ Установить стартовый фрагмент:

```kotlin
setDefaultFragment(R.id.fragment_container, StartFragment())
```

### ➡️ Навигация к другому фрагменту:

```kotlin
navigateTo(NextFragment())
```

### 📦 Получить стек:

```kotlin
val (count, fragments) = getBackStack()
```

### ⬅️ Вернуться назад:

```kotlin
navigateUp()
```

### 🔁 Перейти к фрагменту, если он уже в стеке:

```kotlin
navigateToIfHaveInStack(AnotherFragment())
```

### 🗑 Удалить фрагмент из back stack:

```kotlin
removeFragmentOrUp(AnotherFragment())
```

---

## 🧱 API

```kotlin
object NavigationUtils {
    fun AppCompatActivity.setDefaultFragment(id: Int, fragment: Fragment)
    fun Fragment.navigateTo(fragment: Fragment, addToBackStack: Boolean = true, bundle: Bundle? = null)
    fun Fragment.navigateToIfHaveInStack(fragment: Fragment, addToBackStack: Boolean = true, bundle: Bundle? = null)
    fun Fragment.navigateUp()
    fun Fragment.getBackStack(): Pair<Int, List<String>>
    fun Fragment.removeFragmentOrUp(fragment: Fragment)
}
```

---

## 🎞 Анимации

Анимации, которые должны быть добавлены в `res/anim/`:

| Имя               | Направление       |
|-------------------|-------------------|
| `slide_in_right`  | справа → внутрь   |
| `slide_out_left`  | внутрь → влево    |
| `slide_in_left`   | слева → внутрь    |
| `slide_out_right` | внутрь → вправо   |

Пример `slide_in_right.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android">
    <translate
        android:duration="@android:integer/config_shortAnimTime"
        android:fromXDelta="100%p"
        android:toXDelta="0"
        android:interpolator="@android:anim/accelerate_interpolator" />
</set>
```

---

## 📜 License

MIT License — используй свободно 😎
