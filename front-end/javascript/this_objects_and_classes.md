# This, objects, and es6 classes

### Summary
In this document, we will cover the `this` keyword, object prototypes, uses of `.call()` and `.apply()`, lexical `this`, arrow functions, es6 classes, object behavior delegation, some class theory, and objects-linking-other-objects pattern as provided by Javascript Expert Kyle Simpson. In order for us to better understand Javascript, we must know the finer points of how object creation, assignment of values, and behavior delegation works in this language.

### This
The `this` keyword is a special mechanism in Javascript - it allows us to implicitly pass reference values to other objects.
It is a source of frustration for many developer's, but it's not as complicated as they make it out to be. In this section, we'll cover the `this` keyword, how it functions, why it's useful, and how it can help us write more expressive code. <br>
&nbsp;&nbsp;&nbsp;&nbsp; `this` **has nothing to do with *where* a function is declared, but everything to do with *how* a function is called.** When a function is invoked, an execution context (also activation record) is created. The execution context contains information about where the function is called from, this is known as the call-stack. To summarize, `this` is a binding that's made when a function is invoked. What it references is determined entirely by the call-site of where the function is called

### TL;DR
### This is in the global context
In the global execution context, the `this` keyword refers to the global object.
- In the browser, it is the global window object, if not using strict mode. If strict mode is enabled, `this` is undefined
- In node.js, `this` refers to module.exports because the node engine runs each module inside a wrapper function and the wrapper function is invoked witth the `this` value set to module.exports

### This in function calls
When a function is invoked, the `this` value is binded to the function. The value of `this` is determined by the function's call-site.

### This in constructor calls - the `new` keyword
One way to avoid polluting the global object with identifiers is to use the `new` keyword to create an instance of a constructor. This will prevent properties being added to the global object
One may think the constructor call makes a *copy* of the prototype in the instance; that is not the case - Javascript doesn't do *any* copying.
**A constructor call makes an object *linked to* its own prototype**
The mental model should be linking, not copying. Let me explain:

An object with a `new` operator in front of it does the following
1. create a brand new object
2. links that object to another object
3. calls the function with its `this` reference pointed at the newly created object
4. if the function does not return an object, assume return of `this`
<br/>
For example:

```javascript
function person(firstName, lastName) {
  this.firstName = firstName;
  this.lastName = lastName;
  // implicitly return this;
}
var somePerson = new Person('zendaya', 'cozz');
```

Note: `this` is implicitly returned in a constructor

### This in method calls
When a function is called as a method of an object, the `this` value is set to the object of the method. <br/>
For instance:
```javascript
const Person = {
  firstName: 'Kyle',
  sayHi() {
    console.log(`Hi, my name is ${this.firstName}`)
  }
}
```
Confusion occurs when the `this` value is lost i.e. when a method loses its receiver <br/>
For example:
```javascript
const Person = {
  first: 'Kyle',
  sayHi: function() {
    console.log(`${this.first}`)
  }
}

const greet = Person.sayHi();
greet() // greet is not a function
```
Explanation: <br/>
&nbsp;&nbsp;&nbsp;&nbsp; We are calling `greet()` as a regular, plain function call. `first` is `undefined` because `this` refers to the global object. In order to observe the behavior we expect from `greet()`, we must explicitly `this` to a specific object. <br/>
Observe:
```javascript
const Person = {
  first: 'Kyle',
  sayHi: function() {
    console.log(`${this.first}`)
  }
}
Person.sayHi.apply(Person);
```
Another confusion developers experience of `this` is when the receiver is lost when we pass a method as a callback to another function. <br/>
To demonstrate: `setTimeout(Person.sayHi, 1000)`. In order to solve this problem and observe the expected behavior, we can use the `bind()` method: `setTimeout(Person.sayHi.bind(Person), 1000) will log first`

### Specify this using call(), apply()
What's the use of `call()` and `apply()`? 
`call()` and `apply()` are used to explicitly invoke a function a specified context.
- as their first argument, they take a `this` keyword
```javascript
function ask(question) {
  console.log(this.teacher,question)
}

var workshop1 = {
  teacher: 'Ms. Grace',
}

var workshop2 = {
  teacher: 'Mr. Grottle'
}

ask.call(workshop1, "Can I explicitly set context?")
ask.call(workshop2, "Can I explicitly set context?")
```
In the example, we are invoking the `ask()` function with the context of `workshop1`. We say "wherever this function comes from, invoke it in a particular context which I'm going to specify"
- `apply()`: allows us to invoke a function with arguments
- `call()`: is for an array, it requires parameters to be lsited explicitly

### Hard-binding a function's this value using bind()
`bind()` hard binds the object's `this` to a specific target object, permanently. Meaning, `bind()` produces a new function that is bound to a particular specific `this` context.
For example:<br/>
- when we try to pass a method as a callback to another function, we often lose the receiver of the method. In the case of setTimeout utility function defined by HTML, it explicitly calls your function in the context of the global object context.
Suppose:<br/>
```javascript
const Person = {
  first: 'Kyle',
  sayHi: function() {
    console.log(`${this.first}`)
  }
}
setTimeout(Person.sayHi, 1000)
```
setTimeout calls the callback `Person.sayHi` and sets the context to the global object (imagine the context of script 1000ms later). This will result in the intended `this` context to be lost.
- Explicitly hard-binding the `this` value to the `Person` object, results in binding the `this` to a target object. `setTimeout(Person.sayHi.bind(Person), 1000)`
```javascript
var greet = Person.sayHi.bind(Person);
greet();
```
Even if we used `call()` or `apply()`, the object's `this` reference to the target object is maintained from the hard-binding of `bind()`. <br/><br/>
Notice that we use `bind()` for predictable `this` behavior, rather than flexible `this` behavior.

### Bind in depth
```javascript
/**
 * @params {thisArg} - the args we want to bind to
 * @params {fixedArgs} - the fixed number of arguments when we bind the function
 * note: the snippet - return func.apply(thisArg, [...fixedArgs, ..args])
 * when the new function is invoked, the argument lists are combined. We are using partial application
 * here. We provide all fixedArgs and concatenate all dynamic args
 */
Function.prototype.bind = function(thisArgs, ...fixedArgs) {
  const func = this; // we store a reference to the original function in the function variable
  // spreading (...args) takes care of any arguments that the caller of our inner function might provide
  return function(...args) { // we return a new function
    return func.apply(thisArg, [...fixedArgs, ...args]); // we use apply() to invoke our original function
  }
}
```
Bind returns a new function that we can use to invoke our original function with any of the arguments we want to bind to. We can use `bind()` like so:
```javascript
const functionReference = object.someMethod.bind(object); // pass as many args as we want
functionReference(); // bind returns a function, we can invoke that function
```
### Flexible or predictable? When to use this-aware style code
A heuristic for using `this` aware code is if our method calls depend on the dynamicism of `this`, and every once in a while, we are using `bind()`. However, if we find ourselves using `bind()` often, we are losing the dynamicism of `this` and would be better served using lexical closure (predictability).

### Arrow functions
The arrow function does not have a `this` so it resolves is lexically.
- An arrow function does not define a `this` keyword at all. If we put a `this` keyword inside an arrow function, it will behave like any other function - which mean it will lexically resolve to some enclosing scope that does define the `this` keyword.
- Arrow functions do not have a prototype, that's why it fails when `new` is called on it. It doesn't have a prototype so it can't be 'constructed'.
- Arrow functions use the `this` from its enclosing execution context
- If we try to pass a `this` arg to a function using `call()`, `apply()`, or `bind()`, it will be ignored i.e. the arrow function's thisArg always uses the `this` value captured when the function was created
- Arrow functions can't be used as constructors
- the benefit of using an arrow function is seen when we want to access `this` within a callback

Consider the following example:
```javascript
const workshop = {
  teacher: 'Kyle',
  ask(question) {
    setInterval(() => {
      console.log(this.teacher, question)
    }, 1000);
  }
}
workshop.ask("Is this lexical 'this'?");
// Kyle, Is this lexical 'this?'
```
How are we able to use the value of `this.teacher` in the arrow function? <br/> Explanation: <br/>
There does not exist a `this` binding in an arrow function. Instead, we treat the arrow function like a regular function and use lexical scope to resolve the `this` keyword's value. <br/>
Using lexical scope, we go from the callback function scope to the enclosing scope, one level up, which is the `ask()` function to determine the value of `this`. `ask()`'s definition of the `this` keyword is set by the call-site. <br/>
When the callback in `ask()` is later invoked, it has closed over the parent scope that had a `this` keyword pointing at the `workshop` object 

```javascript
var workshop = {
  teacher: "Kyle",
  ask: (question) => {
    console.log(this.teacher,question)
  }
}
workshop.ask("What happened to 'this'?");
// undefined "What happened to 'this'?"
workshop.ask.call(workshop, "Still no 'this'?");
// undefined "Still no 'this'?"
```
Arrow functions do not have a `this`, so we resolve `this` lexically. `workshop` object is not a scope.
There are only two scopes in this script - the scope of the `ask()` function, which is an arrow function, and the global scope.
A common source of confusion is to thinking that the curly braces create scope but they don't.

Another note, properties aren't scoped, they aren't lexical identifiers, it's a member on an object value, it does not participate in lexical scope at all.

When we do use an arrow function, we want the `this` to behave lexically, i.e. adopt the behavior of some parent scope. The right situation, like a `setTimeout()`  or when we use a `bind()` function

```javascript
const counter = {
  count: 0,
  incrementPeriodically: function() {
    console.log(this)
    setInterval(() => {
      console.log(++this.count)
    }, 1000)
  }
}
counter.incrementPeriodically();
```
Now our callback uses the `this` binding from the `incrementPeriodically()` method. Since we invoked `incrementPeriodically()` using the method syntax, `this` is set to `counter` and everything works out.

### This in class bodies
We will demonstrate how to properly bind `this` value to class bodies so the invocation will result in expected behavior. <br/>
Don't do the following:
```javascript
class Person() {
  constructor(firstName, lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }
  sayHi() {
    console.log(`Hi my name is ${this.firstName}`)
  }
}
const person = new Person('Kyle', 'Fong');
const greet = person.sayHi;
greet(); // cannot read property 'firstName' of undefined
```
We get an error because we are invoking `sayHi()` as a function, `greet()`, and we lose the receiver of the method. *Also*, the class body has an implicit `use strict` mode. To observe the expected behavior, we can use an explicit `bind()` method in the constructor of the class to tie the `sayHi()` function to `person`.
```javascript
class Person {
  constructor(firstName, lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.sayHi = this.sayHi.bind(this);
  }
  sayHi() {/*...*/}
  // Alternatively, we can use class fields and arrow functions
  // to implicitly bind this to the instance of the class
  // sayHi = () => {/*...*/}
  // and remove the explicit binding from the constructor
}
```
### End of TL;DR
# Classes and Prototypical (Delegation)
Let's get this straight - Javascript does not have inheritance. There are only objects delegating behavior to other objects via `[[prototype]]`-linkage. And neither do we make copies of methods and properties of "classes" when we instantiate them, rather, the object references properties. **Again, all we're doing is objects delegating to other objects**. <br/>
&nbsp;&nbsp;&nbsp;&nbsp; Classes are a way of implying relationships through data structures. <br/>
in Java - everything is a class. <br/>
in Javascript - we have `class` syntax but, **simply and cleary**, Javascript does not have classes. We fake classes for the sake of developers who are accustomed to class-oriented languages.<br/>
&nbsp;&nbsp;&nbsp;&nbsp; In this section, we will discuss, in detail, patterns of inheritance, creation and instantiation of objects, delegation of behavior, compare prototypical and classical inheritance, and do so much more.
### Polymorphism
Polymorphism describes the behavior of a child class overriding a parent class for greater specificity. The elegance of polymorphism is implicit inheritance, when a child is able to inherit behavior from their parent. However, the mechanism of polymorphism gives rise to the diamond problem. <br/>
&nbsp;&nbsp;&nbsp;&nbsp; The diamond problem: if a child shares two parents whose methods or properties are named the same - the question is, which parent will the child inherit from? We lose the elegance of polymorphism, implicit inheritance, if we had to explicitly state the parent that the child must inherit from.
### Class-oriented languages v. Prototypical languages
In class-oriented languages:
- Defining a class creates a 'blueprint'.
- In order to use a class, we must instantiate it by creating an object
- An object is a copy of the class
- Constructors are special methods. When we invoke a class, the constructor creates the object for us initializing any information of the instance that we'll need
- Classical inheritance implies copies
- And multiple inheritance opens the door for the diamond problem
For prototypical-delegation:
- In Javascript, **there are just object delegating to other objects**
- Object instances and "children" don't inherit properties and methods, instead, their properties and methods are prototype linked to other objects. Object instantiation doesn't create a copy of the "parent" class. In other words, when invoking a method or accessing a property, we delegate behavior up the prototype chain.

### The role of constructors, `__proto__`, `[[prototype]]`, and `prototype`
#### Constructors
There are no constructor functions, only construction calls of functions. <br/>
Pre-ES6, constructors are just functions that are called with the `new` operator in front of them. This makes a function call a *constructor call*.

When a function is invoked with a `new` operator in front of it, the following happens:
- a brand new object is created
- the newly constructed object is `[[prototype]]` linked
- the newly constructed object is set as the `this` binding for that function call
- we make a `new`-invoked function call using the newly constructed object, and automatically return the newly constructed object, unless specified otherwise

Consider:
```javascript
function foo(a) {
  this.a = a
}

var bar = new foo(a);
console.log(bar.a);
```
**Explanation:** by calling `foo()` with `new` in front of it, we've constructed a new object. This newly constructed object is the `this`-value when we invoke `foo()`.

In summary, when placing a `new` operator in front, functions become constructor calls. `new` creates a new object, almost as a side-effect, and continues to do whatever else it was going to do. <br/>
For example:
```javascript
function NothingSpecial() {
  console.log( "Don't mind me!" );
}

var a = new NothingSpecial();

a; // "Don't mind me!"
   // NothingSpecial {}
```
Explanation: As a result of making a constructor call using the `new` keyword, a brand new object is created, `NothingSpecial()` is invoked and `"Don't mind me!"` is printed. The newly constructed object is `[[prototype]]` linked to `NothingSpecial()`'s prototype. <br/>
As another demonstration of pre-es6 constructor behavior, let's look at the following snippet:
```javascript
function Foo() {}
Foo.prototype.constructor === Foo; // true
var a = new Foo();
a.constructor === Foo // true
```
`a.constructor` actually delegates to `Foo.prototype`. `a` is `[[prototype]]` linked to `Foo()` as a result of invoking `Foo()` with a `new` operator in front of it.

### The prototype chain
We're going to use the following example to demonstrate the prototype chain. Please follow along

```javascript
function Workshop(teacher) {
  this.teacher = teacher;
}
Workshop.prototype.ask = function(question) {
  console.log(this.teacher, question);
}

var deepJS = new Workshop("Kyle");
var reactJS = new Workshop("Suzy");

deepJS.ask("Is 'prototype' a class?");
// Kyle Is 'prototype' a class?

reactJS.ask("Isn't 'prototype' ugly?");
// Suzy Isn't 'prototype' ugly?
```

In javascript, we have a master function called `Object`. On the `Object` function, there's a property that points to an object called `prototype`. 
If you've ever seen `Object.prototype` that's the property pointing from the `Object()` function over to `Object.prototype`.

The property pointing back in the other direction, from the `prototype` to the `Object()` function is `constructor`. This is mad confusing because it implies the `Object()` function is the constructor of the object pointing to it.

The word `constructor` does not have the same meaning that `constructor` has in other class-oriented languages like Java or C++. Instead, `constructor` could be named anything, really. The naming of this property as `constructor` has only one purpose, to maintain a facade that Javascript is a class system.

Provided the background about functions and prototypes, let's dive into our above example to understand how the prototype chain works.

On line 1: We have a function called `Workshop()`, and implicitly, we've created an object called `Workshop.prototype`. The `Workshop()` function points to `Workshop.prototype`. There is also a linkage from `Workshop.prototype` to the `Workshop()` function called `constructor`. The purpose of `constructor` is only to suggest that the `Workshop.prototype` object was created by the `Workshop()` function, as if the `Workshop()` function is a contructor for classes - this is simply not true. Finally, there is a hidden relationship between the `Workshop.prototype` and `Object.prototype`.
On line 4: We make a property and add directly to `Workshop.prototype`.
On line 8: We use the `new` keyword to . let's discuss what it does

1. creates a new empty object
2. links the object to `Workshop.prototype`
3. invokes the function that it was called in front of `Workshop()` with the `this` keyword pointing at the object. As we see on line 2, we put the property called `teacher` directly on the newly created object
4. if the function, `Workshop()`, does not return an object, we return the `this` keyword i.e. the newly created object, and it's named `deepJS`.

Notice that the `teacher` property exists on the object, and the `ask()` **does not get copied to the instances of the `Workshop.prototype`**. Instead there is an internal hidden linkage, the prototype chain (in spec it's `[[prototype]]`),  between `deepJS` and `Workshop.prototype`. 

On line 11: When we invoke the `ask()` method, the `this` keyword points to the `deepJS` object. Why? Because the call-site is line 11. The call-site says invoke the `ask()` function in the context of `deepJS`. Although we found `ask()` by looking up the prototype chain, we use the call-site to invoke it.

In summary, we're able to share methods with potentially many instances because of the `this` binding behavior and the prototype linkage.

### Dunder prototype
```javascript
function Workshop(teacher) {
  this.teacher = teacher;
}
Workshop.prototype.ask = function(question) {
  console.log(this.teacher, question);
}

var deepJS = new Workshop("Kyle");
deepJS.__proto__ === Workshop.prototype; // true
Object.getPrototypeOf(deepJS) === Workshop.prototype; // true
```
On line 12: `deepJS.__proto__` is pointing at `Workshop.prototype`. 
It seems like `deepJS` has a property on it that points to the the thing it's linked to, except `deepJS` doesn't have a property on it called `__proto__`. So, when `deepJS` tries to access a `__proto__` it goes up to `Workshop.prototype` and asks if `Workshop.prototype` has `__proto__`. `__proto__` is linked to the `Object.prototype`. `__proto__` is not a property, it's a getter function. When `__proto__` is invoked, internally, it's `this` keyword will be the call-site, `deepJS`. We must consistently apply the `this` binding rule. We invoke the gget function on the `Object.prototype` in the `this` context of our instance and gives us the hidden internal linkage, the prototype chain.

### Javascript is Behavioral Delegation - forget class systems and classical inheritance
Javascript is prototype delegation system, not a class system. The mental model of class inheritance and child copying parent classes does not fit in a delegation system.
A prototypical system is a superset of a class system because we can implement a class system with prototypical delegation, but not the other way around. Therefore a prototypical system is more powerful than a class system. If we embrace the prototypical system of Javascript, what else, other than writing class-based code, can we do with it?

Javascript and Lua are the other languages where you can create an object without a class. Javascript and Lua really are true Object-Oriented languages

### Differences between Object.create() and the new keyword
The object returned from Object.create() is not `[[prototype]]`-linked to the source object we used to call it with.
`new` creates a new plain object, looks at the function and takes the object on its `prototype` property, and it will set that object (the one found on the `prototype` property) as the prototype of the simple object that it just created. It will feed the plain object into the constructor of the function as it's `this` keyword and return w/e the function returns
### Object Links
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
### Mixins
Mixins are a way of copying an object's methods and properties to a target object. Mixins can lead to multiple inheritance. If a method is defined on the target object,

### `static` keyword
Using `extends` links "parent class" `ObjectA.prototype` to `ObjectB.prototype` via `[[Prototype]]` link. Additionally `ObjectA()` is `[[Prototype]]`-linked to `ObjectB()`.

When `static` is declared before a method or property, these methods/properties are added directly to the class's function object, and not to the function object's `__proto__` object.
Suppose:
```javascript
class Foo {
    constructor() {
        this.a = 2;
        this.b = 4;
    }

    modifyConstructor() {}
    static props() {}
}

var b = new Foo()
console.log(b)

class Bar extends Foo {}

var c = new Bar();

console.log(c);
```
 The `extends` keyword `[[prototype]]`-links `Bar` to `Foo`. `Bar`'s `__proto__` object does not have the statically declared method `Props` because `Props` is added to `Foo`'s function object, and not the `__proto__` object. <br/>

### `super()`
There are two ways of using `super`.
- In the constructor, `super` automatically refers to the parent constructor.
- In method definitions, `super` is used as a property reference or method call. `super` refers to the "parent object" and we can make a property/method access off it. An example of using `super` in a method definition is shown below in `super.gimmeXY()`
For example:
```javascript
class Foo {
  constructor() {
    this.x = 5;
    this.y = 2;
  }
  gimmeXY() {
    return this.x;
  }
}
class Bar extends Foo {
  constructor(a,b,c) {
    super(a,b);
    this.z = a;
  }
  gimmeXYZ() {
    return super.gimmeXY() * this.z;
  }
}

var b = new Bar(5,15,25);
b.x;
b.y;
b.z;
b.gimmeXYZ();
```
To explain a bit more about the above snippet: `Bar extends Foo` means that `Bar` is `[[prototype]]` linked to `Foo` i.e. `Bar.prototype` linked to `Foo.prototype`. <br/>

### Gotchas of `super()` (Source: YDKJS Ch.3, Simpson)
`super` seems as if it is driven by a function's context, like the `this` keyword -- that they are dynamically bound, but that is not the case. When a constructor or method makes a `super` reference, that `super` is statically bound to that class heirarchy and cannot be overridden.<br/> This really applies to mixins.
&nbsp;&nbsp;&nbsp;&nbsp;If we borrow methods from one class and override its `this` with `.call(..)` or `.apply(..)`, we will get unexpected behavior if the method has a `super()` in it. Particularly, this applies to mixins.<br/>
Consider:
```javascript
class ParentA {
  constructor() { this.id = "a"; }
  foo() { console.log( "ParentA:", this.id ); }
}

class ParentB {
  constructor() { this.id = "b"; }
  foo() { console.log( "ParentB:", this.id ); }
}

class ChildA extends ParentA {
  foo() {
    super.foo();
    console.log( "ChildA:", this.id );
  }
}

class ChildB extends ParentB {
  foo() {
    super.foo();
    console.log( "ChildB:", this.id );
  }
}

var a = new ChildA();
a.foo();          // ParentA: a
              // ChildA: a
var b = new ChildB();   // ParentB: b
b.foo();
```
All seems fairly natural and expected in this previous snippet. However, if you try to borrow b.foo() and use it in the context of a -- by virtue of dynamic this binding, such borrowing is quite common and used in many different ways, including mixins most notably -- you may find this result an ugly surprise:
```javascript
// borrow `b.foo()` to use in `a` context
b.foo.call( a ); // ParentB: a
     // ChildB: a
```
`class` + `super` prevents us from cross-pollination of prototype linking multiple objects that do not have a strict static-class heirarchy. One of the benefits of `this`-aware coding is exactly cross-pollination.
We are better off embracing dynamic and flexible classless objects and `[[prototype]]` delegation
Additionally, static properties are inherited and we can super-call static methods.

### Subclass constructor
The default substituted constructor is different for a direct class versus an extended class. The default subclass constructor automatically calls the parent constructor, and passes along any arguments like so:
```javascript
constructor(...args) {
  super(...args);
}
```
Another limitation of ES6 subclass constructors is that, in a constructor of a subclass, you cannot access `this` until `super(..)` is called because the parent constructor is the one creating and initializing the instance's `this`. Pre-ES6 works oppositely; the `this` object is created by the "subclass constructor" and then we call a "parent constructor" with the context of the "subclass" `this`.
```javascript
function Foo() {
  this.a = 1;
}

function Bar() {
  this.b = 2;
  Foo.call(this);
}

// Bar "extends" Foo
Bar.prototype = Object.create(Foo.prototype);

// But this isn't
class Foo {
  constructor() { this.a = 1; }
}

class Bar extends Foo {
  constructor() {
    this.b = 2;  // not allowed before `super()`
    super();     // to fix swap these two statements
  }
}
```
Basically, call `super()` first. Also, using classes, we won't be able to skip calling the 'parent constructor' any longer.


### Mixins and multiple inheritance












If we do not have a constructor for a base class, the following definition is used
```javascript
// default behavior if no constructor is specified
constructor(...args) {
  super(...args);
}
```

We cannot access `this` until `super()` is called. The reason being, the parent constructor is acutally the one creating/initializing the instance's `this`. Pre-ES6, it works oppositely; the `this` object is created by the constructor, and we call a "parent constructor" with the context of the subclass `this`

How does `super` and `this` binding relate to mixins?

### Static methods
Static properties (or class properties) are properties of the object itself. We create a
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

### Subclassing

### Patterns
- OLOO
- Prototypical inheritance
- Object.create(object.assign())
- ES6 classes




```javascript
class Foo {
  constructor(a,b) {
    this.x = a;
    this.y = b;
  }

  locate() {
    return this.x + this.y;
  }
}

// Prototype-style equivalent
function Foo(a,b) {
  this.x = a;
  this.y = b;
}

Foo.prototype.location = function() {
  return this.x + this.y;
}
```




























- `[[Prototype]]` simply means a reference to another object.
- the `new` keyword prototype links objects, it does not copy over functions or properties
When classes are inherited there is a way for the classes themselves to relatively reference the class inherited from (*and not the object instances
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




In the following example, we will observe the how `this` can be used to create behavior across multiple contexts:
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
**Explanation:** The code snippet allows `identify()` and `speak()` to be re-used against multiple contexts, the me and you objects, rather than needing a separate version of the function for each object. The next example demonstrates explicitly passing context:
```javascript
function identify ( context ) {
  return context.name.toUpperCase();
}
function speak ( context ) {
  var greeting = "Hello, I'm " + identify(context);
  console.log(greeting);
}

identify(you); // READER
speak(me); // Hello, I'm KYLE
```
