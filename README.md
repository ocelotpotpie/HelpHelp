HelpHelp
========

Better organisation for your Bukkit-compatible help system.


File Format
-----------
### Sections
The file is divided into three sections, separated by three or more `=` 
characters at the start of a line, on their own.

The first section is YAML code that defines text substitutions for other parts
of the document.

The second section defines the main help index. The bullet list in this section
defines the index help topic in the order that topics appear. Any help topics
not listed here will not appear in the index.

The third section defines all help topics that are not defined by plugins.
The topic name should be a third-level heading.

### Short Text and Preambles
In index help topics, the first block quote (marked up with `>`) after the 
heading defines the topic's preamble.

Similarly, in general topics, the first block quote after the heading defines
the topic's short text.


### Amended Permission
An amended permission to read a help topic can be specified by marking up the
permission as code in backticks between the topic heading and the block quote
that defines its short text or preamble.

For example:

```
# Mods
`modmode.toggle`
> Special information for moderators only.
```

### Headings
Currently headings are assumed to mark the start of a new (general or index)
topic. Consequently, you cannot use headings as formatting *within* a topic.
This may change in a later version of `HelpHelp`.

### Markdown

`HelpHelp` supports a reasonably complete variant of standard Markdown syntax,
with a couple of extensions provided by the [CommonMark](http://commonmark.org/)
Markdown implementation, and a few small limitations.

Markdown is a simple, standard document format for writing text documents that
can be read easily either as plain text or converted automatically into HTML.
The format is supported by GitHub for README.md files describing projects and
is the format of the document you are now reading.

Here are a couple of links to Markdown primers to get you started:

 * https://help.gamejolt.com/markdown
 * https://daringfireball.net/projects/markdown/syntax

`HelpHelp` has at least the following limitations in dealing with Markdown:

 * It doesn't support tables.
 * It doesn't support images or named links. To link to a web page, simply
   include the URL in the Markdown code. `HelpHelp` will recognise that it is
   a link and format it accordingly.
 * There may be other limitations of which I am not aware, simply because I
   haven't thought to test that markup.

In addition to standard Markdown syntax for bold, italics, strikeout, block
quotes and inline code, `HelpHelp` also supports underlined text, marked up
using doubled '+' characters, i.e. `++underlined text++`.

You may find the following site useful for checking that your markup code is
valid Markdown: http://dillinger.io/. The site also understands the `++`
syntax for underlined text.
