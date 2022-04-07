This library is currently WIP.

## Features

* [ ] Basic **deep** mapping(Based on field names).
* [ ] Support array or `Collection`.
* [ ] Ignore fields conditionally.
* [ ] ...

## API Usage Design:

API usage might be like this:

```java
Source source = ...; // Get a source bean
Mapper<Source, Target> mapper = MapperFactory.getMapperFor(Source.class, Target.class);

Target target = mapper.map(source); // Deep mapping
```

Or,

```java
Source source = ...; // Get a source bean
Target target = ...; // Get an existing target bean
Mapper<Source, Target> mapper = MapperFactory.getMapperFor(Source.class, Target.class);

mapper.map(source, target); // Deep mapping
```
