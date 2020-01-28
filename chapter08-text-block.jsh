// To starts, run jshell --enable-preview which is a program able to interpret Java syntax
// then cut and paste the following lines to see how it works
// To exit jshell type /exit

// # Text Block

// ## Design forces
// - add multiline strings in Java (the Kotlin way)
// - try to be smart about accidental indentation

// ## Plain Old String
System.out.println(
   "  SELECT *\n" +
   "  FROM users\n" +
   "  WHERE login == Bob"
   );

// ## Text Block
// Use `"""` to start and end the String
System.out.println("""
     SELECT *
     FROM users
     WHERE login == Bob
   """
   );


// # Incidental white spaces

// ## Block of Text
// Acts as a box around the text
// ```
// |--------------------|
// |..SELECT *          |
// |..FROM users        |
// |..WHERE login == Bob|
// |--------------------| 
// ``` 

// ## Block of Text
// The end `"""` specifies the bottom left corner of the box
System.out.println("""
       SELECT *
       FROM users
       WHERE login == Bob
   """
   );

// ## Block of Text
// By default, spaces on the right are removed,
// so it's less pretty.
// ```
// |----------|
// |  SELECT *|
// |  FROM users|
// |  WHERE login == Bob|
// |--------------------| 
// ``` 

// ## Spaces on the right
// Use `\s` to keep the space on the right
System.out.println("""
     SELECT *           \s
     FROM users         \s
     WHERE login == Bob \s
   """
   );
   
// ## Spaces on the right (2)
// If we ask the length of each line
"""
  SELECT *           \s
  FROM users         \s
  WHERE login == Bob \s
"""
  .lines().mapToInt(String::length).forEach(System.out::println);


// # Escaping

// ## Not a raw String !
// Escaping works as usual (`\t`, `\n`, `\"`, etc.)
System.out.println("""
     SELECT *
     FROM users
     WHERE login == \"Bob\"
   """
   );

// > Internally , the compiler uses `String.translateEscapes()`

// ## Remove the `\n`
// You can ask to not have the implicit newlines with `\`
System.out.println("""
   SELECT * \
   FROM users \
   WHERE login == Bob \
   """
   );


// # Indentation

// ## `indent()`
// Allow to specify the number of white spaces
System.out.println("""
   SELECT *
   FROM users
   WHERE login == Bob
   """
     .indent(8)
   );

// ## `stripIndent()`
// Remove the indentation !
System.out.println("""
     SELECT *
     FROM users
     WHERE login == Bob\
   """
     .stripIndent()
   );


// # String interpolation ?

// ## Formatted
// String.formatted() allows interpolation
// ```java
// var login = "Bob";
// System.out.println("""
//     SELECT *
//     FROM users
//     WHERE login == %s
//   """
//     .formatted(login)
//   );
// ```

// ## Formatted
var login = "Bob";
var password = "****";
System.out.println("credential %s %s".formatted(login, password));

// ## Optimization ?
// Currently, `formatted()` is slow ! (as slow as `String.format()`)

// Should be optimized using the StringConcatFactory like `+`.

