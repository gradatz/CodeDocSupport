# CodeDocSupport

This is a little plugin for the JetBrains IntelliJ IDE. It adds an action into the "Edit -> Copy Path/Reference"
popup to generate a JavaDoc link from the current Java class (using a special `jdoc://` URL format).

This link can easily get embedded into Markdown documentation.

## How to install

Download the plugin from JetBrains' Marketplace.

1. In IntelliJ, open the "Settings -> Plugins" page
2. On the "Marketplace" tab, search for "CodeDocSupport" and install it.

## How to use

To insert a Javadoc link into the markdown documentation of your project:

1. Go to the Java class or method name you want to refer to.
2. From the menu, select "Edit -> Copy Path/Reference...".
3. The Copy Reference Popup will appear. There, a "JDoc" option will be available. 
4. Press this menu to copy the link to the selected class/method into the clipboard.
5. Go to the Markdown file where you want to insert the link and press Ctrl-V.

## Folding

Since the `jdoc://` links can become very long, the plugin will fold them by default into a much more compact text.
As always within JetBrains IDEs, you can unfold/fold again by pressing `Ctrl-<Keypad Plus>` or  `Ctrl-<Keypad Minus>` keys.

## Limitations

Classes which belong to another module than the markdown where you want to put the reference, need to be declared as depenency in the project's
Maven `pom.xml` (either as direct `dependency` or in the `dependencyManagement` section of the parent POM).

Only classes which are part of the production code source can be referenced (that is, no test code).

## Roadmap

Planned features include:

* Highlight invalid jdoc-links as errors
* Support java rename-refactorings and changes in method signature (i.e. rename/change the jdoc-link as well)

