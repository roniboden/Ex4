# OOP Coding Style Verifier

**OOP Coding Style Verifier** is a code style test for (most of) the code style guidelines of HUJI's Object Oriented Programming course.
This test uses the [Checkstyle Java Code Quality Tool](https://checkstyle.sourceforge.io/) to ensure a set of rules consisting of the course's code style guidelines apply.
Make sure to pull changes every so often, as there may be bugs I'm not aware of, and I'll push fixes ASAP when I find out about them.


## Setup (make sure to follow these instrutions after every git pull if changes were made!)
1. Make sure you have git installed and added to your path: You can download git from [here](https://git-scm.com/downloads), follow the guide in the first comment [here](https://stackoverflow.com/questions/4492979/error-git-is-not-recognized-as-an-internal-or-external-command) to add git to your path in Windows and follow the guide [here](https://graphite.dev/guides/how-to-resolve-the-error-git-is-not-recognized-on-mac) (3. Ensure Git is in your PATH) to add git to your path in Mac. If your using Linux you probably don't need any help here.
   
2. Clone this repository to a local directory: Open a terminal in the directory where you store your OOP project directories, and run
   ```
   git clone https://github.com/david-zvi/OOPCodingStyleVerifier.git
   ```
   You can clone the repository into any directory you wish, but I'd reccomend to clone it into the directory containing all of your OOP project directories in them to make it easier to use.

3. If you use an IDE to write your code (such as IntelliJ), make sure it uses tabs for indentation instead of spaces (this test will raise lots of errors if spaces are used). To use tabs instead of spaces for indentation in IntelliJ:

    a. Enable File -> Settings -> Editor -> Code Style -> Java -> Tabs and Indents -> Use tab character
   
    b. Disable File -> Settings -> Editor -> Code Style -> Detect and use existing file indents for editing (this makes it so if indents by spaces already exist in the file, tabs will be spaces no matter what).

If you're using IntelliJ, you can change all existing indentations in a file from using spaces to tabs by selecting all text in the file (ctrl/cmd + A), and then pressing (ctrl/cmd)+(alt/option)+L.
   
4. Adjust test to match your tab size: In the tabWidth property of the Checker module in checkstyle.xml, set the value to your tab size (line 9). In IntelliJ, the tab size is configured in File -> Settings -> Editor -> Code Style -> Java -> Tabs and Indents -> Tab Size.

**IMPORTANT:** Make sure to run ```git pull``` from the directory you ran ```git clone``` from and follow steps 3-4 again every so often, I'm fixing bugs as they arise and you'll need this to get the latest version. If when running ```git pull``` you get ```Already up to date.``` there's no need to do anything.


## How to run
Open a terminal, and run this command (replace the '\\' characters with '/' on Linux/Mac):
```
java -jar <path-to-cloned-directory>\checkstyle-10.21.4-all.jar -c <path-to-cloned-directory>\checkstyle.xml <path-to-your-code-directory>
```

If you followed the recommendation in step 2 of setup, this command should work when running this from your project's directory:
```
java -jar ..\OOPCodingStyleVerifier\checkstyle-10.21.4-all.jar -c ..\OOPCodingStyleVerifier\checkstyle.xml .
```


## What this test does not cover
- Ensure abbreviations and acronyms are not used in names.
- Ensure logically illegal names that do not follow the naming convention are not used: For example, 'checkandReport' will not be detected as an invalid method name since it follows camelCase for the words 'checkand' and 'report', even though that makes no sense and should be 'checkAndReport', and 'and' is not considered a word in the method name for the same reason.
- Ensure the word 'and' does not appear in method comments.
- Ensure proper logic and accuracy of comment contents.
- Ensure documentation tags appear at the end of JavaDocs. Even though this is pretty intuitive to do ourselves, and JavaDocs are generated with them at the end, it was specifically mentioned to be important in the code style guidelines, so make sure all your JavaDocs follow this!
- Ensure non-tag class JavaDoc content is valid: Short purpose description, detailed use description, no documentation of class implementation.
- Ensure class JavaDoc references related classes and methods with @see.
- Ensure method JavaDocs explain only what the method does and not how it does it, sentence at the beginning of ends with a period ('.').
- Ensure correct order for method @param tags - they are generated in this order automatically when generating the JavaDoc with /**.
- Allow two related parameters to be used in the same @param tag - the test does not support this, and when generating the JavaDoc with /** separate @param tags are generated for each parameter. If you really want to have two parameters in a single @param, ignore this error when running the test and double check all parameters are documented in @param tags to make sure.
- Ensure some tags are followed by descriptions - the only tags supported by Checkstyle for this are @param, @return, @throws, @exception, and @deprecated, which will raise an error when not followed by a description.

**Important note:** This test ensures the recommended method limitations (line limit of 40 and parameter limit of 4) are met. This is not strictly forbidden in the course, but rather a recommendation to follow in the coding style guidelines, since if a method crosses one of these, it can probably be adjusted to stay within them by splitting the method up or encapsultaing its parameters into objects.


### Legal
This repository includes an [original, unchanged jar file](https://github.com/david-zvi/OOPCodingStyleVerifier/blob/main/checkstyle-10.21.4-all.jar) of [Checkstyle's 10.21.4 release](https://github.com/checkstyle/checkstyle/releases/tag/checkstyle-10.21.4) in order to make the setup process easier for students wanting to run this test and are less familiar with the process of installing programs from GitHub.
Checkstyle is licensed under the [GNU-LGPL-v2.1-or-later License](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt), a copy of which is included in [CHECKSTYLE_LICENSE](https://github.com/david-zvi/OOPCodingStyleVerifier/blob/main/CHECKSTYLE_LICENSE). All other files in this repository are a copy of Checkstyle's license and files I created myself.
The source code for Checkstyle can be found in [their official repository](https://github.com/checkstyle/checkstyle).
No changes were made to Checkstyle's software.


### Issues and Contribution
If you run into any issues running this test, bugs causing incorrect results, or want to try and help configure the code style guidelines not currently covered, feel free to contact me at david-zvi.kadish@mail.huji.ac.il, open an issue, or fork the repo and open a pull request with proposed changes.
