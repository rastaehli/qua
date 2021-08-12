# QuAJava

## Overview
This is a Java port of the QuA Component Middleware (http://map.squeak.org/package/f82c43b7-2d5e-4723-9298-7c36291f50aa) code developed at Simula Research Laboratory.

This current version is stripped of the original support for "Quality of Service" description and adaptation that was the reason for the development of QuA, but retains the essential model of component description through a "Description" meta-object.
This description meta-object allows us to 
* specify the _type_ and _properties_ of a component, 
* find matching _implementation_ plans consisting of a _builder_ and the _dependencies_ required by that builder,
* provision, assemble, and activate a component, and
* access that component as any other object or service of that type.

The power of this model comes from a _repository_ of component descriptions:
* activated descriptions advertise the availability of existing objects and services,
* planned descriptions advertise a way to assemble a component from dependencies.
Any service you can describe (type and required properties) can be built automatically
if the repository has an implementation plan (a "planned description") for the type/properties and,
recursively, implementation plans are found for all plan dependencies.  
This does not happen by magic.  Implementation plans are developed and advertised in the repository
to satisfy requirements just like in any engineering effort, but QuA allows the same simple description
meta objects to be used at every level of construction, from simple runtime object 
construction to distributed systems provisioning.

## Licence

The following is the "MIT Licence" text:

Copyright 2020 Richard Staehli

Permission is hereby granted, free of charge, to any person obtaining a copy of this soft_ware and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
