Lego
=======

Lego is a library to build any entity (web/api response) in a scatter-gather fashion

## Releases

| Release       | Date            | Description                             |
|:--------------|:----------------|:----------------------------------------|
| Version 3.1.0 | Nov 24 2016     | - DataSource returns a generic type extending from DataType
| Version 3.0.0 | Nov 24 2016     | - Removed Identifiable, Describable     |
| Version 2.0.0 | Apr 19 2016     | - DataType is no longer Identifiable
|               |                 | - Attributes.getAttribute() returns a generic type instead of Object
| Version 1.0.0 | Jan 04 2016     | - First public release                  |

## Changelog

Changelog can be viewed in [CHANGELOG.md](https://github.com/flipkart-incubator/Lego/blob/master/CHANGELOG.md)

##Maven Artifact

Add the following repository to your pom.xml

```xml
    <repository>
      <id>clojars</id>
      <name>Clojars repository</name>
      <url>https://clojars.org/repo</url>
    </repository>
```

And add the following dependency to start using Lego in your maven project.

```xml
    <dependency>
      <groupId>com.flipkart.lego</groupId>
      <artifactId>lego</artifactId>
      <version>2.0.0</version>
    </dependency>
```

## Users

[Poseidon](https://github.com/flipkart-incubator/Poseidon)

## Getting help
For discussion, help regarding usage raise issues 

## Contribution, Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/flipkart-incubator/Lego/issues).
Please follow the [contribution guidelines](https://github.com/flipkart-incubator/Lego/blob/master/CONTRIBUTING.md) when submitting pull requests.

## License

Copyright 2016 Flipkart Internet, pvt ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
