# Testing System for Subscription and Notification Services

## Overview

This project aims to set up a testing environment to compare subscription and notifications services. 


### The subscription and notification services

The subscription and notification services considered are those that allow to the user to subscribe for notificatios on specific changes to triples in an RDF Store. 
  
### Metrics

We are interested in performance test considering the following metrics:

* **Query response time**: Depending on the implementation of the Service may impact the performance of the RDF Store. Thus, a list of arbitrary queries are prepared to be able to test Store query response time.
* **Notification response time**: We want to know how quick the Service will notify the user of a given subscription.


### Testing System Components

This testing system considers the following components:

* RDF Store (the subject of changes)
* Subscription and Notification Service, may be composed of:
  * Server component: to register subscriptions and send notification
  * RDF Store component : specific implementation of the RDF Store to track its changes
* Sparql Query Generator : This component will generate activity for the RDF store by sending SPARQL queries
* Testing System Manager: (this project itself) will orchestrate activitiy between above mentioned components and perform the corresponding measurements.

### Testing System Manager  

* SubscriptiionNotificationService Interface
* SPARQLQuery Proxy
* SparqlSimulator Interface
* Manager


## Install

To generate the war file:

	mvn package



