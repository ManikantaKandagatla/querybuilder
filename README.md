# querybuilder
A simple SQL query builder utility


Pre-requisites: 
1. Define metadata of entities(Optional) 
2. Define the report config supported by the builder utility.

what this utility does ? 
1. Translates a Report config to a RAW SQL query. Client can execute the query on their RDBMS datasource.

Supports: 
1. Simple select query 
2. Supports 3-5 level join query
3. Supports filters on string and integer datatypes
4. Supports Orderby
5. Supports groupBy

Sample Request: 
https://github.com/ManikantaKandagatla/querybuilder/tree/master/src/main/resources/samples for request paylod needs to be send to this utility

API: 

1. POST : http://localhost:8080/getData
   Request payload: sample request

2. POST : http://localhost:8080/buildQuery
   Request payload: sample request

How to run:
1. Download the code
2. Change the datasource properties accordingly in application.properties
2. Run : mvn clean install 
3. Start springboot application -> main class: QuerybuilderApplication

