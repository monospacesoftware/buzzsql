# BuzzSQL

BuzzSQL is a thin layer over Java JDBC that manages automatic configuration of 
database connection setup, pooling, configuration, logging, and statement 
handling.  It achieves a middle ground between using straight JDBC connections 
for database access, and more complex object-relational mapping libraries such 
as Hibernate, Torque, or Cayenne.  

BuzzSQL is super easy to setup, in some cases requiring no extra configuration 
files at all.  This is achieved by an intelligent database connection 
auto-discovery search process.  

The object structure is clean and straight forward, making the library quick 
to learn and use by novice Java developers.  The objects are named according to 
standard SQL statement names; Select for Select, Insert for Insert, Update for 
Update, etc.

The library does not generate Java code, map relational tables to objects, or 
write SQL for the user.  Users must know SQL.  BuzzSQL uses Java 5 enhancements 
such as Autoboxing and Vargs to make creating and calling SQL statement object 
constructors simple and straight forward.

BuzzSQL is licensed under the LGPL 2.0.

+ [Download](https://github.com/monospacesoftware/buzzsql/raw/master/archive/buzzsql-1.3.8.zip)
+ [User Guide](https://htmlpreview.github.io/?https://raw.githubusercontent.com/monospacesoftware/buzzsql/master/doc/userguide.html)
+ [Examples](https://github.com/monospacesoftware/buzzsql/tree/master/example)
