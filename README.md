# QuAJava

## Overview
This is a Java port of the QuA Component Middleware (http://map.squeak.org/package/f82c43b7-2d5e-4723-9298-7c36291f50aa) code developed at Simula Research Laboratory.

This current version is stripped of the original support for "Quality of Service" description and adaptation that was the reason for the development of QuA, but retains the essential model of component description through a "Description" meta-object.
This description meta-object allows us to 
* specify the _type_ and _properties_ of a component, 
* find matching _implementation_ plans consisting of a _builder_ and the _dependencies_ required by that builder,
* provision, assemble, and activate a component, and
* access that component as any other object or service of that type.

The power of this model comes from a _repository_ of pre-existing component descriptions:
* activated components provide pre-existing builders and basic services to bootstrap the construction of more complex components,
* planned components advertise a way to assemble a component from dependencies (that may themselves need to be planned and assembled)


## Licence

The following is the "MIT Licence" text:

Copyright 2020 Richard Staehli

Permission is hereby granted, free of charge, to any person obtaining a copy of this soft_ware and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
