# Final version of the project for Compiler Construction CYBERUS
This is the final JJT file and visitor file we've developed during the Compiler Construction course. This README covers small introduction to the grammar, instructions for compiling it on your own device, some test cases and authors.

## Introduction to the language
The language we've came up with is strongly typed, supporting integer, float, boolean and string types. Each program file has entrypoint procedure named MAIN. Language also supports global variables (defined as local variables for main procedure), functions, procedures, boolean & arithmatic expressions, function calls and arrays. The parser can support type mismatch, conflicts between local and global variables and so on.

## Compiling instructions
1. Run jjtree to automatically generate relevant AST files and .jj file: `jjtree MyParser.jjt`
2. Run javacc to generate the rest of the missing files: `javacc MyParser.jj`
3. Compile the project: `javac -d . *.java`. This command compiles the project under `myparser` directory, as that's the name of the package.

To run the program, add the program's files to your CLASSPATH and run `java myparser.MyParser < example.txt`. Alternatively, you can run the following command from within the same folder as the `myprogram` folder: `java -cp . myparser.MyParser < example.txt`

## Test cases

...

## Authors
Kazimov Aslan
Turaev Oybek
