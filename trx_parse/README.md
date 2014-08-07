# Virtuoso Transaction Log Parsing based rsine complement 

## Overview

This project aims to set up a testing environment to compare subscription and notifications services. 

The Subscription and Notification Services (SNS) considered are those that allow to the user to subscribe for notifications on specific changes to triples in an RDF Store. 

We are interested in performance test considering the following metrics:

* **Query response time**: Depending on the implementation of the Service may impact the performance of the RDF Store. Thus, a list of arbitrary queries are prepared to be able to test Store query response time.
* **Notification response time**: We want to know how quick the Service will notify the user of a given subscription.

## Installation 

* ensure the python package manager ``pip`` is installed

    [sudo] easy_install pip 

* verify that ``python-dateutil`` and ``watchdog`` are installed

    pip list | grep -E "watchdog|dateutil"

* install tem if neccessary
	
	[sudo] pip install watchdog
	[sudo] pip install python-dateutil

## Usage

    python try_parser.py --help 

Above command gives usage information:

    Usage: python trx_parser.py -v <virtuoso_path> -l <virtuoso_trx_log_path> -r <rsine_host> -p <rsine_port>

    Options:
		  -h, --help            show this help message and exit
		  -v VIRT_PATH, --virtuoso_path=VIRT_PATH
		                        The path to virtuoso installation directory.
		  -l VIRT_LOG_PATH, --virt_log_path=VIRT_LOG_PATH
		                        The path where virtuoso transaction logs are
		                        written.
		  -r RSINE_HOST, --rsine_host=RSINE_HOST
		                        The hostname of the server where the rsine service is
		                        running.
		  -p RSINE_PORT, --rsine_port=RSINE_PORT
		                        The port the rsine services listens on.