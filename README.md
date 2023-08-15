# Multi Tenancy with Quarkus, Hibernate & Liquibase

This project was based on the article https://www.linkedin.com/pulse/projetando-arquitetura-de-um-saas-multi-tenant-em-ruben-lins-silva/ but using Quarkus instead of SpringBoot .

This project will serve as a support application for ShareCar and will be responsible for creating a new database for each new registered tenant, as well as other specific data structures for each new tenant.

The data separation strategy for each tenant will be by database segregation.