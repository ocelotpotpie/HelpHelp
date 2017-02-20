HelpHelp
========
Better organisation for your Bukkit-compatible help system.


Features
--------
`HelpHelp` defines custom help topics from a [Markdown](https://daringfireball.net/projects/markdown/)
formatted text file, loaded from a user-specified URL. When loading this text,
`HelpHelp` automatically substitutes text variables from the start of the
file into the body of the text.

A future version (SOON<sup>TM</sup>) of `HelpHelp` will automatically
generate a web page of the help text.


Commands
--------
General admin commands:

 * `/help-load <url>` - Load help from a remote Markdown file.
   * Typically this will be a pastebin of some sort.
   * Remember to use the *raw* URL of the text, not the formatted HTML page.

Technical admin commands:
 * The following commands require access to the `HelpHelp` plugin's data
   directory to be useful.
 * `/help-dump` - Dump help to a file in the configuration directory.
 * `/help-reload` - Reload the help topics and configuration from disk.


Understanding the Bukkit Help System
------------------------------------
### Help Topics
Bukkit Minecraft servers (including Spigot) have a customisable help system
accessed through the `/help` command.

Help text is divided up into *help topics*. Help topics have an associated
permission. In order to read a topic, a player must have that permission.

There are two kinds of help topic:

 * *General Topics* have a title, a short text description and a full text
   section that contains arbitrary text. By default, the server automatically
   creates a general topic for each command, named `/commandname`. That
   topic's permission is set the same as the command permission, so players
   can only see command topics for commands that they can run.

 * *Index Topics* have a title, a preamble, and a list of topics that are
   included in that specific index. There can be as many index topics as you
   like. By default, Bukkit servers create one Index Topic *for each plugin*;
   each such topic lists all of the commands implemented by the corresponding
   plugin. The list of topics in the index is tailored according to the
   permissions of the player reading the index; the index will only list those
   topics that they can read.


### Customising Topics
The Bukkit help configuration file, `help.yml`, allows administrators to
define new general and index topics, to amend the text of existing topics
and even to amend the permission of topics to change their visibility to
players. This is a standard feature of all Bukkit servers, independent of the
`HelpHelp` plugin. The essence of `HelpHelp` is simply to make it easy to
modify `help.yml`.


### The Default Index
When a player runs `/help` with no arguments, they are shown the *Default
Index Topic*. In a default Bukkit server configuration, the Default Index Topic
lists topics for all plugins, all commands and all aliases. However, `help.yml`
can define an arbitrary Default Index Topic as an index topic named "Default".
The premise of `HelpHelp` is that a custom Default Index Topic, filled with
references to hand-written topics about the server features and rules would be
more useful to players than the automatically generated per-plugin index topics.
`HelpHelp` facilitates writing the custom general topics and the custom
Default Index Topic.


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


Permissions
-----------
 * `helphelp.load` - Permission to use `/help-load`.
 * `helphelp.dump` - Permission to use `/help-dump`.
 * `helphelp.reload` - Permission to use `/help-reload`.


Configuration
-------------
 * `boiler-plate:` is a configuration section containing YAML settings that
   are copied *verbatim* into `help.yml` prior to any generated help topics
   being added to that file.
