# DDD and Object Oriented: Concepts, Analysis, Heuristics

## Points
### Why do we move logic from a controller?
If we make a change, like add a property to the domain model, we have to make the same change for every use case of that model, and there could be n more use cases. We would also add m validations for that property - so one change could result in n x m changes.

### What roles do factories play? What do we put in them? When do we use them?

### how can we apply the observer pattern in order to signal when relevant things happen, directly from domain layer itself?

## DDD
- Crud-first code is written imperatively. Acceptable until we need to respond to business rules.
    + imperative code requires us to specify exactly *how* everything happens
    + Crud-first design is a transaction script
        * to make the design of our code more expressive, declarative we can use DDD - domain modeling
- DDD can give us clean abstractions so that code is organize by responsibility
    + give us SOLID principles

## Controller Layer
- Responsibilities: 
    + routes incoming requests and returns responses
    + orchestrates transformation of data (can call utilize a mapper)
    + serializes and deserializes data representations
        * deals with JSON and HTML
    + performs high level security and authentication
- when handling a response, returns a view model

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
- listen, they should have domain logic and validation in it

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
- data containers used to transport data between layers and tiers
- can help us standardize our API response structure so that when we
    - perform migrations
    - add new columns 
    - remove a column
    - change the name of a column
we don't break the API for each client that depended on it
- can be thought of as a data contract b/c it provides a format that a client can always expect to see from an API call
- should only contain attributes, getters, setters, private fields but no business logic
- what would be the conditions for it to be acceptable to add utility methods

## Repositories
- act as a facade over complex queries and persistent technologies
    + facade is simply an interface over a larger body of code, like domain entity persistence and domain entity retrieval logic
- repositories should be created by entity/database table
    + instead of getById, prefer getReportById and getReportOwnedByUserId
- repos are an infrastructure-layer concern
- generally speaking, their role is to persist and retrieve domain entities
- Persistence
    + Responsibilities: 
        * scaffold complex persistence logic across junction and relationship tables
        * rollback transactions that fail
        * on `save()` check if the entity already exists and then perform the create or update
    + for example of the complex data access logic of the persisitence layer: only repositories should know the logic for "create if the entity does not exist, else update the entity", and not other layers
- Retrieval
    + Responsibilities: 
        * retrieve the entirety of data needed to create domain entities (like a report also contains feeling, identity; this information must be gathered from other tables)
        * what is needed to be included in order to create DTOs and domain objects

        - This goal of this layer is to minimize duplicate query logic
        - The kind of code in this layer is query construction
        - Exposes itself to mapping layer
        - The repository uses the data access object to reconstruct the domain entity using one or more than data transfer object
- Prefer to use more descriptive language when implementing a repository
    + for example: getReportById, instead of generic getById from a generic repo class
        * saves from data access layer leaking into calling code
        * more explicit about what we want

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

## Sources
- https://khalilstemmler.com/articles/typescript-domain-driven-design/repository-dto-mapper/
- 

Notes:
- Should repository layer return data transfer object?
    + no, repository is responsible for turning persisted data back to entities (models) and vice versa
    + model is a business model representing a business entity. DTO on the other hand, *while it looks like a model*, is concerned with transfer of the object between various environments and is a transient object. Usually mappers are responsible for turning the model into a data transfer object
    + what about the specialized transfer objects for between the service and data layer for more complex queries to avoid having to do multiple queries?
    + source: https://stackoverflow.com/questions/5068984/should-the-repository-layer-return-data-transfer-objects-dto?rq=1
- What is the difference between data access object and repository pattern?
    + DAO is an abstraction of data persistence
    + Repository is an abstraction of collection of objects
    + DAO is considered closer to the db, often table-centric
    + Repository would be considered closer to the Domain dealing only in aggregate roots
        * aggregate roots are domain concepts (order, playlist, clinic visit, report) and not collection classes (lists, maps, etc.) in generics. consistency boundaries for transactions/concurrency and deemphasize that outside entities cannot hold references to other aggregate's child entities 
        * an example is a model containing a customer entity and an address entity. We would never access an address entity directly from the model because it doesn't make sense without the context of the associated customer. So customer and address combined form and aggregate and the aggregate root is customer
        * aggregate roots are consistency boundaries for transactions/concurrency
        * aggregate root encapsulates multiple classes and we can manipualte the whole heirarchy through the main object
        * source: https://stackoverflow.com/questions/1958621/whats-an-aggregate-root
    + The repository is a narrow interface, simply a collection of objects with get(id), findBy(), add(entity)
    + The repository groups data access objects in order to create a single entity (domain object)
    + source: https://stackoverflow.com/questions/8550124/what-is-the-difference-between-dao-and-repository-patterns?rq=1
- Data transfer objects are only useful for when you have a significant mismatch between the model in your presentation layer and the underlying domain model. In this case, it makes sense to make presentation specific facade/gateway that maps from the domain model and presents an interface that's convenient for the presentation. It;s a pain in the ass to use, second to only ORMs

Problem:
I don't know the right level of abstraction for each layer
- the responsibilities of the controller
- what a domain object is
- what domain logic is and where we write it
- what a repo, dto, and mapper looks like

Value objects: responsible for handling validation logic
Where do we handle domain logic - as close to the entity as possible, otherwise domains ervices
Repositories, Data Mappers, DTOs are tools to help us store, create, and delete domain entities
    - also known as data access logic
must encapsulate data access logic

### Data Mappers
used to translate domain to DTOs, domain entities to persistence, and persistence to domain

- handle API request (controller's responsibility)
- perform validation on the domain object (domain entity or value object responsibility)
- persist a domain entity to the db (repository responsibility)

## Value Objects
see: https://khalilstemmler.com/articles/typescript-value-object/
We introduce value objects to encapsulate where validation should occur and satisfy invariant properties
- For example: a user's username must be between 2 and 100 characters, a user password must be encrypted and hashed, a user must have a unique identity UUID
- validation on object creation is normally delegated to value objects, but what can happen and when is up to the entity
- Ask what are the pre-conditions and required parameters in order to create this object?
- OOP is about decomposing problems into smaller classes and modules, bite-sized chunks of logic. Value objects are representative of OOP because it encapsulates validation logic

## Understanding Entities
see: https://khalilstemmler.com/articles/typescript-domain-driven-design/entities/
### Entities
- What are entities exactly?
    + Entities are a form of an object that represents something meaningful to our particular business domain. Domain objects that may have an id. We model an entity using a class
- Sometimes it doesn't feel right or natural to put certain domain logic inside of an entity.
    + entities reference one another (like a post references a comment), and there are other times where two entities should not necessarily know about each other
    + For example, if we were modeling a Movie Rental application, with a Customer entity and a Movie entity, where do we put the purchaseMovie() method?
    + A Customer can purchase a movie, but the Customer entity shouldn't need to know anything about Movies.
    + Conversely, a Movie can be purchased by a Customer. But we wouldn't want to reference a Customer in the Movie model, because ultimately, a Customer has nothing to do with a Movie.
    + This is the type of logic that we put in a Domain Service instead
    + What's a domain service?
        * This is where we locate domain logic that doesn't belong to any one object conceptually. Not to be confused with an Application Service. Domain Services only operate on Domain Objects, whereas Application Services are artifacts that are unpure to the domain, that may pull data from external resources (APIs, object databases, etc, and so on).

What would a entity base class look like?

### Factories
Factory methods are used for creating entities.
- For instance: if we wanted to write many variations of a User, we would write an abstract factory and let each user factory specify their own implementation

## Anemic Domain Model
see: https://khalilstemmler.com/wiki/anemic-domain-model/
What is it:
- Domain objects that contain little or no business logic (validations, calculations, business rules etc.)
- They lack encapsulation and isolation

Benefits of a rich domain model:
- better discoverability of where new code belongs, so adding new features takes less time
- any logic in services that doesn't solely belong to a single entity should remain a Domain Service. Otherwise, the business logic should be moved to that entity
- Any logic performed on external resources (like calling a 3rd party API) should belong in an application service


## Clean Architecture
Model = Domain Layer
see: https://khalilstemmler.com/articles/enterprise-typescript-nodejs/clean-nodejs-architecture/
Does this code enfore a rule about how something shoul work in my domain?
Or does this code simply make something work?

### Policy 
Policy: abstractions, interfaces, business logic, declarative
- specifying what should happne and when
- mostly concerned with business-logic, rules and abstractions that exist in the domain we're seeking to code

### Detail
Detail: concretions, implementations, infrastructure, imperative
Details: controllers, routes, databases, services to external APIs, caches, ORMs, framework language
Policy: entities, business logic and rules, domain services, domain events
We must write interfaces between detail and policy

Domain layer code can't depend on infrastructure code **BUT** infra code *can depend* on domain layer code (because it goes inwards). We are following the rules of dependency inversion (part of SOLID principles)

### Ports and Adapters
Ports are interfaces, the abstract classes - specifying what a service can do, but doesn't acutally implement those things specifically (policies)
Adapters are the concrete classes - the implementation (details)

- Suppose we have an email service. In this service, we want to send email
    + In our application, today, we might use mailchimp to send emails, and tomorrow, we might also use sendgrid. 
    + Instead of implementing the sendEmail business logic in both services, we abstract the method sendEmail to an interface.
        * Writing an interface allows us to specify a policy, all that's left to do is create an implementation (mailchimpService extends emailService (implements sendMail), sendgridService extends emailService (implements sendMail))
    + Having zero dependencies in our domain layer code (in this case, the emailService interface) allows us to test it
    + We don't have coupling because we don't have concretions 
    + We ahve better flexibilty in our code, if we change the policy might end up affecting a detail. But if we change a detail, it never affects the policy because the policy doesn't depend on a detail


## Domain, Application, and Infrastructure Services
see: https://stackoverflow.com/questions/2268699/domain-driven-design-domain-service-application-service
### Domain services
encapsulates business logic that doesn't naturally fit within a domain object, and are **NOT** typical CRUD operations - those would belong to a repo

### Application Services
Used by external consumers to talk to your system (think web services). If consumers need access to CRUD operations, they would be exposed here

### Infrastructure Services
Used to abstract technical concerns (email providers)

Keeping Domain Services along with your Domain Objects is sensible – they are all focused on domain logic. And yes, you can inject Repositories into your Services.

Application Services will typically use both Domain Services and Repositories to deal with external requests.

## Aggregate Roots
see: https://khalilstemmler.com/articles/typescript-domain-driven-design/aggregate-design-persistence/#Aggregates
Aggregates arise from the understanding the kinds of entity relationships. There exists 1-1, 1-many, and many-many entity relationships. In each of these relationships we ask:
- When is it appropriate to create a repository for each of these entities? And what are those signals?
- How do we save multiple entities to the database?
- How do we decide on boundaries for these entities?

Aggregates help us create a boundary around a cluster of associated entities that we treat as a singular unit.
For example: 
a report belongs to one user
a reports belongs to one organization
a report belongs to one location
a report has a feeling
a report has an identity

a user has many reports
a user belongs to an organization
a user has an email
a user has an occupation

Aggregate goals:
- execute use cases
- provide enough info to enforce model invariants within a boundary
- ensure acceptable db perf
- *optional* provide enough info to transform domain entity to a DTO (for the view) so it will make it easier for us to build API response DTOs
    + adding additional info to our aggregate for the sake of having them available for our DTOs has potential to hurt performance, don't do it.
    + DTOs can have a tight requirement to fulfill a user inferface, so instead of filling up an aggregate with all that info, just retrieve the data you need, directly from the repository/repositories to create the DTO.

Suppose we create a report, we should be able to see it right away - we should have an applications service in the backend that listens for reportCreatedEvent




The purpose of Aggregate roots are to perform data changes upon them - like create, update, and delete (but not read)

When we know the use cases of an aggregate root, we then need to define aggregate boundaries s.t. all use cases can be performed, and enough information is provided w/in the boundary to ensure that no operation breaks any business rules, and we consider new business rules and use cases being introduced.

We must think about how many tables we'll need to join in order to create an aggregate. We'll need optimal performance from our db transactions