#!/bin/bash

mvn -Dspring-boot.run.fork=false -Dspring.profiles.active=dev spring-boot:run "$@"
