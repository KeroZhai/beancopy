# BeanCopyUtils

A simple but useful utility to copy beans.

## Features

* **Deep copying**.
* Support conditional copying for specific fields.
* Support copying between fields with different names or types.
* Support copying array or collection of beans.

## Usage

Usage is as simple as the following examples.

### Basic

Say you have two classes, one serves as source and the other as target.

``` Java
public class Source {
    private long id = 1;
    private int[] numbers = { 1, 2, 3 };
    public String getName() {
        return "source";
    }
}

public class Target {
    private long id;
    private int[] numbers; 
    private String name;
}
```

To copy from a `Source` bean to a `Target` bean, just use:

``` Java
Target target = BeanCopyUtils.copy(new Source(), Target.class);
```

or,

``` Java
Target target = new Target();
BeanCopyUtils.copy(new Source(), target);
```

As you can see, getters/setters are not necessray, however, it's recommanded to provide them since they are Javebeans.

### Advanced

#### Fields Remapping

By default, fileds in two Javabeans are mapped by their names. For those with different names, you can use `@AliasFor` to indicate that the annotated field *is* an alias for a field in another Javabean, just like below:

``` Java
public class Source {
    private long id;
}

public class Target {
    @AliasFor("id")
    private long userId;
}
```

#### Converters

Use `@Converter` to provide a custom converter (class). A custome converter class must implements the `Converter` interface. For example:

``` Java
public class TimestampToDateConverter implements Converter<Long, Date> {
    @Override
    public Date convert(Long timestamp) {
        return new Date(timestamp);
    }
}

public class Source {
    private long timestamp;
}

public class Target {
    @AliasFor("timestamp")
    @Converter(TimestampToDateConverter.class)
    private Date date;
}
```

#### Conditional Copying

Use `@CopyIgnore` to indicate that when (or except) a certain condition is met, the annotated field should be ignored. Besides, you can also specify whether or not the annotated field should be ignored if its value is `null` or empty.

> An empty string, array, collection or zero is all considered as an empty value, including `null`.

Take a look at the class below:

``` Java
public class Bean {
    public static final String COPY_WITH_NAME = "copyWithName";
    public static final String COPY_WITHOUT_NAME = "copyWithoutName";

    @CopyIgnore(except = COPY_WITH_NAME)
    private int id;
    @CopyIgnore(when = COPY_WITHOUT_NAME, policy = IgnorePolicy.EMPTY)
    private String name;
}
```

It's obviously not diffcult to understand what those annotations do. The field `id` will always be ignored during copying except you want to `COPY_WITH_NAME`, and the field `name` will be ignored only when you want to `COPY_WITHOUT_NAME` or its value is empty.

Those two `String` constants serve as conditions, which can be specified by providing an array of them when copying. For example:

``` Java
BeanCopyUtils.copy(new Bean(), Bean.class, new String[] { Bean.COPY_WITH_NAME }); // Both id and name will be copied
BeanCopyUtils.copy(new Bean(), Bean.class, new String[] { Bean.COPY_WITHOUT_NAME }); // Both id and name will be ignored
```

You can also decide whether to ignore all the fields with `null` or empty values:

``` Java 
BeanCopyUtils.copy(new Bean(), Bean.class, IgnorePolicy.EMPTY);
```

However, the annotation has a higher priority so that you can make some exceptions on certain fields.

#### Colltctions

Generally, no additional configuration is required when handling target fields of type `Collection`.

If the type of a target field is an interface, like `List`, then the copied value will be of the same type with the source. If this is not what you want, you can just declare the field with an implementation type, like `ArrayList`. If you insist with an interface type, you can also use `@ToCollection` to specify one.

``` Java
public class Bean {
    @ToCollection(ArrayList.class)
    private List<Integer> numbers;
    private ArrayList<String> names; 
}
```

## Performance
The performance has not been tested yet, but it should be acceptable.

It is rather appreciated if you can offer some advice or even personally help improve it!