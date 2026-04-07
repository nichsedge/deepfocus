# 1) Core Concept

A **focus timer app** with:

* Pomodoro mode (structured sessions)
* Stopwatch mode (free tracking)
* Built-in white noise / ambient sound during sessions
* Lightweight analytics

Think: simple to start, hard to misuse.

---

# 2) Key Features (MVP → Advanced)

## MVP (build this first)

* **Start / Pause / Stop timer**
* Modes:

    * Pomodoro (25/5 default, customizable)
    * Stopwatch (count-up)
* **White noise playback on session start**
* Basic stats:

    * Total focus time today
    * Session count

## Phase 2

* Custom sound mixing (rain + fan + café)
* Background playback (even when app minimized)
* Notifications + lock screen controls
* Daily/weekly reports

## Phase 3

* Tags/projects
* Goal setting (e.g. 4h/day)
* Cloud sync (Firebase)

---

# 3) UX Flow (simple, fast)

### Home Screen

* Big timer display (center)
* Mode toggle:

    * Pomodoro | Stopwatch
* Start button (primary CTA)

### During Session

* Timer running
* Sound indicator (e.g. 🌧 Rain ON)
* Controls:

    * Pause
    * Stop
    * Switch sound

### End Session

* Summary:

    * Duration
    * Tag (optional)
* Save / discard

---

# 4) White Noise System (important part)

## Sound Types

* Rain
* Brown noise
* Fan
* Café ambience
* Forest

## Behavior

* Auto-start when timer starts
* Loop seamlessly
* Fade-in (0 → volume in ~1.5s)
* Stop or fade-out when session ends

## Android Implementation

Use:

* `ExoPlayer` (recommended over MediaPlayer)

Example:

```kotlin
val player = ExoPlayer.Builder(context).build()
val mediaItem = MediaItem.fromUri("asset:///rain.mp3")
player.setMediaItem(mediaItem)
player.repeatMode = Player.REPEAT_MODE_ONE
player.prepare()
player.play()
```

---

# 5) Architecture (clean + scalable)

## Tech Stack

* Kotlin
* Jetpack Compose (UI)
* MVVM
* Room (local DB)
* ExoPlayer (audio)
* WorkManager (background tasks)

## Layers

### UI (Compose)

* TimerScreen
* StatsScreen
* SettingsScreen

### ViewModel

* TimerViewModel
* AudioViewModel

### Domain

* TimerManager
* SessionManager
* AudioManager

### Data

* Room DB (SessionEntity)

---

# 6) Timer Logic (core engine)

Use a **single source of truth**:

* System clock (not countdown ticks)

Example:

```kotlin
val startTime = System.currentTimeMillis()
val duration = 25 * 60 * 1000

val remaining = duration - (System.currentTimeMillis() - startTime)
```

Why:

* Prevent drift
* Survives app backgrounding

---

# 7) Background Behavior

Must support:

* Screen off
* App minimized

Use:

* Foreground Service

```kotlin
startForeground(notificationId, notification)
```

Include:

* Timer progress in notification
* Play/Pause action

---

# 8) Data Model

```kotlin
@Entity
data class SessionEntity(
    @PrimaryKey val id: Long,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val mode: String, // pomodoro / stopwatch
    val tag: String?
)
```

---

# 9) Settings

* Pomodoro duration
* Break duration
* Auto-start break
* Default white noise
* Volume

---

# 10) UI Design Guidelines

* Minimal (no clutter)
* Dark mode default
* Large typography (focus on time)
* Subtle animation (pulse timer)

Color idea:

* Focus = warm (orange/red)
* Break = cool (blue/green)

---

# 11) Edge Cases (don’t skip)

* Incoming calls → pause or continue?
* Audio interruptions (Spotify etc.)
* App killed → recover session
* Battery optimization restrictions

---

# 12) Monetization (optional)

* Free:

    * Basic timers + few sounds
* Paid:

    * Full sound library
    * Advanced stats
    * Cloud sync

---

# 13) What Most Apps Get Wrong (avoid this)

* Too many features → kills focus
* Timer drift bugs
* Bad audio looping (clicks/gaps)
* Over-designed UI

Keep it:
👉 fast
👉 predictable
👉 frictionless

---