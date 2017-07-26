## Java Visitor Generators

Generates typed visitors from a heirachy as an annotation processor.


## Setup



```

repositories {
  jcenter()
  maven {
    url "http://dl.bintray.com/zourzouvillys/maven"
  }
}

dependencies {

  apt 'io.zrz:visitors:0.1'
  compileOnly 'io.zrz:visitors:0.1'

}

```



## Usage



```

@Visitable.Base({ Function.class, BiFunction.class, Consumer.class })
interface BaseType {

}


@Visitable.Type
class SomeClass implements BaseType {

}

@Visitable.Type
class OtherClass implements BaseType {

}


```

For more advanced usage:


```

@Visitable.Base(
  visitors = {
    @Visitable.Visitor(value = BiFunction.class, bindType = Function.class, className = "RandomVisitor", packageName = "my.package.name")
  }
)
interface BaseType {

}


// on the implementations

@Visitable.Type("NameForType")
class MyClass implements BaseType {

}




```


