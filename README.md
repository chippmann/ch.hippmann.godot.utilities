# Godot Kotlin Jvm utilities
This is a small library providing common utilities which might be useful in many [godot kotlin jvm](https://github.com/utopia-rise/godot-kotlin-jvm) projects.

> **Note:** this library is basically just a collection of little things i wrote over and over in small projects and now just extracted into this common library. Consider it untested and inefficient! Feel free to use and improve it. PR's are very welcome.

## Table of Contents

- [Adding to your project](#adding-to-your-project)
- [Utilities and their usage](#utilities-and-their-usage)
    - [Autoload](#autoload)
    - [Logging](#logging)
    - [[DEPRECATED] Coroutine dispatchers](#coroutine-dispatchers)
    - [[DEPRECATED] Godot coroutine scope](#godot-coroutine-scope)
    - [[DEPRECATED] Await signals](#await-signals)
    - [[DEPRECATED] Signal callback connection](#signal-callback-connection)

## Adding to your project
Add the library as a dependency to your project. As it is published to maven central, you should also make sure that you have maven central set up as a repository:
```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.hippmann.godot:utilities:<version>")

    // if you plan on using the coroutine helpers; don't forget to add the kotlinx coroutines dependency:
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:<version>")
}
```

## Utilities and their usage
### Autoload
Just a little shorthand to access your autoload singletons:
```kotlin
val autoloadInstance = autoload<YourAutoloadClass>() // unsafe -> throws an error if the autoload is not present or not of expected type
val autoloadInstanceNullable = autoloadSafe<YourAutoloadClass>() // safe -> returns null if the autoload is not present or not of expected type

val autoloadInstanceNamed = autoload<YourAutoloadClass>("CustomNameYouSetInTheEditor") // provide a custom node name you set in the godot project settings when you added the autoload singleton
val autoloadInstanceNullableNamed = autoloadSafe<YourAutoloadClass>("CustomNameYouSetInTheEditor") // provide a custom node name you set in the godot project settings when you added the autoload singleton
```

### Logging
Logging abstraction to print logs more concisely:
```kotlin
val someException: Throwable = fromSomewhere

// Prints:
// 2024-07-26T11:16:34.424 [main] DEBUG ::HelloWorld A log message explaining why an exception occurred 
// stacktrace printed
Log.debug(someException) {
    "A log message explaining why an exception occurred"
}

// Prints:
// 2024-07-26T11:16:34.424 [main] DEBUG ::HelloWorld A log message explaining why an exception occurred 
// stacktrace printed
Log.debug(someException, "A log message explaining why an exception occurred")

// Prints:
// 2024-07-26T11:16:34.424 [main] DEBUG ::HelloWorld Just a log message
Log.debug {
    "Just a log message"
}

// Prints:
// 2024-07-26T11:16:34.424 [main] DEBUG ::HelloWorld Just a log message
Log.debug("Just a log message")
```

The above examples are present with the following severities:
- debug
- info
- warn
- err

### [DEPRECATED] Coroutine dispatchers
> :warning: **Note:** This functionality is deprecated! Switch to the official [godot-coroutine-library](https://godot-kotl.in/en/stable/user-guide/coroutines/)

Mainly provides a `Dispatchers.Main` and abstracts all dispatcher usage so they are accessed the same:

This library provides an autoload singleton you need to add to your project (remember to have it higher in the order if you use dispatchers in your own autoload singletons, so it's initialised before your autoloads!). You can find the registration file (`gdj`) in the `dependencies` directory in the `gdj` output directory of your project after you've built your project.
![Add dispatcher autoload](doc_assets/add_dispatcher_autoload.png)

You can access the dispatchers like so:
```kotlin
val mainDispatcher = mainDispatcher()
val ioDispatcher = ioDispatcher()
val defaultDispatcher = defaultDispatcher()

// example usage in `launch`:
launch(mainDispatcher()) {
    // your code running on the main thread
}
```

### [DEPRECATED] Godot coroutine scope
> :warning: **Note:** This functionality is deprecated! Switch to the official [godot-coroutine-library](https://godot-kotl.in/en/stable/user-guide/coroutines/)

Allows you to launch kotlin coroutines from within a node. Iit also provides you the means to run continuations in the context of the node (you can freely choose where, by calling `resumeGodotContinuations`).

**Note:** It is very important that if you use `withGodotContext` you also have a `resumeGodotContinuations` call in your node! Otherwise all coroutines which use `withGodotContext` will block indefinitely and cause memory leaks for good measure! 

```kotlin
@RegisterClass
class TestNode : Control(), GodotCoroutineScope by DefaultGodotCoroutineScope() {
  // by default, runs coroutines on the dispatcher `Dispatchers.Default` with a `SupervisorJob`. Errors in coroutines are propagated by throwing
  // you can however override the scope if you  want (this example is the actual default implementation):
  // OPTIONAL BLOCK: START
  override val coroutineContext: CoroutineContext =
    defaultDispatcher() + SupervisorJob() + object : CoroutineExceptionHandler {
      override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler
      override fun handleException(context: CoroutineContext, exception: Throwable) {
        throw exception
      }
    }
  // OPTIONAL BLOCK: END

  @Export
  @RegisterProperty
  lateinit var label: Label

  @Export
  @RegisterProperty
  lateinit var button: Button

  @RegisterFunction
  override fun _ready() {
    button.pressed.connect(this, TestNode::onButtonPressed)
  }

  @RegisterFunction
  override fun _process(delta: Double) {
    resumeGodotContinuations() // this resumes any continuations started with `withGodotContext`. You can place it anywhere you want to. It basically runs all pending blocks of `withGodotContext` synchronously in the order they were added
  }

  @RegisterFunction
  fun onButtonPressed() {
    launch {
      // using default dispatcher defined by DefaultGodotCoroutineScope
      // do some work
      println("BACKGROUND: Executing work on background dispatcher")

      delay(100)
      val resultOfWork = "result of work"

      withGodotContext { 
        // the code here runs the next time you call `resumeGodotContinuations`. Which in this example, is the next time `_process` is called
        println("GODOT_CONTEXT: Setting label")
        label.text = resultOfWork
      }
      delay(100)
      println("BACKGROUND: Executing more work on background dispatcher")
    }
  }
}
```

### [DEPRECATED] Await signals
> :warning: **Note:** This functionality is deprecated! Switch to the official [godot-coroutine-library](https://godot-kotl.in/en/stable/user-guide/coroutines/)

Allows you to await signal emitions inside coroutines.

> **Note:** Necessitates the setup of [Coroutine dispatchers](#coroutine-dispatchers) and implicitly applies [GodotCoroutineScope](#godot-coroutine-scope)!

> **Note:** This functionality only exists temporarily until [GH-501](https://github.com/utopia-rise/godot-kotlin-jvm/issues/501) is implemented  
> Once that's the case though, migration should be very simple

You must call `initSignalAwait` before any call to the `await` function!

Example:
```kotlin
@RegisterClass
class SignalAwaitSample: Node(), SignalAwaitable by SignalAwaiter() {
    init {
        // needs to be called before any call to `await`!
        initSignalAwait()
    }

    @RegisterFunction
    override fun _enterTree() {
        launch {
            GD.print("Before await for ${::ready.name} signal")
            ready.await()
            GD.print("Node appears to be ready according to signal await")
        }
        runBlocking { delay(1.seconds) } // to give background thread some time to run in this over simplified example
    }

    @RegisterFunction
    override fun _ready() {
        GD.print("Node is ready! Any signal await messages should appear after this line!")
    }
}
```

### [DEPRECATED] Signal callback connection
> :warning: **Note:** This functionality is deprecated! This functionality is now officially supported.

Allows you to connect to signals using lambdas.

> **Note:** This functionality only exists temporarily until this feature is officially introduced in Godot Kotlin/JVM!  
> Once that's the case though, migration should be very simple

You must call `initSignalConnectable` before any call to the `connect` function which receives a lambda!

The return type of these new `connect` functions is a custom sealed class which in the success case returns a id which identifies a given callable. This id can be used to disconnect specific lambdas if needed using the corresponding `disconnect` function. The `disconnectAll` function can be used to disconnect all connected lambdas at once.

Example:
```kotlin
@RegisterClass
class SignalConnectorSample : Node(), SignalConnectable by SignalConnector() {
  @RegisterSignal
  val customSignalWithArgs by signal<String>("someData")

  init { 
    // needs to be invoked before any call to the new connect or disconnect methods!
    initSignalConnectable()
  }

  @RegisterFunction
  override fun _enterTree() {
    ready.connect {
      GD.print("${SignalConnectorSample::class.simpleName} is ready")
    }
    customSignalWithArgs.connect(Object.ConnectFlags.CONNECT_ONE_SHOT.id) { arg ->
      GD.print("Received signal emition from ${::customSignalWithArgs.name} with arg: $arg. This signal should only be received once!")
    }
  }

  @RegisterFunction
  override fun _ready() {
    customSignalWithArgs.emit("Should be received")
    customSignalWithArgs.emit("Should NOT be received!!!")
  }
}
```
