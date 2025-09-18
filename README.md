# HologramLib

A packet-based hologram library for Bukkit/Spigot plugins, designed for efficiency using movement listeners instead of server ticks.

## Features

- Create and manage holograms with minimal performance impact
- Generic `HologramManager<InternalIdType>` for custom internal ID types
- Text holograms with dynamic metadata updates
- Placeholder support via PlaceholderAPI (non-ticking by default)
- No reliance on scheduled tasks for moving/updating holograms
## Installation

### Gradle
Add the repository:
```groovy
repositories {
    maven { url = uri("https://repo.tcoded.com/releases/") }
}
```
Add the dependency:
```groovy
dependencies {
    implementation("com.tcoded:HologramLib:1.3.3")
}
```
(REQUIRED) Shade with Shadow plugin to avoid package conflicts:
```groovy
plugins {
    id ("com.gradleup.shadow") version '9.1.0'
}
shadowJar {
    relocate("com.tcoded.hologramlib", "your.plugin.package.hologramlib")
}
```

### Maven
```xml
<repositories>
  <repository>
    <id>tcoded-releases</id>
    <url>https://repo.tcoded.com/releases/</url>
  </repository>
</repositories>
<dependencies>
  <dependency>
    <groupId>com.tcoded</groupId>
    <artifactId>HologramLib</artifactId>
    <version>1.3.3</version>
  </dependency>
</dependencies>
```
Shade with Maven Shade Plugin:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>3.6.1</version>
  <executions>
    <execution>
      <phase>package</phase>
      <goals><goal>shade</goal></goals>
      <configuration>
        <relocations>
          <relocation>
            <pattern>com.tcoded.hologramlib</pattern>
            <shadedPattern>your.plugin.package.hologramlib</shadedPattern>
          </relocation>
        </relocations>
      </configuration>
    </execution>
  </executions>
</plugin>
```

## Quick Start

### Initialization
In your main `JavaPlugin` class:
```java
public class MyPlugin extends JavaPlugin {
    private HologramLib hologramLib;

    @Override
    public void onEnable() {
        hologramLib = new HologramLib(this);
    }
}
```

### Hologram Manager
Obtain a manager with your internal ID type (e.g., `Location`):
```java
import com.tcoded.hologramlib.manager.HologramManager;
HologramManager<Location> manager = hologramLib.getHologramManager();
```
The internal ID type must have a proper `equals()` and `hashCode()` implementation for uniqueness.

### Creating a Text Hologram
```java
import org.bukkit.Location;
import net.kyori.adventure.text.Component;
import com.tcoded.hologramlib.hologram.TextHologram;
import com.tcoded.hologramlib.hologram.TextHologramLine;

// Create a new hologram instance
/* Location loc = new Location(getServer().getWorld("world"), x, y, z); */
TextHologram<Location> hologram = manager.create(loc);
hologram.setLocation(loc);

// Set initial text line
TextHologramLine firstLine = hologram.getLine(0);
firstLine.getMeta().setText(Component.text("Hello"));

// Add another line
TextHologramLine secondLine = manager.createLine();
secondLine.getMeta().setText(Component.text("World"));
hologram.addLine(secondLine);

// Show hologram to all tracked players
hologram.show();
```

### Managing Lines
```java
import net.kyori.adventure.text.Component;
import com.tcoded.hologramlib.hologram.TextHologramLine;

// Add a new line
TextHologramLine newLine = manager.createLine();
newLine.getMeta().setText(Component.text("Another line"));
hologram.addLine(newLine); // This will automatically show if isVisible()

// Get a lines (by index, or all)
hologram.getLine(0);
hologram.getLines();

// Remove a line (by index)
hologram.removeLine(1);
```

### Editing Metadata
```java
import net.kyori.adventure.text.Component;

// Change text of first line
hologram.getLine(0).getMeta().setText(Component.text("Updated Text"));

// Adjust vertical offset (height) of first line
hologram.getLine(0).setHeight(0.5);

// Apply metadata changes to all tracked players
hologram.updateMeta();
```

### Removing Holograms
```java
// Remove a specific hologram by its internal ID
manager.killAndRemove(hologram.getInternalId());

// Or remove all holograms
manager.killAndRemoveAll();
```

## Frequently Asked Questions (FAQ)

### What is `<InternalIdType>`?
As you have likely noticed, HologramLib takes a generic `<InternalIdType>`.
This is a unique key/identifier for each hologram you create.
Instead of requiring all your projects to keep track of holograms themselves, HologramLib has an internal mapping of ID to Hologram instance. The internal Map implementation is a `ConcurrentHashMap` making it safe to use in multithreaded environments.
- (Disable) Don't want to use this?
    -  You can simply use an incrementing `Integer` or `UUID` for each hologram you create.
- (Easy) Want to tie holograms to locations?
    - Use `Location` as your InternalIdType for position-based holograms. You can now create and retrieve Holograms based on a `Location`.
- (Advanced) Use a unique identifier for each hologram:
    - NPC or custom object with stable `hashCode()` and `equals()`. This approach makes it easy to update holograms if the object it is tied to changes without requiring you to write another mapping.

### How can I make placeholders update?
HologramLib does not tick holograms by default. 
To update the placeholders, you will need to tick the holograms that require updates. 
Simply create a repeating async task and call `hologram.updateMeta()`.
This method will re-calculate the text, and therefore placeholders in the hologram's lines.
```java
// Example: Update all holograms every second using FoliaLib
// https://github.com/TechnicallyCoded/FoliaLib

private FoliaLib foliaLib;
private HologramLib<UUID> hologramLib;

@Override
public void onEnable() {
    // FoliaLib Setup
    foliaLib = new FoliaLib(this);
    PlatformScheduler scheduler = foliaLib.getScheduler();
    
    // HologramLib Setup
    hologramLib = new HologramLib<>(this);
    
    // HologramLib Ticking
    Consumer<WrappedTask> updater = task -> hologramManager.getHolograms().forEach(Hologram::updateMeta);
    scheduler.runTimerAsync(updater, 20L, 20L);
}
```

## License
[ARR License](LICENSE)
