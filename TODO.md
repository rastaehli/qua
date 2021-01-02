todo
- Description has subsumed type, plan, quality, for the purpose of flattening meta info for easy serialization?  Consider how to encapsulate these aspects.
- component build model: type, plan (top level, recursively plan dependencies), build/assemble (instantiate implementation interfaces, bottom up), startUp.
- justify "implementationByName".  
    * This supports distinct identity for alternate implementations of same type.
    For example, if one impl is for republicans, the other for democrats
    the dependencies can bind to the name as a property, guaranteeing
    they get the correct instance.  Does name need to be a special property of
    all types in the model or can it just be a regular property of types that
    need it?  Even for a singleton, it is helpful for a set of dependencies
    to identify the dependency by name to ensure they all get the same
    instance.
- 
