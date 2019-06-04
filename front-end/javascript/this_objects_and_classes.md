# This, objects, and es6 classes

### Summary
In this document, we will cover the `this` keyword, object prototypes, uses of `.call()` and `.apply()`, lexical `this`, arrow functions, es6 classes, object behavior delegation, some class theory, and objects-linking-other-objects.

### Motivation
We must know the finer points of object creation, assignment of values, and behavior delegation in order to understand Javascript.

## This
#### Overview
The `this` keyword is a special mechanism in Javascript - it allows us to implicitly pass reference values to other objects.

It is a source of frustration for many developer's, but it's not as complicated as they make it out to be. In this section, we'll cover the `this` keyword, how it functions, why it's useful, and how it can help us write more expressive code.

#### What is this
`this` has nothing to do with where a function is declared, but everything to do with the manner in which the function is called.

When a function is invoked, an activation record (aka execution context) is created. This contains in4mation abt. where the fcn. is called from (call-stack)

`this` is a binding that's made when a function is invoked. What it references is determined entirely by the call-site where the function is called

#### Quick example:

```javascript
function identify() {
  return this.name.toUpperCase();
}

function speak() {
  var greeting = "Hello, I'm " + identify.call(this);
}

var me = {
  name: 'Kyle'
}

var you = {
  name: 'Reader'
}

identify.call(me);  // KYLE
identify.call(you); // READER
speak.call(me); // Hello, I'm KYLE
speak.call(you); // Hello, I'm READER
```

**Explanation:** The code snippet allows `identify()` and `speak()` to be re-used against multiple contexts (me & you) objects, rather than needing a separate version of the function for each object

Alternatively, we could have explicitly passed the context

    function identify ( context ) {
      return context.name.toUpperCase();
    }
    function speak ( context ) {
      var greeting = "Hello, I'm " + identify(context);
      console.log(greeting);
    }

    identify(you); // READER
    speak(me); // Hello, I'm KYLE

#### Explicitly passing context is messier
using the `this` mechanism is:
- much cleaner
- more elegant way of implicitly 'passing along' an object reference
- much cleaner API design


The `this` keyword is **NOT** to be confused with any reference to lexical scoping or bridging values via lexical scope.

Not understanding the `this` mechanism pushes the developer to use lexical scope, a comfort zone. Lexical scope should not be used in substitution for not understanding how `this` works.

For example:
**insert an example here**

### 4 ways to determine this value
There are 4 ways to determine the call-site of the `this` keyword. Finding the actual call-site from the call-stack is the only thing that matters for a `this` binding

#### Summary, determining this
1. Is the function called with `new` (a `new` binding)? If so, `this` is the newly crated object `var bar = new foo()`
2. Is the function called with `.apply()` or `.call()` (explicit binding). or hidden inside a `.bind()` hard binding? If so `this` is the explicitly specified object
3. Is the function a context (implicit binding) a.k.a an owning or containing object? If so, `this` is *that* context object
4. Otherwise, the `this` value is the default binding, either `undefined` in strict mode, or the global object

#### Default binding
Consider:

    function foo() {
      console.log(this.a)
    }

    var a = 2

    foo(); // access 'a', property on the global object

**Explanation:** when foo() is called, `this.a` resolves to our global variable `a`. The default binding for `this` applies to the function call, so `this` points at the global object

#### Implicit binding, implicity lost
This happens when the value of a reference is lost and reference falls back to its default binding

Consider:
```javascript
function foo() {
  console.log(this.a); // Default binding, oops, global!
}

var obj = {
  a: 2,
  foo: foo
}

var a = "Default binding, oops, global!"

var bar = obj.foo
bar(); <--- the call-site
```

**Explanation:** We expect bar() to return the value of `2`, however, it returns `"Default binding, oops global!"`. What's the reason for this?

This is an example of implicitly losing the `this` binding.
Even though `bar()` appears to be a reference to `obj.foo`, in fact, it's just another reference to `foo()` itself. Moreover, the call-site is what matters - and the call-site is `bar()`, thus the default binding applies. When an implicitly-bound function loses its binding, its binding falls back to the default binding of either the global object or `undefined`, depending on the strict mode.

#### Implicitly lost while running a callback

Consider:
```javascript
function foo () {
  console.log(this.a)
}

function doFoo(fn) {
  fn(); // <--- the call-site!
}

var obj = {
  a: 2,
  foo: foo
}

var a = "oops global" // property on the global object

doFoo( obj.foo ) // "oops global!"
```
**Explanation:** Javascript is ***pass by reference***. Here, we are implicitly passing by reference `obj.foo` to `fn` i.e. `var fn = obj.foo`
It is the same as the above example, we are making a plai un-decorated call and t/f fallback to the `a` property on the global object.

Put another way: parameter passing is just an implicit assignment, and since we're passing a function, it's an implicit reference assignment. So the end result is a call-site that looks up `a` in the scope of `doFoo()`, then the global scope, finding `a` to be declared as `"Oops global"`

#### Implicitly lost con't.

Consider:
```javascript
function foo() {
  console.log(this.a)
}

var obj = {
  a: 2,
  foo: foo
}

var a = "oops global"

setTimeout(obj.foo, 100)
```
Equivalent to
```javascript
function setTimeout(fn, delay) {
  // wait(), somehow for 100 seconds
  fn()
}
```
**Explanation:** We expect `obj.foo` to print `2` because `2` is within its **context**. However, the call-site of `obj.foo` is in `setTimeout()`. `foo()` is declared and later added as a reference to `obj`. We are making a plain function call to `foo()` because regardless if `foo()` is declared or added as a reference property, the function is ***not owned or contained by the `obj` object***. We fall-back to the default binding, which is global object or `undefined`.

#### Explicit binding
We can use `.call()` and `.apply()` to explicitly bind `this` to the invoked function.

`.call()` allows us to specify an object as the value of `this` for a function

Consider:
```javascript
function foo() {
  console.log(this.a);
}

var obj = {
  a: 2,
}

foo.call(obj)
```
**Explanation:** Invoking `foo()` w/explicit binding - `foo.call()` allows us to force foo's `this` to be `obj`

#### Hard Binding

Consider:
```javascript
function foo() {
  console.log(this.a);
}

var obj = {
  a: 2
}

var bar = function() {
  foo.call(obj);
}

bar(); // 2
setTimeout(bar, 100); // 2
// bar hard binds foo's this to obj
// so that it cannot be overridden
bar.call(window); // 2
```
Or:
```javascript
function foo(something) {
  console.log(this.a, something);
  return this.a + something;
}

var obj = {
  a:2
}

var bar = foo.bind(obj);
var b = bar(3);
console.log(b);
```
**Explanation:** `bind(...)` returns a new function that is hard-coded to call the original function with the `this` context set as we specified.

#### API Call "Contexts"
built-in helper functions, such as .`forEach()`, `.map()` etc. certainly use `.call()` and `.apply()` to explicitly assign the value of the `this` object keyword at each iteration

i.e.
```javascript
function foo(value) {
  console.log(value, this.id)
}

var obj = {
  id: 'awesome'
}

var arr = [1,3,5,2,4]

// uses 'obj' as 'this' for foo(...) calls
arr.forEach(foo, obj);
```
#### Constructors

In typical class-oriented languages, constructors are special methods attached to classes. When a class is instantiated with the `new` operator, the constructor of that class is called. In JavaScript, the `new` keyword has **no** connection to class-oriented functionality.

Constructors in JavaScript are just functions with a `new` operator in front.
they are not attached to classes, nor are they instantiating a class
**they are just. regular. functions.**

There are no constructor calls in JavaScript, just constructor functions.

When a function is invoked with a `new` operator in front of it, the following happens:

- a brand new object is created
- the newly constructed object is `[[prototype]]` linked
- the newly constructed object is set as the `this` binding for that function call
- the `new`-invoked function call will automatically return the newly constructed object, unless specified otherwise

Consider:
```javascript
function foo(a) {
  this.a = a
}

var bar = new foo(a);
console.log(bar.a);
```
**Explanation:** by calling `foo()` with `new` in front of it, we've constructed a new object and set that new objcet as the `this` for the call of `foo()`. So `new` is the final way that a function call's `this` can be bound.

Consider:
```javascript
function foo() {
  console.log(this.a);
}

var a = 2;

var o = {
  a: 3,
  foo: foo
}

var p ={
  a: 4
}

o.foo(); // 3
(p.foo = o.foo)(); // 2
```
**Explanation:** The result value of the assignment expression `p.foo = o.foo` s a reference to the underlying object. As such, the call-site is just `foo()`, not `p.foo()` or `o.foo()`

#### Lexical binding and arrow functions
Lexical binding is the mechanism of relying on lexical scope to bind the values of the `this` keyword. The arrow function lexically captures w/e `this` is at call-time

Use cases: use in callbacks, such as event handlers and timeouts.
Otherwise, prefer to use `.bind()`.

Consider:
```javascript
function foo() {
  setTimeout(() => {
    // this here is lexically adopted from foo()
    console.log(this.a);
  }, 100);
}

var obj = {
  a: 2
}
```
#### Recap:
In order of most precedent when binding the value of `this`
1. Hard-Binding
2. new binding
3. implicit binding
4. default binding
Lexical binding and the arrow function adopt lexical scoping for `this` binding whatever it is form its enclosing function call. It's syntactically equivalent to doing `self = this`

#### Arrow functions
"Arrow functions are  bound to their parent's context" Nope, inaccurate.
Arrow functions do not have a `this`, which means any usage of `this` inside an arrow function is just like any other variable, and is looked up lexically through parent scopes until a `this` is found.

the bind(), call(), and apply() methods try to set a `this` on a function. but when used on an arrow function, which does not have a `this`, they effectively do nothing in that respect.

you can't observe the difference between "not having a `this`" and "binding to the `this` of some outer function". behaviors the same. but very different reason for it.

### ES6 classes
`super()` = static binding rather than late binding as `this`, even if it gives the impression that a function that is `super` bound is more a 'method' than 'function' - it's just semantics.

Consider:
```javascript
class Food {
    static isHealthy() {
        return true
    }
    static isEdible() { // references it's own class
        return this.isHealthy()
    }
}

console.log(Food.isEdible())

class Vehicle {
    isLegal() {
        return true
    }
}

class Car extends Vehicle {
    canUse() {
        return this.isLegal()
    }
}

console.log(new Car.canUse()); // returns true
console.log(Vehicle.prototype.isLegal()) // invoke property on prototype

class Person {
    constructor(id) {
        // shadowing of the id() method
        // with a property value on the instance
        this.id = id;
    }

    id () {

    }
}

const abigail = new Person('cc');
abigail.id(); // shadowing of the id() method with a property value on the instance we talked about above
```

### Overview
- the `class` keyword operates on the `[[prototype]]` (delegation) mechanism; therefore, the `class` keyword is just syntactic sugar over the function. All objects declared with a `class` keyword will have a prototype object.
- using `extends` - the prototype object, and not the class itself, is prototype linked to another class’s prototype object.

### Pseudo-Inheritance, still prototype-linked
In our example, `Car` is prototype linked to `Vehicle's` prototype using the `extends` keyword.
- `extends` gives the appearance of a classical parent-child relationship, however, instead of properties being copied from one class to another, methods and properties are *actually* created on the prototype object of that class.

To demonstrate, we can invoke `isLegal()` on the prototype object -> `console.log(Vehicle.prototype.isLegal()`.

### Classes, in further detail
To explain ES6 class behavior further, let's discuss why the code `console.log(new Car.canUse())` returns true. This is because the `this` keyword is referencing the newly created object we get from using the `new` keyword on our `Car` class. This newly created object is prototype linked to the `Vehicle's` prototype object through extends.

- We have access to the methods (otherwise known as functions or properties) that exist on another class. The method exists on the prototype object, and not on the class itself
Again, if looking to access a method (property) on the class, we must note that the method doesn’t exist on the class, the method is written to the prototype object of the class.

### Static keyword
To assign the method (property) to the class - we use the static keyword.
The static keyword will assign it to the class instead of the prototype of the class

Static properties can be called in its own class, and we can use the ’this’ keyword, because the static property is referencing it’s own class

### Shadowing
see above

#### Object-literal shorthand
Object literal shorthand is nice because it offers cleaner, terse syntax to your objects, but, you lose the naming of delegated properties.
i.e.
```javascript
let obj = {
  foo: function() { }
}

let obj = {
  foo() {}
}
```

#### Notes:
Contrary to belief: not everything in Javascript is an object. `function` is a sub-type of Object

Javascript uses the Prototype model for inheritance.

In most OO languages, like C++, Java and so on, there is a strict system of classes and instances; if you want a Bar that is just like a Foo but has a Hat property, you have to define a class Bar which inherits from Foo (or contains an instance of Foo), and then instantiate the class to create a new BarObject. By contrast in Javascript, all you have are instances, and to create a new type you just add stuff to an existing one.

**Parasitic inheritance**
Classical inheritance is about the is-a relationship, and parasitic inheritance is about the was-a-but-now's-a relationship.

Consider:
```javascript
function Vehicle() {
    this.engines = 1
}

Vehicle.prototype.ignition = function() {
    console.log("Turning on my engine")
}

Vehicle.prototype.drive = function() {
    this.ignition();
    console.log("steering and moving")
}

function Car() {
    // `car` is a `Vehicle`
    var car = new Vehicle()

    // we modify our `car` to specialize it
    car.wheels = 4

    // save a privileged reference to `Vehicle::drive()`
    var vehDrive = car.drive

    car.drive = function() {
        console.log(this) // <-- do you know where the call-site is?
        vehDrive.call(this)
        console.log("Rolling on " + this.wheels + " all good to go!")
    }

    return car;
}

var myCar = Car()

myCar.drive()
```
--> QUESTION: We certainly cannot say the call-site of the this in car.drive is *in the scope* of Car()... can we say the call-site is Car()? i.e. we give no mention of scope, we just say 'the call-site is Car()'

Since the two objects also share references to their common functions, that means that even manual copying of functions (aka, mixins) from one object to another doesn't actually emulate the real duplication from class to instance that occurs in class-oriented languages.

JavaScript functions can't really be duplicated (in a standard, reliable way), so what you end up with instead is a duplicated reference to the same shared function object (functions are objects; see Chapter 3). If you modified one of the shared function objects (like ignition()) by adding properties on top of it, for instance, both Vehicle and Car would be "affected" via the shared reference.

Explicit mixins are a fine mechanism in JavaScript. But they appear more powerful than they really are. Not much benefit is actually derived from copying a property from one object to another, as opposed to just defining the properties twice, once on each object. And that's especially true given the function-object reference nuance we just mentioned.

If you explicitly mix-in two or more objects into your target object, you can partially emulate the behavior of "multiple inheritance", but there's no direct way to handle collisions if the same method or property is being copied from more than one source. Some developers/libraries have come up with "late binding" techniques and other exotic work-arounds, but fundamentally these "tricks" are usually more effort (and lesser performance!) than the pay-off.

Take care only to use explicit mixins where it actually helps make more readable code, and avoid the pattern if you find it making code that's harder to trace, or if you find it creates unnecessary or unwieldy dependencies between objects.

#### Property v. method
functions never belong to objects. Methods do not exist on objects

although functions may have a `this` reference in them, `this` is dynamically bound at run-time, at the call-site and the relationship with the object is indirect.

Though function and method are interchangeable in Javascript, we are actually just doing property accesses

Ex:
```javascript
var obj = {
  foo:  function foo() {
    console.log("foo")
  }
 }

var someFoo = obj.foo
someFoo // function foo(){..}
```
**Explanation:** Declaring a function expression as part of an object literal does not make the function 'belong' to the object, it still is just a reference to the same function object

#### [[Get]] and [[Set]]

The `[[get]]` algorithm for an object does, perhaps a bit more work because it looks up the `[[prototype]]` chain for the property. If the `[[get]]` operation doesn't find the property - then it returns the value of undefined.
For variables - they are references to values - and so we see a `ReferenceError` thrown

In order to distinguish whether a property exists and holds the explicit value of `undefined` OR the property *does not* exist and `undefined` was the default return after `[[get]]` failed  --> we can use

```javascript
var myObject = {
  a:2
}
myObject.hasOwnProperty("a") // true
myObject.hasOwnProperty("b") // false
```
Setters [[set]]

get creates a property on the object, but doesn't hold the value. Instead, it makes a call to the getter function [[get]] and returns the result of the property access. setters override the default [[put]] (aka assignment), per-property.

```javascript
var myClass = {
  get a() {
    return 2;
  }

  set a(args) {
    console.log(this.a, args)
  }
}

myClass.a = 3

myClass.a // 2 3
```
When a property is accessed, [[get]] operation is invoked, and [[put]] for setting (assigning) values

we can customize an object using such as `writable(..)` `Object.seal(..)` and `Object.freeze(..)`
I will typically use `[[get]]` and `[[set]]` with classes because

# Classes and Prototypical Inheritance
classes are a way of implying a certain data structure. We can model relationships with classes

in Java - everything is a class.
in Javascript - has some class syntax but **Simply and CLEARLY** Javascript does not have classes. We fake classes. However, many developers are class-oriented. Here's the scoop...

#### Polymorphism
the idea of a child inheriting and modifying the behavior of the parent for greater specificity. The mechanism of polymorphism gives rise to the diamond problem, wherein, if a child shares two parents whose methods or properties are named the same - the question is, which parent will the child inherit from?

We lose the elegance of polymorphism, implicit inheritance, if we had to explicitly state the parent that the child must inherit from.

When classes are inherited --> there is a way for the classes themselves  to relatively reference the class inherited from (*and not the object instances
created from them!!*), this relative reference is usually called `super`

---> Javascript **does not automatically** create copies (as classes imply) between objects. Objects and functions only have shared references duplicated, not the objects/functions themselves. Not paying attention is a source of a variety of gotchas

explicit pseudo-polymorphism `OtherObj.methodName.call(this, ...)` is brittle and ugly syntax.
This comes up often when shadowing a method - i.e. Vehicle Object has a method called drive() and Car has a method called drive and overrides it - see parasitic inheritance (don't do it, not worth it)

#### Class-oriented languages
- Class mechanics
  - define a class is like defining a blueprint. We create the base for other classes to build on. In classical inheritance, Objects are copies of classes. In order to use a class, we must instantiate the class into an object by copy. Again, in classical inheritance. think java
  - An object is a copy of a class, in classical inheritance
- Constructors
  - Classes have a constructor. When we invoke a class, the constructor creates the class for us - it initializes any information of the instance that we will need
- Class Inheritance implies copies
- Multiple inheritance and the diamond problem

#### [[Prototype]] and Prototypical Inheritance
In prototypical inheritance, the `[[prototype]]` simply means reference to another object.
The `[[get]]` operation follows the `[[prototype]]` **link** of the object if it can't find the property on the object directly

The mental model that many developers have is copy and fill. When an object is created, properties from other 'classes' fill ::NEEDS ATTENTION REVISE


Here's what prototypical inheritance *is not*, and what developer's get confused on:
- implied classes
  - A function that implies a class is declared with an upper-case letter, even though this has *no significance* to the engine at all
  - the `new` keyword implies a construction of the object. Ex:
    ```javascript
    function Foo() {
      // ...
    }

    Foo.prototype.constructor === Foo; // true

    var a = new Foo()
    a.constructor === Foo // true
    ```
  - The `.constructor` property seems to be on `a`, however, the `.constructor` reference is *delegated* to `Foo.prototype`, which **happens to** have a `.constructor` pointing to `Foo`.
  - By default, The `Foo.prototype` object has a public property called `.constructor`. `.constructor` is a reference back to the function.
  - When `new` is called ---> we'd assume `a` is also given a property called `.constructor`, of which points to `Foo`, the function that created it. However, *this is not true*. `a` has no `.constructor` property on it. Also, we cannot say `Foo` constructed `a`
  - It's tempting to think `Foo` is a constructor because we call `new` and we observe that it constructed an object. However, `Foo` is just a function.
  Using `new` with `Foo`, the phrase constructs an object which we assign to `a` (which can be thought of as a side-effect)
  **---> The call was a constructor call** but `Foo` itself is just a function. with a `new` keyword in front.

  - Another example:
    ```javascript
    function Foo() { ... }

    Foo.prototype = { ... }; // create a new prototype object

    var a1 = new Foo();
    a1.constructor === Foo; // false!
    a1.constructor === Object; // true!
    ```
- What's going on here? Why is `a1.constructor === Foo` false? It would seem `Foo` constructed `a1`
- `a1` has no `.constructor` property, so it delegates up the `[[prototype]]` chain to `Foo.prototype`. `Foo.prototype` doesn't have the `.constructor` property either, like the default `Foo.prototype` would have had, we keep delegating up until `Object.prototype`. *That* object has a `.constructor` property on it, which points to the built-in `Object(..)` function.

- Say it with me, in Javascript, ***constructor does not mean constructed by***. A constructor on an object points at a function who, reciprocally, has a reference back to the object -- a reference which it calls `.prototype`.


#### Delegation
Objects (aka instances)
delegation links rather than copy operations

  --> Function calls are "constructor calls" iff new is used
- assignment of `this`
  - In class-oriented languages, a constructor will assign any properties and values the object will need, similar to how class instances encapsulate data values
  - Ex:
  ```javascript
    function Person(name) {
      this.name = name
    }

    Foo.prototype.myName = function() {
      return this.name
    }

    var a = new Person('Kyle')
    var b = new Person('Don')

    a.myName()
    b.myName()
    ```
- many would think Person copies over the functions and properties to `a` and `b`, however they are `[[prototype]]` linked. `a` and `b` end up with an internal `[[prototype]]` linkage to `Foo.prototype`. `myName` will not be found on `a` or `b`, it'll be found on `Foo.prototype` through delegation

The correct model of Prototypical inheritance is object's linking and behavior delegation.

`[[prototype]]` is about linking objects. Let's explain why it's useful

let's explain what it's not


JavaScript is one of the few languages where you can create an object on its own. Classes can't (being that they don't exist!) describe what an object can do. The object defines its own behavior directly. ***There's just the object***

`Bar.prototype = Object.create( Foo.prototype )` - make a `new` 'Bar dot prototype' objects that's linked to 'Foo dot prototype'

`Bar.prototype = Foo.prototype` doesn't work the way you'd expect it. Bar's prototype is assigned to `Foo.prototype` - when we start assigning properties `Bar.prototype.myLabel` - we are adding the property on the shared `Foo.prototype` and not on a separate object.

`Bar.prototype = new Foo()` - `new Foo()` creates a new object that is linked to `Foo.prototype`, but uses a `Foo()` function call.
- This is seen as a side-effect because if `Foo` has any side-effects, logging, changing state, adding data properties to `this`, those side-effects happen at the time of this linking

- The safest option is to use `Object.create()` to make a new object linked and with no side-effects, or Object.setPrototypeOf()

#### Inspecting Class Relationships
If we want to inspect what an object might delegate to we can use `Object-A instanceof Object-B`. In class-oriented languages, inspecting an instance (just an object in Js) for its inheritance ancestry (or more aptly described as the delegation linkage in Javascript)is called introspection (or reflection in JS)
- `instanceof` - in the entire prototype chain of an `Object-A`, does the prototype of `Object-B` appear?
The much cleaner approach for reflection is `Foo.prototype.isPrototypeOf(a)`

#### Object Links
Creating links amongst objects is the objective of prototype inheritance. The `[[prototype]]` linkage tells the engine to look for the property/method on the linked-to-object - objects linked together moves towards a delegation-oriented language

Although `[[prototype]]` should not be used as a fallback if an object cannot handle a method or property, (Instead, use the ES6 `Proxy`), you can design your API with `[[prototype]]` and reap the benefits of `[[prototype]]` linkage!
```javascript
var anotherObject = {
  cool: function() {
    console.log("I'm cool!")
  }
}

var myObject = Object.create(anotherObject)

myObject.doCool = function() {
  this.cool() // internal delegation
}

myObject.doCool() // "I'm cool!"
```
Internally, this takes advantage of `[[prototype]]` delegation and follows the delegation design pattern

#### Recap (taken from YDKJS Ch.5 this and obj prototype)
'The most common way to get two objects linked to each other is using the new keyword with a function call, which among its four steps (see Chapter 2), it creates a new object linked to another object.

The "another object" that the new object is linked to happens to be the object referenced by the arbitrarily named .prototype property of the function called with new. Functions called with new are often called "constructors", despite the fact that they are not actually instantiating a class as constructors do in traditional class-oriented languages.

While these JavaScript mechanisms can seem to resemble "class instantiation" and "class inheritance" from traditional class-oriented languages, the key distinction is that in JavaScript, no copies are made. Rather, objects end up linked to each other via an internal [[Prototype]] chain.

For a variety of reasons, not the least of which is terminology precedent, "inheritance" (and "prototypal inheritance") and all the other OO terms just do not make sense when considering how JavaScript actually works (not just applied to our forced mental models).

Instead, "delegation" is a more appropriate term, because these relationships are not copies but delegation links.' -Author of YDKJS, Kyle Simpson

#### Delegation-Oriented Design

We should strive for behavior delegation because it better reflects the kind of object relationship that javascript models, instead of class-oriented inheritance.

Class Theory
we'd create a base class. XYZ and ABC would inherit the base class and add properties and methods for their use cases and greater specificity. They might also modify the methods and properties from the base class, this is known as polymorphism.
ABC and XYZ would be copies of the base class.

Behavior Delegation Theory,
the base *object* would define its own methods. ABC and XYZ objects would define their own properties and methods. If ABC or XYZ needed to use from the base object, they would delegate to it.
The prototype mechanism links objects to other objects, there are no classes no matter how much we try to convince ourselves.
In behavior delegation, we avoid if at all possible naming things the same at different levels of the `[[prototype]]` chain (that would be shadowing if we did name things the same...). It creates brittle code and naming collisions.
The names of methods are strewn out, and can be easier to understand/maintain.

```javascript
function Graph(size) {
  this.size = size
}

Graph.prototype.getSize = function() {
  return this.size
}

function SpecialGraph(spaceAvailable) {
  Graph.call(this, spaceAvailable)
}

var SpecialGraph = Object.create(Graph.prototype)

SpecialGraph.prototype.spaceAvailable = function() {
  console.log("there is " + this.getSize() + " space available in the graph")
}

var graph1 = new SpecialGraph(60)
var graph2 = new SpecialGraph(30)

graph1.spaceAvailable()
graph2.spaceAvailable()

var Graph = {
  init: function(size) {
    this.size = size
  },

  getSize: function() {
    return this.size
  }
}

var SpecialGraph = Object.create(Graph)

SpecialGraph.spaceAvailable = function() {
  console.log("there is " + this.getSize() + " space available in the graph")
}

var graph1 = Object.create(SpecialGraph)
graph1.init(60)

var graph2 = Object.create(SpecialGraph)
graph2.init(30)

graph2.spaceAvailable()
```



#### Examples

**1**
```javascript
// This code is trying to use lexical scope to bind a:1 and b:3

a = 4;
b = 6;
function outer(){
  function inner(){
  console.log(`this.a is ${this.a} and this.b is ${this.b}`);
  }
  inner();
}
outer.bind({a:1,b:3})()

Revised:

a = 4;
b = 6;
function outer(){
  inner = () => {
  console.log(`this.a is ${this.a} and this.b is ${this.b}`);
  }
  inner();
}
outer.bind({a:1,b:3})()
```
Explanation: Remember, to understand this binding, we have to understand the call-site: the location in code where a function is called (not where it's declared). We must inspect the call-site to answer the question: what's this this a reference to?

Object-oriented programming, classical inheritance, prototypical inheritance, delegation, constructors, classes, methods/properties, getters and setters, composition over inheritance, polymorphism, prototype chain, ES6 classes and their prototypical equivalent

Object.assign()
spread operator
this in function calls
constructor calls
method calls
lexical scope

For an alternative: see https://github.com/getify/You-Dont-Know-JS/blob/f0d591b6502c080b92e18fc470432af8144db610/this%20%26%20object%20prototypes/ch6.md#nicer-syntax
