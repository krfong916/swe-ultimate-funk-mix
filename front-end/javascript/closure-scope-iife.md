IIFE

revealing module pattern enables separation of concerns and improve readability. We can specify public and private members
IIFE isolate the scope because it allows us to define private properties and methods inside the IIFE w/out polluting the global space around it

sometimes we need to define and call function at the same time and only once so an anonymous function could help us.

functions w/names are only useful when you need to call them from different places in your code

function w/good names are useful but when writing a library with a lot of people will use there's always a risk that a function will be overridden by some other script

IIFE allows us to immediately return what we return inside of it

closure over properties and methods


Immediately invoked constructors and object literals
http://2ality.com/2013/11/immediately-invoked.html




### Anonymous IIFE
consider an anonymous IIFE as a singleton, perhaps code that only needs to run once.

```javascript
(function(obj) {
  // declaration of
  // variables,
  // methods
})();
```
A use case for an anonymous IIFE:
- any kind of loop that involves local variables and tasks (setTimeout), microtasks (Promises), or async code (ajax or get request) where we want to access the local variables separately for each loop iteration in the async completion function

Examples:
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

**C**
```javascript
for (var i = 0, max = 6; i < max; i++) {
  (function(){
    setTimeout(function timer() {
      console.log(i);
    }, 0);
  })();
}
```

**D**
```javascript
for (var i = 0, max = 6; i < max; i++) {
  setTimeout(function timer() {
    console.log(i);
  }, 0);
}
```

**Explanation**
An IIFE creates its own lexical scope. Closure is when a function is able to remember and access its lexical scope (even when that function is executing outside its lexical scope), or put another way, a closure is when the inner function has access to its outer enclosing function's variables and properties.

- example A/B: The IIFE creates a lexical scope at each turn of the for-loop. `i` is declared within the for-loop and we are accessing the reference to  `i` from the anonymous function at each iteration i.e. we are closing over the local scope the IIFE creates during each of the for-loops' iterations. This is an example of closure.
- example C: Here, we are not accessing the lexical scope the IIFE creates during each turn of the for-loop. It is true that we are creating more lexical scope because we invoke an IIFE during each iteration of the for-loop, but we are not closing over the value of i during each iteration.
- example D: we are closing over the global scope, Each one of the function callbacks share the reference to the same `i`

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

### Self-contained module using an IIFE
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

### Export, Import
ES6 makes __ first class so the module pattern is

import and export are also closures

sources:
https://coderbyte.com/algorithm/3-common-javascript-closure-questions
https://stackoverflow.com/questions/23124067/what-are-good-use-cases-for-javascript-self-executing-anonymous-functions
https://www.oreilly.com/library/view/learning-javascript-design/9781449334840/ch09s02.html#id744998
