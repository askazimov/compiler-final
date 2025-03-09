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

### Test case 1: correct parsing
```
PROC test(Integer z, Integer x)
VARS
BEGIN
END

FUN square(Integer x) RETURNING Integer
VARS
BEGIN
    RETURN x * x;
END


FUN factorial(Integer y) RETURNING Integer
VARS
Integer counter[12];
Integer x;
BEGIN

	
	x := square();

	WHILE (x < 10) DO
		x := x + 1;
	ENDWHILE

	RETURN x;
END

PROC main()
VARS
Integer result;
Integer arraySize;
Integer f;
BEGIN
	
	arraySize := getInt();
	
	IF arraySize > 300 OR arraySize < 0 THEN
	ENDIF

	result := square(5);
	

	f := factorial(12);
END
```

### Test case 2: syntax error
```
FUN square() RETURNING Integer
VARS
Integer x;
BEGIN
    RETURN x * x;
END

PROC unbalanced()
VARS
BEGIN
    IF (5 > 3 THEN
        echo("Error");
    ENDIF
END


PROC main()
VARS
    Integer result;
BEGIN
    result := square(5);
END
```

### Test  case 3: local variable conflicts with global variable
```
FUN square() RETURNING Integer
VARS
Integer x;
BEGIN
    RETURN x * x;
END

PROC unbalanced()
VARS
BEGIN
    IF (5 > 3) THEN
        echo("Error");
    ENDIF
END


PROC main()
VARS
    Integer result;
    Integer x;
BEGIN
    result := square(5);
END
```

### Test case 4: return type mismatch
```
FUN randFunc() RETURNING Boolean
VARS
BEGIN
    RETURN TRUE;
END

PROC main()
VARS
    Integer result;
BEGIN
    result := randFunc();
END
```

### Test case 5: procedure used in function context
```
PROC test(Integer x)
VARS
BEGIN
    x := x * x;
END

PROC main()
VARS
    Integer result;
BEGIN
    result := test(5);
END
```

### Test case 6: usage of built-in functions
```
PROC main()
VARS
    Integer result;
    String input;
BEGIN
    result := getInt();
    input := getString();

    echo(input);
END
```

## Authors
Kazimov Aslan
Turaev Oybek
