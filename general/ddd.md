# DDD and Object Oriented: Concepts, Analysis, Heuristics
## Controller Layer
- public face of the application
- routes incoming requests and returns responses
- orchestrates transformation of data
    + serializes and deserializes data representations
        * deals with JSON and HTML
- performs high level security and authentication

## Application Layer
- returns simple data structures
    + deals with non-serialized data structures and delegates responsibility serializiation to the controller
- domain layer === business logic
    + maintains data integrity
    + contains bulk of the logic
- how do we handle sesssions?
    + must make decision if we want to pass authentication around, or if it should be terminated after controller/some entry layer
        * should we keep servers stateful, or stateless?
- wraps the domain
- ex: suppose a use case that involves two instances of domain objects, such as User and Team, and passes them to JoinTeamPolicy. If the user can join, the application layer delegates the persistence responsibility to the infrastructure layer

## Infrastructure Layer
- provides adaptors for technologies - persistence, email, twitter, stripe, etc.
- deals with simple data structures, structs, arrays, numbers, strings, binary, buffer streams
- also, wraps the application
    + anything that is needed to expose use cases to the world and make the app communicate
    + gives the domain model and application services hands and feet
        * processing HTTP requests, producing a resposne for an incoming request
        * makes HTTp requests to other servers
        * stores things in the db
        * sends emails
        * publishes messages
- requires integration testing - verifies all the assumptions the infrastructure code makes - all the real things - db, 3rd party code, external services
- data access layer lives here, that means data access objects as well
- Remember, data access objects are the low level persistence interface returning a data transfer object

## Data Access Layer
- contains logic that persists domain model in the db
- can be thought of as an adapter of the infrastruture with a bit more functionality - implements the simple wrapper, an interface, that does the dirty db work
- loosely couples the action of pulling data from the data store and business logic
- if we have to change data stores, we don't want to change the interface of how we access data
- abstracts data acesss objects and repository
- Comprised of data access objects

## Domain Model (entity)
- defines constraints, rules, relationships among objects, how they'll behave and the type of data they'll carry
- does not handle crud operations (creating a user, updating an address)
- transferring model data to and from storage will likely require data mapper pattern
- should have knowledge of the kind of data source

### Domain Object
- A domain object is a collection of data without the logic


## Domain Layer (core)
- used by the application layer to define use cases

## Data Access Object
- build and execute queries on the data source and map the results to a plain old javascript object
- we use this to perform any op like selecting/retrieving data from the db. 

### Data Access Object Patterns 
acts as mediator between in-memory objects and the db (persistence)

#### Data Mapper
- a form of the data access object, this type of object handles impedance mismatches behind the scenes
- no SQL interface code, no knowledge of the db schema
- performs bi-directional transfer of data between a persistent store and in-memory data representation
- use if you need abstract domain objects from the db representation or the object impedance mismatch
- acts as a layer between the actual business domain and the db persisting the data
- receives the domain model object (an entity) as a parameter and uses it to implement CRUD operations
- Data Mappers handle loading of instances of a domain model (domain objects) through the persistence layer

#### Table Data Gateway
- receives all the parameters for the methods and will not know anything about the domain model object

- Used in the persistent layer of the application, uses the principle of encapsulation
- implemented correctly, decouples the application from the actual database
    + put another way, data access object patterns hides the underlying db implementation from the class that accesses the data via data access object calls
- ex:
```javascript
class person_domain_model {
    public id;
    public firstname;
    public lastname;
    public addresses;
}

class person_data_mapper {
    /**
    * @param  [integer] $id
    * @return [person_DO] an instance of a person or null, if no person
    *                     with that id was found.
    */
   public function findById (id) {}

   /**
    * @return [array of person_DO]
    */
   public function fetchAll() {}

   /**
    * persists a person object
    * @param [person_data_object] an instance of a person
    */
   public function saveOrUpdate(person) {}
}
```

## Data Transfer Object
- data containers used to transport data between layers and tiers (classes and modules)
    + objects that store data for transferring between layers
    + should only contain attributes, getters, setters, private fields but no business logic
    + you CAN add utility methods

## Repository Pattern
- This goal of this layer is to minimize duplicate query logic
- The kind of code in this layer is query construction
- Exposes itself to mapping layer
- The repository uses the data access object to reconstruct the domain entity using one or more than data transfer object
## Services
- services are higher level domain objects - but instead of business logic, they are responsible for the interaction between domain objects and mappers. These structures end up creating a ‘public’ interface for interacting with the domain business logic. if you avoid them, we run the risk of leaking some of the domain logic into controllers. 

## What is business logic?
- things that are more or less immutable for the company. Logic that will not change
- defines properties of objects
    + ex: object A of type B must have attributes C and D but not E. Application logic is more of a technical spec. like using a java servlet and specific form to persist in a nosql db.
- Business logic - what happens when an order for product x is placed, how is the cost of product y calculated? - the bits of code that pertain to the user’s use case. Application logic is the sys. architecture? Everything is setup to help the business logic execute - database wrapper, service facades?
- business logic is the code where we create real-world business rules around how data is created and changed
- perhaps, the presentation logic manages the interaction with the user, the data logic handles data persistence, while the business logic handles the stuff that happens between the two
the business logic are the flows and rules that only make sense in the context of the user’s business
- for instance: if the user’s are fisheries managers, the business logic looks like: if this landing of fish puts them over their quota limit, lock their account and notify the ticket writing department. Or a person in program A may transfer quota to someone else in program A but not someone in program B unless they have a valid medical transfer certificate.
- diff b/t business and app: some logic reads - if the customer buys 2 products or more, apply discount. if not, don’t apply discount. Others read - if the HTTP response from the remote API is not 201, wait x seconds and retry
- ex business logic: show the cheapest item first, or most frequently purchased. Maybe we want to sell candy bars at a specific price, and maybe limit that offer to one candy per customer

## Rule of thumbs
- only pass around the bare minimum this creates easy to test code
- easy test code is better architected
- good APIs are transparent in their expectations and dependencies
- functions should either do something or answer something, but not both
    + either change the state of an object, or return some information about that object - doing both leads to confusion
## Controller
- should not know about the type of storage being used
- keep the transaction control in the application layer
    + all the controller has to know about is the data that can go into and come out of the application - it doesn't need to know anything else

#### Try
- one level of indentation per method
- not to use the `else` keyword
- one dot per line
- don't abbreviate names
- keep all classes and object less than 50 lines

## Dependency Injection
## Gateway
### Relationship between entities and models
## Dependency Inversion Principle
## Application Service

