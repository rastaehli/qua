todo
- explain: 
    * pretty-print an implementation tree: 
        * each node's type follow by child dependency nodes (and builder)
    * build catches exceptions, shows error and context (plan/provision/build/activate for each goal in stack).  For example, "could not find implementation plan for TypeX, in repositories Y and Z, while provisioning plan for B, while provisioning plan for A."
- justify "implementationByName".  
    * This supports distinct identity for alternate implementations of same type.
    For example, if one impl is for republicans, the other for democrats
    the dependencies can bind to the name as a property, guaranteeing
    they get the correct instance.  Does name need to be a special property of
    all types in the model or can it just be a regular property of types that
    need it?  Even for a singleton, it is helpful for a set of dependencies
    to identify the dependency by name to ensure they all get the same
    instance.
- unit test diagnostic messaging when construction plan is not found:
    * show description of what it was looking for 
    * show chain of reasoning, why it was looking for an implementation.  
    * show where it looked for implementations
- test that match/specialize/merge operations do not reference mutable data, that repository advertised implementations are not modified.
