# BeanCopyUtils
[English](https://github.com/KeroZhai/beancopy/blob/master/README.md) | [简体中文](https://github.com/KeroZhai/beancopy/blob/master/README_zh_CN.md)

一个简单但有用的 Bean 拷贝工具。

## 特性

* **深拷贝**.
* 支持按条件拷贝指定的字段。
* 支持不同名或不同类型字段间的拷贝。
* 支持拷贝 Bean 的数组或者集合（Collection）。

## 使用

### 基础

例如有两个类，一个作为源，另一个作为目标。

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

想要拷贝一个源对象到目标对象，只需要使用：

``` Java
Target target = BeanCopyUtils.copy(new Source(), Target.class);
```

或是,

``` Java
Target target = new Target();
BeanCopyUtils.copy(new Source(), target);
```

如你所见，getters/setters 并不是必需的，然而，一个合法的 JavaBean 应该具备它们。

### 进阶

#### 字段重映射

默认情况下，两个 JavaBean 字段间是通过字段名来进行对应的。对于那些名称不同的字段，可以使用 `@AliasFor` 注解来指明被注解的字段是另一个 JavaBean 中某个字段的*别名*，如下所示：

``` Java
public class Source {
    private long id;
}

public class Target {
    @AliasFor("id")
    private long userId;
}
```

#### 转换器

通过 `@Converter`注解来提供一个自定义的转换器类，自定义的转换器类必须实现 `Converter` 接口。例如：

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

#### 条件拷贝

通过 `@CopyIgnore` 来指明当（或除了）某种条件满足时，被注解的字段应该在拷贝时被忽略。此外，你也可以指定是否要忽略值为 `null` 或空的字段。

> 空字符串、数组、集合或 `0` 都被认为是一个空值，**包括 `null`**。

来看看下面的类：

``` Java
public class Bean {
    public static interface IncludingId {}
    public static interface ExcludingName {}

    @CopyIgnore(except = IncludingId.class)
    private int id;
    @CopyIgnore(when = ExcludingName.class, policy = IgnorePolicy.EMPTY)
    private String name;
}
```

很明显，不难去理解上面这些注解做了些什么。字段 `id` 总会在拷贝时被忽略，除非当你想要包含它（`IncludingId`）, 而字段 `name` 只有当你想要排除它（`ExcludingName`） **或者**它的值为空的时候才会被忽略。

如你所见，条件表示为类字面量，可在拷贝的时候以数组的形式指定。例如：

``` Java
// `id` 和 `name` 都会被拷贝
BeanCopyUtils.copy(new Bean(), Bean.class, new String[] { Bean.IncludingId.class });
// `id` 和 `name` 都会被忽略
BeanCopyUtils.copy(new Bean(), Bean.class, new String[] { Bean.ExcludingName.class });
```

你也可以决定是否要忽略所有值为 `null` 或空的字段：

``` Java
BeanCopyUtils.copy(new Bean(), Bean.class, IgnorePolicy.EMPTY);
```

然而，注解上的配置有更高的优先级，因此你可以针对某个字段进行一些额外配置。

#### 集合

一般来说，对于 `Collection` 类型的目标字段，并不需要额外的配置。

如果目标字段的类型是接口，比如 `List`，那么被拷贝的值会和源字段类型相同。如果这不是你想要的，你可以直接将目标字段声明为一个实现类， 比如 `ArrayList`。鉴于使用接口类是一个好的做法，你可以通过 `@ToCollection` 来指定一个实现类。

``` Java
public class Bean {
    @ToCollection(ArrayList.class)
    private List<Integer> numbers;
    private ArrayList<String> names;
}
```

## 性能

性能其实并没有怎么测试，但应该能接受。

希望您能对此提供一些建议，甚至是亲自帮助改进！欢迎 PR。

## License

[MIT](https://opensource.org/licenses/MIT)
