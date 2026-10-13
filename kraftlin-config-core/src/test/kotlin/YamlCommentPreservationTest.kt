@file:Suppress("unused")

package io.github.kraftlin.config

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

internal class YamlCommentPreservationTest {

    @TempDir
    lateinit var dir: Path

    // ========================= Round trip =========================

    @Test
    fun `unchanged file is saved byte for byte`() {
        @Language("yaml")
        val yaml = """
            |# My plugin
            |#   indented header line
            |
            |# Comment for first
            |first: 1.10 # inline first
            |second: '007'
            |third: "quoted"
            |
            |# Comment for list
            |list:
            |- # comment on item a
            |  a # inline a
            |- b
            |section:
            |  # nested comment
            |  nested: True
            |  empty: ~
            |flow: {x: 1, y: [1, 2]} # flow inline
            |# comment after section
            |text: |
            |  literal
            |  text
            |last: x
            |
            |# end of file
            |""".trimMargin()
        val file = write(yaml)

        wrapConfig(file).save()

        assertEquals(yaml, Files.readString(file))
    }

    @Test
    fun `changed value keeps key comments and inline comment`() {
        val file = write("# Max players\nmax: 10 # per world\nother: 'x'\n")
        val wrapper = wrapConfig(file)

        wrapper.set("max", 20)
        wrapper.save()

        assertEquals("# Max players\nmax: 20 # per world\nother: 'x'\n", Files.readString(file))
    }

    @Test
    fun `changed section keeps the comments of its key`() {
        val file = write("# Section\nsection:\n  a: 1\nafter: x\n")
        val wrapper = wrapConfig(file)

        wrapper.set("section", mapOf("b" to 2))
        wrapper.save()

        assertEquals("# Section\nsection:\n  b: 2\nafter: x\n", Files.readString(file))
    }

    @Test
    fun `nested comments survive a change of a sibling`() {
        val file = write("section:\n  # first nested\n  one: 1\n\n  # second nested\n  two: 2\n")
        val wrapper = wrapConfig(file)

        wrapper.set("section.one", 10)
        wrapper.save()

        val reloaded = wrapConfig(file)
        reloaded.addDefault("section.one", 0)
        assertEquals(10, reloaded.getInt("section.one"))
        assertEquals(listOf("first nested"), reloaded.getComments("section.one"))
        assertEquals(listOf("second nested"), reloaded.getComments("section.two"))
    }

    @Test
    fun `comments on list items survive a change elsewhere`() {
        val file = write("value: 1\nlist:\n- # about a\n  a # inline a\n- b\n")
        val wrapper = wrapConfig(file)

        wrapper.set("value", 2)
        wrapper.save()

        assertEquals("value: 2\nlist:\n- # about a\n  a # inline a\n- b\n", Files.readString(file))
    }

    @Test
    fun `new key gets developer comment and existing comments are kept`() {
        val file = write("# Header\n\n# Operator note\nexisting: 'x' # keep\n")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val existing by config("existing", "default", "Developer existing")
            val added by config("added", 5, "Developer added")
        }

        config.saveDefaults()

        assertEquals(
            "# Header\n\n# Operator note\nexisting: 'x' # keep\n# Developer added\nadded: 5\n",
            Files.readString(file)
        )
    }

    @Test
    fun `removed key drops its comments and keeps the others`() {
        val file = write("# Keep me\nkeep: 1\n\n# Legacy setting\nlegacy: 2\n# Also keep\nafter: 3\n")
        val wrapper = wrapConfig(file)

        wrapper.remove("legacy")
        wrapper.save()

        assertEquals("# Keep me\nkeep: 1\n# Also keep\nafter: 3\n", Files.readString(file))
    }

    @Test
    fun `end of file comments are kept when values change`() {
        val file = write("a: 1\n\n# end of file\n")
        val wrapper = wrapConfig(file)

        wrapper.set("a", 2)
        wrapper.save()

        assertEquals("a: 2\n\n# end of file\n", Files.readString(file))
    }

    @Test
    fun `in-line comment stays behind a value with line breaks`() {
        val file = write("key: 'a' # about key\nother: 1\n")
        val wrapper = wrapConfig(file)

        wrapper.set("key", "line1\nline2")
        wrapper.save()

        assertEquals("key: \"line1\\nline2\" # about key\nother: 1\n", Files.readString(file))
    }

    @Test
    fun `keys added to a flow mapping are written in block style`() {
        val file = write("section: {}\n")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val a by config("section.a", 1, "About a")
        }

        config.saveDefaults()

        assertEquals("section:\n  # About a\n  a: 1\n", Files.readString(file))
    }

    @Test
    fun `flow mapping with an in-line comment gets new keys`() {
        val file = write("limits: {min: 1} # bounds\n")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val min by config("limits.min", 0, "Minimum")
            val max by config("limits.max", 10, "Maximum")
        }

        config.saveDefaults()

        assertEquals("limits: # bounds\n  # Minimum\n  min: 1\n  # Maximum\n  max: 10\n", Files.readString(file))
    }

    @Test
    fun `developer comments turn a flow mapping into a block mapping`() {
        val file = write("limits: {min: 1, max: 5}\n")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val min by config("limits.min", 0, "Minimum")
            val max by config("limits.max", 10, "Maximum")
        }

        config.saveDefaults()

        assertEquals("limits:\n  # Minimum\n  min: 1\n  # Maximum\n  max: 5\n", Files.readString(file))
    }

    @Test
    fun `a block mapping turns the flow mappings around it into block mappings`() {
        val file = write("outer: {inner: {x: 1}}\n")
        val wrapper = wrapConfig(file)

        wrapper.set("outer.inner.y", 2)
        wrapper.setComments("outer.inner.y", listOf("About y"))
        wrapper.save()

        assertEquals("outer:\n  inner:\n    x: 1\n    # About y\n    y: 2\n", Files.readString(file))
    }

    @Test
    fun `flow mapping containing comments is written in block style`() {
        val file = write("flow: {\n  # about a\n  a: 1\n}\n")

        wrapConfig(file).save()

        assertEquals("flow:\n  # about a\n  a: 1\n", Files.readString(file))
    }

    @Test
    fun `in-line comment of a section moves behind a value written on one line`() {
        val file = write("section: # about\n  a: 1\n")
        val wrapper = wrapConfig(file)

        wrapper.set("section", "x")
        wrapper.save()

        assertEquals("section: x # about\n", Files.readString(file))
    }

    @Test
    fun `setting the value a key already has keeps its formatting and comments`() {
        val yaml = "name: 'x' # inline\nitems:\n- # about a\n  a\n- b\n"
        val file = write(yaml)
        val wrapper = wrapConfig(file)

        wrapper.set("name", "x")
        wrapper.set("items", listOf("a", "b"))
        wrapper.save()

        assertEquals(yaml, Files.readString(file))
    }

    @Test
    fun `long values are not wrapped`() {
        val message = "&aWelcome to the server, please read the rules at https://example.com before you start playing!"
        val words = List(20) { "word$it" }.joinToString(" ")
        val file = write("message: '$message' # shown on join\nother: 1\n")
        val wrapper = wrapConfig(file)

        wrapper.set("other", words)
        wrapper.save()

        assertEquals("message: '$message' # shown on join\nother: $words\n", Files.readString(file))
    }

    @Test
    fun `aliases are written as copies`() {
        val file = write("base: &b {x: 1}\ncopy: *b\nlist: &l [1, 2]\nboth: [*l, *l]\n")

        wrapConfig(file).save()

        assertEquals("base: {x: 1}\ncopy: {x: 1}\nlist: [1, 2]\nboth: [[1, 2], [1, 2]]\n", Files.readString(file))
    }

    // ========================= Header =========================

    @Test
    fun `header is not part of the first key comments`() {
        val wrapper = wrapConfig(write("# Header\n#   indented\n\n# First\nfirst: 1\n"))
        assertEquals(listOf("First"), wrapper.getComments("first"))
    }

    @Test
    fun `only comment lines after the last blank line belong to the first key`() {
        val wrapper = wrapConfig(write("# Header\n\n# More header\n\nfirst: 1\n"))
        assertEquals(emptyList(), wrapper.getComments("first"))
    }

    @Test
    fun `header stays on top when the first key is removed`() {
        val file = write("# Header\n\n# First\nfirst: 1\n# Second\nsecond: 2\n")
        val wrapper = wrapConfig(file)

        wrapper.remove("first")
        wrapper.save()

        assertEquals("# Header\n\n# Second\nsecond: 2\n", Files.readString(file))
    }

    @Test
    fun `removing the first key leaves no extra blank lines at the top`() {
        val withHeader = write("# Header\n\nfirst: 1\n\n# Second\nsecond: 2\n")
        val withoutHeader = write("first: 1\n\n# Second\nsecond: 2\n")

        for (file in listOf(withHeader, withoutHeader)) {
            wrapConfig(file).apply { remove("first"); save() }
        }

        assertEquals("# Header\n\n# Second\nsecond: 2\n", Files.readString(withHeader))
        assertEquals("# Second\nsecond: 2\n", Files.readString(withoutHeader))
    }

    @Test
    fun `header above a flow mapping is kept`() {
        val file = write("# Header\n\n{a: 1}\n")

        wrapConfig(file).save()

        assertEquals("# Header\n\n{a: 1}\n", Files.readString(file))
    }

    @Test
    fun `comments of a file without keys are written once`() {
        val file = write("# Header\n\na: 1\n\n# End\n")
        wrapConfig(file).apply { remove("a"); save() }

        repeat(3) { wrapConfig(file).save() }

        assertEquals("# Header\n\n{}\n\n# End\n", Files.readString(file))
    }

    @Test
    fun `comments below an empty mapping stay below new keys`() {
        val file = write("{}\n# End\n")
        val wrapper = wrapConfig(file)

        wrapper.set("k", 1)
        wrapper.save()

        assertEquals("k: 1\n# End\n", Files.readString(file))
    }

    @Test
    fun `file without keys and with an in-line comment is saved`() {
        val unchanged = write("{} # inline\n")
        val changed = write("--- # inline\n{}\n")

        wrapConfig(unchanged).save()
        wrapConfig(changed).apply { set("k", 1); save() }

        assertEquals("{}\n", Files.readString(unchanged))
        assertEquals("k: 1\n", Files.readString(changed))
    }

    @Test
    fun `header stays on top when the first key gets a developer comment`() {
        val file = write("# Header\n\nfirst: 1\n")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val first by config("first", 0, "Developer first")
        }

        config.saveDefaults()

        assertEquals("# Header\n\n# Developer first\nfirst: 1\n", Files.readString(file))
    }

    @Test
    fun `comments of a file without keys become the header`() {
        val file = write("# Only a header\n")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val a by config("a", 1, "Developer a")
        }

        config.saveDefaults()

        assertEquals("# Only a header\n\n# Developer a\na: 1\n", Files.readString(file))
    }

    // ========================= getComments / setComments =========================

    @Test
    fun `getComments keeps indentation inside comments`() {
        val wrapper = wrapConfig(write("#   indented\n#no space\n# normal\nkey: v\n"))
        assertEquals(listOf("  indented", "no space", "normal"), wrapper.getComments("key"))
    }

    @Test
    fun `setComments replaces only the key's own comments`() {
        val file = write("a: 1\n\n# old\nb: 2\n")
        val wrapper = wrapConfig(file)

        wrapper.setComments("b", listOf("new"))
        wrapper.save()

        assertEquals("a: 1\n\n# new\nb: 2\n", Files.readString(file))
    }

    @Test
    fun `setComments keeps the header`() {
        val file = write("# Header\n\n# old\nfirst: 1\n")
        val wrapper = wrapConfig(file)

        wrapper.setComments("first", listOf("new"))
        wrapper.save()

        assertEquals("# Header\n\n# new\nfirst: 1\n", Files.readString(file))
    }

    @Test
    fun `setComments splits entries on line breaks`() {
        val file = write("key: v\n")
        val wrapper = wrapConfig(file)

        wrapper.setComments("key", listOf("one\ntwo", "three\r\nfour"))
        wrapper.save()

        assertEquals("# one\n# two\n# three\n# four\nkey: v\n", Files.readString(file))
        assertEquals(listOf("one", "two", "three", "four"), wrapConfig(file).getComments("key"))
    }

    @Test
    fun `comment indentation and empty lines round-trip`() {
        val file = write("key: v\n")
        val wrapper = wrapConfig(file)

        wrapper.setComments("key", listOf("Example:", "  indented", ""))
        wrapper.save()

        assertEquals("# Example:\n#   indented\n#\nkey: v\n", Files.readString(file))
        assertEquals(listOf("Example:", "  indented", ""), wrapConfig(file).getComments("key"))
    }

    @Test
    fun `key comments are kept when a list value is replaced`() {
        val file = write("# Worlds\nworlds: [] # none yet\n")
        val wrapper = wrapConfig(file)

        wrapper.set("worlds", listOf("world"))
        wrapper.save()

        assertEquals("# Worlds\nworlds:\n- world\n", Files.readString(file))
    }

    private fun write(@Language("yaml") yaml: String): Path {
        val file = dir.resolve("config-${System.nanoTime()}.yml")
        Files.writeString(file, yaml)
        return file
    }
}
