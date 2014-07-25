# Testing System for Subscription and Notification Services

## Overview

This project aims to set up a testing environment to compare subscription and notifications services. 

The Subscription and Notification Services (SNS) considered are those that allow to the user to subscribe for notificatios on specific changes to triples in an RDF Store. 

We are interested in performance test considering the following metrics:

* **Query response time**: Depending on the implementation of the Service may impact the performance of the RDF Store. Thus, a list of arbitrary queries are prepared to be able to test Store query response time.
* **Notification response time**: We want to know how quick the Service will notify the user of a given subscription.


### Testing System Components  

This thesting system is composed of follwing elements:

* SubscriptiionNotificationService Interface: this will interact with the SNS to perform corresponding subscriptions.
* SPARQLQuery Proxy: this proxy will monitore queries executed agains the store and measure the response time.
* SparqlSimulator Interface: this will interact with sparql client simulators, basically to start/stop the simulation
* Manager: provides the iterface to start and stp the testing

### Implementations

Currently we have implemented Subscription and Notification Service interfaces for:

* [Rsine](https://github.com/rsine/rsine)

And Sparql Client Simulator:

* Simple SPARQL Client simulator: reads txt files from the system and executes the queries against the Proxy
* Supply Chain Dashboard: generates messages of product shipping in a supply chain network (not open source).

## Configure

The services URLs as well the triple store endpont URL have to be provided in the `web.xml` file context parameters. 

## Install

You need first to configure the URL services for the different components in the web.xml file. Then, you can generate the war file using maven:

	mvn package

Install the war file in a servlet container.

These instruction do not include the services to evaluate. You need of course, to perform the corresponding installations of SNS (e.g. [Rsine](https://github.com/rsine/rsine)), triple store and Simulators accordingly. 


## Run

The testing system can be run using the following parameters:


parameter    | description
------------ | -------------
?action=run  | initialise the system by registering subscriptions and starting the simulation  
?action=stop | stops the simulation and computes statistics


