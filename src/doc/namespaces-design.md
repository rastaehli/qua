## Problem: names that do not conflict

### Context:
The QuA component model requires that we create names for types,
singleton implementations, builders and such.  Applications using these names 
may span many domains, creating a high risk of collisions: different definitions
for the same name.

This problem is commonly solved (on the web, and in Java for example) with namespaces:
uniquely named and owned domains where the owners can locally enforce their 
own rules for meaningful names without fear of conflict from other domains.

### Solution:
Qua adopts the semantic web standard of URIs to define a namespace.  For example,
the URI "https://github.com/rastaehli/qua/" can be used by the project owner
as a namespace prefix for the type "Builder" to avoid conflicts with how others
have used the name.

To make use of these long names easier, QuA also adopts the following techniques:
- A fully qualified name is a string of the form <namespace>/<name>.  A fully
qualified name can be used in any context to refer to the object that <namespace>
associates with <name>. 
- The QuA object is used to define the context and can be constructed with
aliases for commonly used namespaces.  When using this qua context object a
name of the form <alias>:<name> will be automatically translated to the fully 
qualified name <namespace>/<name> when a mapping <alias> to <namespace> is found.
- A Repository defines its own namespace for the descriptions advertised in it.
When a description with "name" property equal to <aName> is advertised in a 
repository R with namespace <rNamespace> it may be retrieved from R by name <aName>
or from any repository (that can locate R by its namespace) by the fully qualified
name <rNamespace>/<aName>.
