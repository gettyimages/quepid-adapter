# Quepid Proxy

This is an implementation of a proxy service that allows [Quepid](https://github.com/o19s/quepid) to communicate with Solr instances through middleware search API services.

This project allows you to perform human judgments of search results when your backend instance of Solr is accessed through an API that abstracts and controls the search queries and results. The proxy works by emulating the Solr search API in front of the middleware search service. This makes Quepid think it is talking directly to Solr and enables the use of Quepid.

## Running the Proxy

```shell
git clone https://github.com/gettyimages/quepid-proxy.git
cd quepid-proxy
mvn clean install
mvn spring-boot:run
```

After compiling you can also just run the jar file:

```shell
java -jar ./quepid-proxy-service/target/quepid-proxy.jar
```

This will start the proxy on port `8080` (unless the port has been changed in the `application.properties`).

You can now add a new case to Quepid by using `http://localhost:8080/search` as the Solr endpoint. The following Quepid operations are supported:

* Search
* Snapshot comparison
* Getting individual document details

## Custom Implementations

Because all middleware search services are propietary and different, a custom implementation is required for each one. Look at the `quepid-proxy-uspto` project for an example implementation that uses the USPTO search API.

To make this project work with your middleware API service, simply copy the `quepid-proxy-uspto` project, modify the API definitions, and modify how the middleware search responses get converted to Solr documents. You will likely need to do this for two functions - one to perform the search and another get a single document by some unique identifier.

## Issues and Improvements

Contributions are welcome. Please open a [GitHub issue](https://github.com/gettyimages/quepid-proxy/issues).

# License

MIT License
Copyright (c) 2021 Getty Images

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.