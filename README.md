# Message
### Declaration style message builder for kotlin


Works with Adventure chat AIP: https://github.com/KyoriPowered/adventure

The library is bundled with Paper, but requires loading/shading with vanilla Spigot and BungeeCord.

## Examples

```kotlin

import net.kyori.adventure.text.format.NamedTextColor

val simpleMessage = message {
    text("Hello World!")
}

val complexMessage = message {
    text("Complicated ") {
        color(NamedTextColor.BLUE)
        strikeThrough()
        underlined()
        obfuscated()
        bold()
        italic()
    }
    text("Multipart message ") {
        color(NamedTextColor.GREEN)
        hoverMessage {
            text("First part ") {
                color(NamedTextColor.GOLD)
                bold()
            }
            text("second part") {
                color(NamedTextColor.DARK_GRAY)
            }
        }
        runCommand("/say hello!")
    }
    text("with text formatting ") {
        underlined()
        openUrl("https://www.minecraft-asylum.de/")
    }
    text("and actions") {
        color(NamedTextColor.BLACK)
        insert("Fancy String")
        hoverMessage("Simple Text") {
            color(NamedTextColor.RED)
        }
    }
}
```


## POM

```xml
<dependencies>
    <dependency>
        <groupId>me.minoneer.minecraft.message</groupId>
        <artifactId>message</artifactId>
        <version>${message.version}</version>
        <scope>compile</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <artifactId>maven-shade-plugin</artifactId>
            <version>${shadePlugin.version}</version>
            <configuration>
                <minimizeJar>false</minimizeJar>
                <createDependencyReducedPom>false</createDependencyReducedPom>
                <artifactSet>
                    <includes>
                        <include>me.minoneer.minecraft.message:message</include>
                    </includes>
                </artifactSet>
                <relocations>
                    <relocation>
                        <pattern>me.minoneer.minecraft.message</pattern>
                        <shadedPattern>me.minoneer.minecraft.project.lib.message</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```


## Comparison to md_5 builder

```kotlin

import net.kyori.adventure.text.format.NamedTextColor

val builder = ComponentBuilder("").append("Test").color(ChatColor.RED).event(
    HoverEvent(
        HoverEvent.Action.SHOW_TEXT,
        ComponentBuilder("Hover Text").color(ChatColor.DARK_GRAY).italic(true).create()
    ).event(
        ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/foo bar")
    )
).create()

val message = message {
    text("Test") {
        color(NamedTextColor.RED)
        hoverMessage("Hover Text") {
            color(NamedTextColor.DARK_GRAY)
            italic()
        }
        suggestCommand("/foo bar")
    }
}
```
