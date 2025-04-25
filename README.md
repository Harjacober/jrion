# Orion
### Introduction
* Overview of the Language

### Getting Started
* Installation Guide
  * System Requirements
  * Downloading and Installing Orion
    * Building from Source
  * Setting Up the Development Environment
* First Program: "Hello, World!"
* Running Your First Program

### Language Syntax
* Basic Syntax
* Variables and Data Types
* Operators
* Comments
* Control Structures (if, while, for, etc.)
* Blocks and Scopes
* Functions
  * Defining Functions
  * Parameters and Return Values
  * Scope and Lifetime
  * Native Functions

### Core Concepts
* Data Structures
  * Arrays
  * Maps
* Functional Programming
  * Higher-Order Functions
  * Lambdas and Closures
* Object-Oriented Programming
  * Classes and Objects
  * Inheritance

### Contributing
* How to Contribute
* Possible Areas for Contribution

## Introduction
**Orion** is a dynamically typed scripting language, based on the Crafting Interpreters book by Robert Nystrom. It implements everything from the book, plus a few extra features built on top.

This project, called **Jrion**, is a Java implementation of **Orion**. I’ve followed the book’s language philosophy and overall structure pretty closely, but there are some differences in the way things are implemented and in a few design decisions. For example, while the book makes all class members public, Orion introduces access modifiers. Instead of using an `init` method for class constructors (as in the book), Orion uses the class name itself as the initializer. Also, whereas the book bakes print statements directly into the grammar, Orion provides native `print` and `println` functions instead.

On top of that, **Orion** adds some useful features like built-in arrays and maps.
You can find more details below.

## Getting Started
### Installation Guide
**System Requirements**
* Orion is designed to run on modern operating systems, including Windows, macOS, and Linux.
* Ensure you have a compatible version of the Orion interpreter installed.
* **Optional:** A VScode editor with the Orion plugin (Not available yet...) to support syntax highlighting when writing code.

**Downloading and Installing Orion**

**Mac**
* Download the [orion-mac-aarch64.pkg](https://drive.google.com/file/d/17urSd71dQL9VmfjS-UP3gFup3z_YqCx4/view?usp=drive_link) or [orion-mac-x64.pkg](https://drive.google.com/file/d/1yfGgSPF8Z0ywIe0w8eC3pub8FH7_RXy1/view?usp=drive_link) file.
* Open the package file you downloaded and follow the installation prompts.
* Orion is installed to `/usr/local/orion`. The package should also create a symlink in `/usr/local/bin/orion`.
* Restart your Terminal sessions and verify that Orion is installed by typing the following command:

```declarative
$ orion --version
```
* Command line should print the version of Orion installed.

**Linux**

* Download the [orion-x64.tar.gz](https://drive.google.com/file/d/1AOPrDl3yFzkuIRkZvxhGVheth6b8Z5B7/view?usp=drive_link) file for x64-bit Linux here, or [orion-aarch64.tar.gz](https://drive.google.com/file/d/1oxlW9omJ2mNnmxnjqqHrOKzUE8lHXCOb/view?usp=drive_link) for arm64-bit Linux.
* Remove any previous versions of Orion installed on your system by deleting the `/usr/local/orion` directory or whatever directory `Orion` was previously installed.
* Extract the tarball using the following command:
```declarative
$ tar -xvzf orion.tar.gz
```
* Move the extracted files to a directory of your choice, e.g., `/usr/local/orion`.
* Add the directory to your PATH environment variable by adding the following line to your `~/.bashrc` or `~/.bash_profile` file:
```declarative
export PATH=$PATH:/usr/local/orion/bin
```
* Restart your Terminal sessions and verify that Orion is installed by typing the following command:
```declarative
$ orion --version
```
* Command line should print the version of Orion installed.

**Windows**
* Download the [orion-win.zip](https://drive.google.com/file/d/1H1Pr00OV09u3wVeULN4X6MVu8gP_R1NT/view?usp=drive_link) file for Windows.
* Unzip the downloaded file to a directory of your choice, e.g., `C:\Program Files\Orion`.
* Add the bin directory to your PATH environment variable by following these steps:
  * Right-click on "This PC" or "My Computer" and select "Properties."
  * Click on "Advanced system settings."
  * Click on the "Environment Variables" button.
  * In the "System variables" section, find the `Path` variable and click "Edit."
  * Add the path to the bin folder (e.g., `C:\Progran Files\Orion\bin`) to the list of paths.
  * Click "OK" to save the changes.
  * Restart your Command Prompt or PowerShell sessions and verify that Orion is installed by typing the following command:
```declarative
$ orion --version
```
* Command line should print the version of Orion installed.

**Building from Source**
* Ensure you have the following dependencies installed:
  * JDK 17 or higher
* Clone the Orion repository from GitHub:
```declarative
git clone https://github.com/Harjacober/jrion.git
```
* Open the project in your favorite IDE (e.g., IntelliJ IDEA, Eclipse).
* Run the `com.kingjoe.orion.jrion.RionClass`

**Setting Up the Development Environment**
* Install Visual studio Code
* Install the Orion plugin for Visual Studio Code from the marketplace. (Note: The plugin is not available yet)
* Open Visual Studio Code and create a new file with the `.orion` extension.
* Start writing your Orion code in the new file.
* You can use the Orion interpreter directly from the command line to run your programs.
```declarative
$ orion {filename}
```

### First Program: "Hello, World!"
To get started, here’s the simplest program you can write in Orion: printing "Hello, World!" to the console.

**Example:**
```declarative
println("Hello, World!");
```
**The Program outputs:**
```declarative
Hello, World!
```
### Running Your First Program
Once you've installed the Orion interpreter, you can run your first program by firing up the interpreter interactively or
saving your source code to a .orion file and executing it from the command line:

To run the program interactively:
```declarative
$ orion
[2025-04-21 12:45:44] >> println("Hello, World!");
Hello, World!
```
To run the program from a file:
```declarative
$ echo 'println("Hello, World!");' > hello.orion
$ orion hello.orion
Hello, World!
```

## Language Syntax
### Basic Syntax
In Orion, programs are written with a mix of expressions and statements. Each statement typically ends with a semicolon (;).
**Example:**
```declarative
var message = "Hello, Orion!";
println(message);  // This prints the message to the console
```
### Variables and Data Types
Orion supports several data types, including: strings, numbers, booleans. Variables are declared using the `var` keyword.
**Example:**
```declarative
var number = 10;       // Integer
var greeting = "Hello"; // String
var isActive = true;    // Boolean
```
### Operators
Orion supports common operators like arithmetic, comparison, and logical operators.
**Example:**
```declarative
var a = 5;
var b = 10;
var sum = a + b; // Arithmetic operator
var isEqual = (a == b); // Comparison operator
var isTrue = (a < b and b > 0); // Logical operator
```
**Arithmetic Operators:**

`+, -, *, /, %`

**Logical Operators:**

`and, or`
Logical operator not only works with boolean values but also with other types. For example, `and` operator will return the first falsy value or the last truthy value.
`nil` is considered falsy, while other values are considered truthy.
```declarative
"bread" and "jam" // returns "jam"
"bread" or "jam" // returns "bread"
 nil and "jam" // returns nil
```

### Comments
Orion supports single-line and multi-line comments.
**Single-line comment:**
```declarative
// This is a single-line comment
```
**Multi-line comment:**
```declarative
/* This is a
   multi-line comment */
```

### Control Structures
**If-Else**
Conditionally execute blocks of code using `if`, `elseif`, and `else`.
**Example:**
```declarative
var n = 4;
if (n == 4) {
    println("even: " + n);
} elseif n == 1 {
    println("odd: " + n);
} else {
    println("invalid");
}
```
**While Loop**
Repeat a block of code while a condition is true.
**Example:**
```declarative
var n = 1;
while (n <= 5) {
    if n % 2 == 0 {
        println("even: " + n);
    } else {
        println("odd: " + n);
    }
    n = n + 1;
}
```
**For Loop**
Iterate over a range of values or an array.
**Example:**
```declarative
var arr = [1, 2, 3, 4, 5];
for (int i = 0; i < arr.length; i++) {
    println(arr[i]);
}
```
For `while` loops and `if` statements, the parentheses that wrap the condition are optional. However, it is recommended to use them for better readability.
### Functions

Functions are blocks of code that can be reused. They can take parameters and return values. Functions are defined using the `fun` keyword.

**Defining Functions**

Functions are defined with the `fun` keyword. You can define them with parameters and return values.

**Example:**
```declarative 
fun add(a, b) {
    return a + b;
}
var result = add(5, 10);
println("Result: " + result); // Output: Result: 15
```
**Parameters and Return Values**

Functions can take multiple parameters and return a value. The return type is inferred from the return statement. the return statement is optional, and if omitted, the function will return `nil`.
**Example:**
```declarative
fun count(n) {
    while(n < 100) {
        if n == 5 {
            return n;
        }
        print(n + " ");
        n = n + 1;
    }
}

print("Return value = " + count(1)); // Output: Return value = 5
```

**Scope and Lifetime**

Variables defined inside a scope are local to that scope and cannot be accessed outside of it.
**Example:**
```declarative
var l = 2;
var b = 3;
var area = l * b;

{
  var r = 2;
  var pi = 3.142;
  var area = 2 * pi * r * r;
  println("Area of Circle = " + area);
}

println("Area of Rectangle = " + area);

// Output:
// Area of Circle = 25.136
// Area of Rectangle = 6
```

**Native Functions**
Orion provides several native functions for common tasks, such as `println`, `print`, and `time`. These functions can be used without any additional imports.
**Example:**
```declarative
println("Hello, Orion!"); // Prints to the console

print("This is a test."); // Prints without a newline

var before = time(); // Gets the current time in milliseconds
println("Time interval: " + (time() - before)); // Prints the time interval in milliseconds
```

### Core Concepts
#### Data Structures
Orion supports basic data structures like arrays and maps.
##### Arrays
Arrays are ordered collections of elements. They can hold values of different types.

**Example:** Declaring an array:
```declarative
var arr = [1, true, "word", ["nested"], {"key": "value"}]; // [1, true, "word", ["nested"], {"key": "value"}]
arr = [2] * 3; //[2, 2, 2]
arr = [nil] * 2; //[nil, nil]
```
**Example:** Array operations:
```declarative
var arr = [1, 2, 3];
println(arr[0]); // Output: 1

arr[0] = 10; // Sets the first element to 10
println(arr[0]); // Output: 10

println(arr.length); // Output: 3
```
##### Maps
Maps are collections of key-value pairs. Keys can be of any type, and values can also be of any type.

**Example:** Declaring a map:
```declarative
var map = {"a": 1, "b": 2, "c":3, "d": [true, 5], 4: false};

map["a"] = 11; // Sets the value of key "a" to 11
map["d"][0] = false; // Sets the first element of the array at key "d" to false
map["newKey"] = "newValue"; // Adds a new key-value pair to the map

println(map["a"]); // Output: 11
println(map["d"][0]); // Output: false
println(map["newKey"]); // Output: newValue
```

### Functional Programming
Functions are first-class citizens in Orion, meaning they can be assigned to variables, passed as arguments, and returned from other functions.

**Higher Order Functions, Lambdas and Closures**

You can define functions inline using lambdas and closures, capturing variables from the surrounding environment.

**Example:** Closures
```declarative
fun makeCounter() {
    var i = 0;
    fun count() {
        print(i);
        i = i + 1;
    }
    return count;
}

var counter = makeCounter();
counter();  // Prints: 0
counter();  // Prints: 1
```

**Example:** Variable scopes in closures
```declarative
var a = "global2";
fun show() {
    fun showA() {
        println(a);
    }
    showA();
    var a = "block2";
    showA();
    a; //using variable 'a' to prevent compile time error
}

show();

// Output:
// global2
// global2
```

**Example:** Higher-order functions
```declarative
fun applyFunction(func, value) {
    return func(value);
}

fun square(x) {
    return x * x;
}

var result = applyFunction(square, 5);
println("Result: " + result); // Output: Result: 25
```
**Example:** Using lambdas
```declarative
var squares = fun (k) {
    return k * k;
};

fun printSquares(n) {
    for (var i = 1; i <= n; i = i + 1) {
        print(squares(i) + " ");
    }
}

printSquares(5); // Output: 1 4 9 16 25
```

### Object-Oriented Programming

Orion cuurently supports  classes and inheritance. Polymorphism is not supported yet.

**Classes and Objects**

You can define classes using the `class` keyword, and create objects by calling the class constructor.

**Example:** Defining a class

```declarative
class Person {
    var name;
    var age;

    Person(name, age) {
        this.name = name;
        this.age = age;
    }

    greet() {
        println("Hello, my name is " + this.name + " and I am " + this.age + " years old.");
    }
}

var person = Person("Orion", 3);
person.greet(); // Output: Hello, my name is Orion and I am 3 years old.
```

**Access modifiers**

There's no reserved keywords for access modifiers. The default access modifier is `public`. To make a class member private, you will prefix the member with an underscore `_`.

**Example:** Access modifiers
```declarative
class A {
    var fieldIsPublic;
    var _fieldIsPrivate;

    methodIsPublic(){
        this.methodIsPrivate();
    }

    _methodIsPrivate() {
        println("I am private");
    }
}

var a = A();

println(a.fieldIsPublic); //nil
println(a.fieldIsPrivate); //runtime error

a.methodIsPublic(); //I am private
println(a.methodIsPrivate()); //runtime error
```

**Inheritance**
Orion supports single inheritance. You can create a subclass using the `<` operator.
**Example:** Inheritance
```declarative
class Animal {
    var species;

    Animal(species) {
        this.species = species;
    }

    makeSound() {
        println("Animal sound");
    }
}
class Dog < Animal {
    var breed;

    Dog(species, breed) {
        super(species);
        this.breed = breed;
    }

    makeSound() {
        println("I am a " + this.breed + " " + this.species);
    }
}
var dog = Dog("Dog", "Labrador");
dog.makeSound(); // Output: I am a Labrador Dog
```

### Contributing
**How to Contribute**

If you would like to contribute to the Orion project, please follow these steps:
1. Fork the repository on GitHub.
2. Create a new branch for your feature or bug fix.
3. Make your changes and commit them with clear messages.
4. Push your changes to your forked repository.
5. Create a pull request to the main repository, describing your changes and why they should be merged.
7. Once your pull request is approved, it will be merged into the main repository.
8. Make sure to keep your forked repository up to date with the main repository to avoid merge conflicts in the future.
12. If you are adding new features, please include tests to ensure the functionality works as expected.
13. If you are fixing bugs, please include a test case that demonstrates the issue and how it was resolved.

**Possible areas for contribution:**
* Provide standard library to support the following
  * File I/O
  * Accepting user input
* Add Support for Bitwise operators
* Add Error handling
* Add an entry point to the program
* Add support for packages to group related classes and functions
* Add support for imports to include code from other files