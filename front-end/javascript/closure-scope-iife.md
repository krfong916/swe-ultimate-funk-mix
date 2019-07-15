## Scope and closure

### What is scope?
Scope is the set of rules that determines how to locate a variable identifier and assign it a value

### Compiler theory
How is code compiled? There are a few phases
- Tokenizing: characters are broken into chunks - tokens
- Lexing: when the tokenizer invokes the stateful parser rules to figure out whether a chunk is a distinct token or  part of another token
- Parsing: we build an Abstract Syntax Tree (AST) from a stream of tokens. The AST represents a structure of a program. For instance: the top-level node called a program and the children nodes could be a variable declarations and function declarations. The variable declarations have children nodes that describe properties of the variable.
- code generation: the engine turns the AST into a set of machine instructions that allocate memory to construct a variable.
- KEY POINT: Javascript is compiled milliseconds before the code it is executed i.e. compiled at runtime

### How this relates to scope
Scope collects and maintains a lookup list of declared identifiers and enforces a strict set of rules of how these identifiers can be accessed within the currently executing code. <br/>
For example: How are values assigned for `var a = 2`?
1. The compiler tokenizes/lexes and parses the code. The code generator interprets the AST into assembly instructions to allocate memory.
2. The compiler declares a variable. When executing code, the engine looks up the variable in the scope collection and assigns the variable its assignment if found.
If the compiler cannot fund the RHS reference within the currently executing scope, it will look in outside scopes, going one level out until the outermost scope is reached.
*Note:* unfulfilled RHS references result in ReferenceError
Unfulfilled LHS references will result in implicit creation of global if not in strict mode

### Lexical Scope
Lexical scope is created at lex time from the code we author.
Shadowing - the same identifier is declared at multiple levels of scope. The more-inner identifier’s value shadows the outer. Once scope finds a match, it stops looking. i.e. object properties are resolved once an identifier has been matched in that scope

### Function and block scope
The thinking behind function scope is to only expose what is completely necessary, the principle of least privilege. This principle states that we should only expose what is minimally necessary, and hide all else, like an API. Another way to think about function scope is that a function declaration is much like wrapping around code, in effect, “hiding” it.
Other benefits include:
- collision avoidance
- global namespace pollution: modules, a solution to global namespace pollution

#### Term: Enclosing means outer! i.e. the outer scope
### Functions as scope
The key difference between a function declaration and function expression is that a declaration is bound to an identifier.
```javascript
function foo() {
  console.log('hello');
}

(function foo() {
  console.log('hello');
})();
```
In the first snippet, we have a function declaration. foo is bound to the enclosing scope, and we can call foo directly with foo().
in the second snippet, the name foo is not bound to the enclosing scope, instead, foo is bound inside of its own function. i.e. foo is found only in the scope of the IIFE. It does not pollute the enclosing scope.

### Anonymous v. named function expressions
An anonymous function expression is a function with no name identifier i.e. an anonymous IIFE
name your IIFEs, it makes it easier to perform stack traces, debug, and perform recursion
```javascript
var a = 2;
(function IIFE (def) {
  def(window);
})(function def (global) {
  var a = 3;
  console.log(a); // 3
  console.log(global.a) // 2
});
```
### Blocks as scopes
These can be enforced using a let variable, otherwise, they’re accessible outside of the enclosing scope
- for loops - if using var in the for loop declaration, we can access the variable i in the enclosing scope, but why pollute the enclosing scope when we can use let
- if statements
- try/catch blocks - catch implements block scope, meaning, we cannot access its variables in the enclosing scope
Block-scoping is also useful because it allows the garbage collector to reclaim memory
```javascript
function process(data) {
  // do something interesting
}

// anything delcared inside this block can go away after
{
  let someReallyBigData = {..};

  process ( someReallyBigData );
}
var btn = document.getElementById( 'my_button' );

btn.addEventListener('click', function click(event) {
  console.log('button clicked');
}, /*capturing phase*/ false);
```
### let and const
`let` is not hoisted and is mutable. Additionally, `let` attaches the variable declaration to the scope of whatever block, {…} curly brackets, it is contained in. Put another way, `let` attaches to arbitrary blocks rather than to the enclosing function’s scope. <br/>
&nbsp;&nbsp;&nbsp;&nbsp;`let` really shines through for for-loops, like when we want to bind i to the for loop body and re-bind for every iteration.
`const` is immutable and is not hoisted. i.e. it create a block-scoped variable

### Hoisting
Declarations are hoisted, assignments are left in place. Recall, the engine will compile your code before interpreting it. The compilation phase will find and associate declarations with their appropriate scope.
The declaration is processed during compilation phase and the assignment is left in place for execution. Metaphorically, this is known as hoisting. The variables and function declarations are moved from where they appear to the top of the code.
—> Important hoisting is per scope and anonymous function expressions are not hoisted, EVEN if its a named function expression. A function’s name does not get hoisted if it’s a part of the function expression. Functions are hoisted first, then variables.<br/>
Observe: declarations are hoisted, assignments and expressions are not (like named and anonymous expressions and IIFEs)
```javascript
foo();
function foo() {
  console.log(a);
  var a = 2;
}

// is actually

function foo() {
  var a;
  console.log(a);
  a = 2;
}
foo();
```
### Closures
Closure is when a function is able to remember and access its lexical scope (even when that function is executing outside its lexical scope), or put another way, a closure is when the inner function has access to its outer enclosing function's variables and properties.

Closure to maintain private variable
```javascript
function person (governmentName) {
  var chineseName = '邝'
  return function allNames() {
    console.log('your government name is ' + governmentName + ' and your chinese name is ' + chineseName);
  }
}
var thisPerson = person('Kyle')
thisPerson();

// or

function person (governmentName) {
  var chineseName = '邝'
  function allNames() {
    console.log('your government name is ' + governmentName + ' and your chinese name is ' + chineseName);
  }
  return allNames()
}
person('Kyle') // in global scope
```
Notice: we are able to remember and access private variables declared within the lexical scope of `allNames()` even when executing outside of its lexical scope. Also `allNames()` is the inner function that has access to its enclosing function's variables and properties
### Revealing module pattern
The revealing module pattern enables separation of concerns and improve readability. We can specify public and private members
**Revealing Module Pattern**
```javascript
var namespace = function() {
  // declaration of
  // variables,
  // public methods
  // private methods
  return {
    publicAPIMethod: someMethod,
    publicVariable: someVariable
  }
}
```
### Immediately-invoked function expressions - IIFEs
An IIFE isolates the scope because it allows us to define private properties and methods inside the IIFE w/out polluting the global space around it. Sometimes we need to define and call function at the same time and only once so an anonymous function could help us. <br/>
&nbsp;&nbsp;&nbsp;&nbsp;Functions w/names are only useful when you need to call them from different places in your code. Function w/good names are useful but when writing a library with a lot of people will use there's always a risk that a function will be overridden by some other script <br/>
&nbsp;&nbsp;&nbsp;&nbsp;IIFE allows us to immediately return what we return inside of it. We have closure over the internal properties and methods.
### Anonymous IIFE
Consider an anonymous IIFE as a singleton, perhaps code that only needs to run once.
```javascript
(function(obj) {
  // declaration of
  // variables,
  // methods
})();
```
Here are some of the use cases for an anonymous IIFE:
- any kind of loop that involves local variables and tasks (setTimeout)
- microtasks (Promises)
- async code (ajax or get request) where we want to access the local variables separately for each loop iteration in the async completion function

An IIFE creates its own lexical scope. Closure is when a function is able to remember and access its lexical scope (even when that function is executing outside its lexical scope), or put another way, a closure is when the inner function has access to its outer enclosing function's variables and properties.

Example A/B: The IIFE creates a lexical scope at each turn of the for-loop. `i` is declared within the for-loop and we are accessing the reference to  `i` from the anonymous function at each iteration i.e. we are closing over the local scope the IIFE creates during each of the for-loops' iterations. This is an example of closure <br/>
**A**
```javascript
for (var i = 0; i < max; i++) {
  (function(index) { // we have an implicit variable declaration and assignment
    setTimeout(function() {
      console.log(index);
    }, 0);
  })(i);
}

// let's explain the above snippet again with a more clear lexical scope and demonstration of closure:
for (var i = 0; i < max; i++) {
  (function() {
    var index = i // we have access to the value of i from the outside scope
    setTimeout(function() {
      console.log(index);
    }, 0);
  })(i);
}
```
**B**
```javascript
for (var i = 0, max = 6; i < max; i++) {
  (function(){
    setTimeout(function timer() {
      console.log(i);
    }, 0);
  })();
}
```
Example C: Here, we are not accessing the lexical scope the IIFE creates during each turn of the for-loop. It is true that we are creating more lexical scope because we invoke an IIFE during each iteration of the for-loop, but we are not closing over the value of i during each iteration. <br/>
```javascript
for (var i = 0, max = 6; i < max; i++) {
  setTimeout(function timer() {
    console.log(i);
  }, 0);
}
```
Example D: we are closing over the global scope, Each one of the function callbacks share the reference to the same `i`
```javascript
for (var j = 0, max = 6; j < max; j++) {
  // we have access to j because it is in our lexical scope
  (function() {
    var k = j;
    setTimeout(function() {
      console.log(k);
    }, 0);
  })();
}
```
More examples:
**Write a function that would allow us to do this**
```javascript
var addSix = createBase(6)
addSix(10) // returns 16
addSix(20) // returns 26
```
```javascript
function createBase(number) {
    return function(total) {
        return total += number;   
    }
}

// let's explain the above snippet again with a more clear understanding of closure and lexical scope
function createBase(number) {
  return function(total) {
    return total += number;
  }
}
// we create a closure to keep the value passed to the function createBase
```
**How would you use a closure to create a private counter?**
```javascript
function counterModule ( update ) {

  var _count = 0;

  function get() {
    return _count;
  }

  function set ( number ) {
    if (typeof number == 'number') {
      _count += number;
    } else {
      console.log('throw err msg');
    }
  }

  function increment () {
    _count++;
  }

  return {
    get: get,
    set: set,
    increment: increment;
  }
}

var counterModuleInstance = counterModule();
counterModuleInstance.get(); // 0
counterModuleInstance.set(4); // 4
counterModuleInstance.increment(); // 5
counterModuleInstance.get(); // 5
```

**How would you write a self-contained module using an IIFE**
We fully shield our variables and methods from the global scope, so our variables act as if they are private - the variables and methods are limited to within the module's closure, so that the only code able to access its scope are the module's functions. This is an example of a singleton
```javascript
var namespace = (function(global) {
  return {
    // declaration of
    // variables,
    // public methods
    // private methods

    // a public variable
    publicVar: 'publicVar',

    // a public variable
    publicMethodA: function () {}
  }
})(window);

namespace.publicMethodA();
```

### Import and export
ES6 makes __ first class so the module pattern is
import and export are also closures

### Sources:
https://coderbyte.com/algorithm/3-common-javascript-closure-questions
https://stackoverflow.com/questions/23124067/what-are-good-use-cases-for-javascript-self-executing-anonymous-functions
https://www.oreilly.com/library/view/learning-javascript-design/9781449334840/ch09s02.html#id744998
http://2ality.com/2013/11/immediately-invoked.html

### More good stuff
```javascript
var bankAccount = function(initialBalance){
  var balance = initialBalance
  return {
    getBalance: function() {
      return balance;
    },
    deposit: function(amount) {
      balance += amount
      return balance;
    },
    withdraw: function(amount) {
      if (amount <= balance) {
        balance -= amount;
        return true;
      } else {
        return false;
      }
    }
  }
}

var kylesAccount = bankAccount(1.43);
kylesAccount.deposit(100);
kylesAccount.withdraw(20);
kylesAccount.withdraw(19.26);
kylesAccount.getBalance();
```
