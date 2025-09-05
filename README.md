# CodeDocSupport

This is a little plugin for the JetBrains IntelliJ IDE. It adds an action into the "Edit -> Copy Path/Reference"
popup to generate a JavaDoc link from the current Java class.

This link can easily get embedded into Markdown documentation.

## How to install

As this plugin is currently not being published on JetBrains' Marketplace, it needs to get installed
manually:

1. Download the plugin ZIP from the build/distributions folder (alternatively, clone the project and build yourself)
2. In IntelliJ, open the "Settings -> Plugins" page and press the "Gear" icon to "Install Plugin from Disk"

## How to use

To insert a Javadoc link into the markdown documentation of your project:

1. Go to the Java class you want to refer to.
2. From the menu, select "Edit -> Copy Path/Reference...".
3. The Copy Reference Popup will appear. There, a "JDoc" option will be available which will put the link to this class into the clipboard
4. Go to the Markdown file where you want to insert the link and press Ctrl-V.

## Limitations

Currently, only classes from modules of the project where the markdown is hosted can be selected 
(The documentation rendered by the markdown plugin does not support references to libraries).

Links to methods are not supported yet.
