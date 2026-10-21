# LumiFollow

Reusable Citizens NPC pursuit library, extracted from LumiDrugs' cartel enforcers.
It has no dependency on LumiDrugs, drugs, quests, economies, skins, or NPC registries.
Java 21, Paper 1.21.10 API, and a compatible Citizens installation are required.
The library is shaded into the consuming plugin; it is not a server plugin to install separately.

## Build and dependency

From the repository root:

```sh
./gradlew clean build
```

This builds the independent library at `lumifollow/build/libs/lumifollow-1.0.0.jar`
and the plugin at `lumidrugs/build/libs/LumiDrugs.jar`. To build/install only the library:

```sh
./gradlew :lumifollow:build :lumifollow:publishToMavenLocal
```

After `:lumifollow:publishToMavenLocal`, a Gradle consumer can use:

```groovy
repositories { mavenLocal() }
dependencies { implementation 'io.github.saboooor:lumifollow:1.0.0' }
```

Keep Paper and Citizens as `compileOnly` dependencies in the consumer. Maven consumers can use:

```xml
<dependency>
    <groupId>io.github.saboooor</groupId>
    <artifactId>lumifollow</artifactId>
    <version>1.0.0</version>
</dependency>
```

These coordinates are not published to Maven Central. Each build machine must install the
library first, or you can publish this Gradle module to your own Maven repository.
Build the library alone with `./gradlew :lumifollow:build` from the repository root.
Use `gradlew.bat` instead of `./gradlew` on Windows.

Use Gradle Shadow (or Maven Shade in a Maven consumer) to bundle it and relocate `io.github.saboooor.lumifollow` to a private package
such as `your.plugin.internal.lumifollow`. Keep Paper and Citizens as `provided` dependencies.
Declare `depend: [Citizens]` in `plugin.yml`, or soft-depend on Citizens and instantiate this
library only when Citizens is available. LumiDrugs demonstrates the latter arrangement.

## Quest plugin example

```java
import io.github.saboooor.lumifollow.FollowController;
import io.github.saboooor.lumifollow.FollowOptions;
import io.github.saboooor.lumifollow.LumiFollow;

// In onEnable(), after checking Citizens is enabled:
LumiFollow following = new LumiFollow(this);

// Use an NPC your quest plugin created/owns:
FollowController escort = following.follow(npc, player, FollowOptions.defaults()
        .withDoors(true)
        .withClimbing(true, 6, 100)
        .withRecovery(true, true, true));

// The NPC waits within stopDistance and resumes when the player moves away.
// Keep this session running throughout the quest; no repeating follow() calls are needed.
escort.stop();                      // Pause; escort.follow(player) resumes.
following.release(npc);              // Dispose its controller before destroying the NPC.
// In onDisable():
following.close();                   // Cancels tasks and unregisters event listeners.
```

Create one `LumiFollow` service per owning plugin. All API calls must run on the server main
thread. Controllers are reused by NPC identity, so repeated `follow` calls do not create
duplicate tasks. Options remain fixed for the lifetime of a controller: call `release(npc)`
and request another controller to replace its settings. The library never creates or destroys
NPCs and never changes their skins, equipment, quest state, or registry ownership.

## Policies and behavior

`FollowOptions` is an immutable record. Use its full constructor to customize walk/sprint
speed multipliers, stop distance, path range, path refresh interval, stationary timeout,
door handling, climbing height/timeout, teleport recovery, target-teleport following,
cross-world following, and teleport sounds. The `withDoors`, `withClimbing`, and
`withRecovery` methods offer shortcuts while preserving other settings.

Defaults preserve enforcer pursuit: speed 1.15 / 1.5 when the player sprints away, stop at
2.5 blocks, range 64, paths refreshed every 10 ticks, stationary timeout 20 ticks,
doors enabled, climbing up to 6 blocks for at most 100 ticks, and teleport recovery enabled.
Citizens handles ordinary navigation. Bounded velocity assistance handles walls, safe drops,
and failed-path approaches. This does not add block breaking or arbitrary parkour.

The service handles player teleports/world changes, quit/death, NPC chunk unloads, and owner
or Citizens shutdown. Quit/death and invalid NPCs stop pursuit; plugin shutdown disposes all
controllers. NPC destruction remains the caller's responsibility. With cross-world following
disabled, changing worlds stops pursuit. With teleport recovery disabled, pursuit never
teleports automatically. Disabling `followTeleports` skips the teleport-event response;
distance/world recovery can still apply if those policies are enabled.

Recovery searches loaded chunks for safe ground and respects the world border. If no safe
landing exists it returns false and retries later; it never falls back to an unchecked player
position. `SafeLocations.findNear(...)` is also available for initial NPC spawning.
`FollowController.teleport(location)` is an explicit caller-controlled teleport, so the caller
must validate its destination. Teleport cancellation is respected.

Door opening changes blocks directly, as in the original enforcer implementation. For NPCs
that should leave doors alone, use `withDoors(false)`. Climbing assumes a player-sized body.

## License

GPL-3.0, retained from the source project; see `LICENSE`.
