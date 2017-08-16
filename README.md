# Ice Builder for Eclipse

The Ice Builder for Eclipse provides a plug-in which automates the compilation of [Slice](https://doc.zeroc.com/display/Ice/The+Slice+Language) files using the [Slice-to-Java](https://doc.zeroc.com/display/Ice/slice2java+Command-Line+Options) compiler and manages the resulting generated code.

An [Ice](https://github.com/zeroc-ice/ice) installation with `slice2java` version 3.4.2 or higher is required.

The Ice Builder for Eclipse plug-in provides the following features:

  - Automatically compiles all the Slice files in your project
  - Incrementally recompiles Slice files after modifications
  - Maintains dependencies between Slice files
  - Highlights compilation errors in your source code
  - Manages the generated code to remove obsolete files automatically

## Contents

- [Usage](#Usage)
  - [Installing the Plug-in](#installing-the-plug-in)
  - [Configuring the Plug-in](#configuring-the-plug-in)
  - [Activating the Plug-in for a Project](#activating-the-plug-in-for-a-project)
  - [Configuring Project Settings](#configuring-project-settings)
  - [Using the Plug-in](#using-the-plugin)
- [Building the Plug-in from Source](#building-the-plugin-from-source)

## Usage

### Installing the Plug-in

ZeroC hosts an Eclipse plug-in site that you can add to your Eclipse configuration. Follow these steps to install the `Ice Builder` plug-in:

  1. From the `Help` menu, choose `Install New Software`
  2. Click the `Add` button
  3. Enter a name in the `Name` field, such as `ZeroC`
  4. In the `Location` field, enter `http://zeroc.com/download/eclipse`
  5. Click `OK`
  6. Select the `Ice Builder` plug-in and click `Next`
  7. Click `Next` again
  8. If you agree to the license terms, check the box and click `Finish`
  9. Click `OK` if you are warned about unsigned content

Alternately, you can install the plug-in from the Eclipse Marketplace.

  1. From the `Help` menu, choose `Eclipse Marketplace`
  2. Search for `Ice Builder`
  3. Click `Install` to install the `Ice Builder` plug-in

Note: If you are upgrading from versions older than 4.0.0, you should uninstall the old plug-in before installing newer versions. For such versions,
after installation it might be necessary to manually reconfigure the `Ice Builder` settings as old settings are not automatically migrated.

### Configuring the Plug-in

The plug-in preferences can be accessed by navigating to `Window -> Preferences` and selecting `Ice Builder`.
![Preferences Dialog](/Screenshots/preferences.png)
`Ice Home` is the directory where the Ice runtime files are stored, by default it is set to the directory of your most recent Ice installation, but it can
be changed if necessary by either directly specifying a directory through the text box or by using `Browse...` to navigate to one.
An `Invalid Ice Home` error message will be displayed if the plug-in considers the specified location invalid.

`Build Automatically` allows you to choose whether Slice files should be rebuilt automatically as they're updated;
it is generally recommended to disable this option for larger projects.

### Activating the Plug-in for a Project

The plug-in is enabled on a per project basis. To enable it, right click on the project, choose `Ice Builder` and select `Add Ice Builder`.

Upon activation, the project immediately creates a `generated` directory to hold the Java source files the Slice compiler generates from your Slice files.
![Activating the Plug-in](/Screenshots/activation.png)
To de-activate the plugin, simply navigate to the `Ice Builder` menu and instead select `Remove Ice Builder`. Note that removing the Ice Builder plug-in
will not affect your Slice files, but will remove all generated code.

### Configuring Project Settings

To configure the project-specific settings, select `Properties` from the `Project` menu or right-click on the name of your project and choose `Properties`.
Click on the `Ice Builder` menu to view the plug-in's settings for the selected project.
![Project Properties Dialog](/Screenshots/properties.png)
The `Generated Code Directory` allows you to specify where all generated code should be placed in the project. By default, the plug-in uses the `generated`
directory for this. To change the directory, you must first create the new directory, and then click `Browse` to navigate to it. The new directory must be empty,
and the plug-in requires exclusive use of it; any files or changes made to the directory will be lost when the Slice compiler is run.

Any external Slice files your project is dependent upon can be specified using the `Include Directories` interface, where you can add directories 
the compiler can search for referenced Slice definitions. These directories are searched in the order they're listed in the interface.

For more advanced users, additional options can be passed directly to the Slice compiler with the `Additional Options` section. For a list of all supported
options and their descriptions, you can reference the `slice2java` command line section of the [Ice Manual](https://doc.zeroc.com/display/Ice/slice2java+Command-Line+Options).

### Compiling Slice Files

Slice files can be manually compiled through Eclipse or by using the `Compile` Button under the `Ice Builder` project menu.

Or if automatic building is enabled, Slice files will be compiled if either of the following are true:
 * Any Slice files in the project have been updated since the last compilation through the plug-in (this includes removing Slice files from projects).
 * The options used to compile Slice files have changed.

During a full build, the Ice Builder plug-in automatically searches all directories within your project's scope for Slice files, and compiles all of them, in addition to removing any obsolete generated code.

For incremental builds, only those Slice files that have changed, and those that directly or indirectly reference them, are recompiled, and their corresponding generated code updated.

## Building the Plug-in from Source

In order to build the plug-in, you will need an installation of Eclipse suitable for plug-in development.

It will be necessary to install the Eclipse Plug-in Development Environment
if it is not already installed. Go to `Help > Eclipse Marketplace` and search
for `Eclipse PDE`, then click `Install` to install the Eclipse PDE.

To import the project choose `File > Import > General > Existing Projects into Workspace`
and select the java directory in this repository as the root directory.

To create the plug-in, use `File > Export > Plug-in Development > Deployable plug-ins and fragments`.
