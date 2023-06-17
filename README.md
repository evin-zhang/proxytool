# proxytool
# Proxy Controller

This tool can be used as a mock server. When you don't want to manually configure a large amount of API mock data, you can use this proxy tool to cache the real request results and use them as mock data.
## Table of Contents
- [Dependencies](#dependencies)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)

## Dependencies
This controller requires the following dependencies:
- [Spring Framework](https://spring.io/) - The core framework for building Java applications.
- [Lombok](https://projectlombok.org/) - A library that simplifies the development process by automatically generating boilerplate code.
- [Reactor](https://projectreactor.io/) - A reactive programming library for building non-blocking applications.
- [JDbr Proxy Tool](https://www.jdbr.com/proxytool) - The main proxy tool library.

Make sure to include these dependencies in your project.

## Usage
1. Create an instance of `ProxyController` by injecting an instance of `ProxyGatewayService` into its constructor.
2. Configure the base URL path for the proxy tool by annotating the class with `@RequestMapping("/proxy")`.
3. Define the API endpoints by annotating the `proxyRequest` method with `@RequestMapping(value = "/{target-system-name}/**", method = {RequestMethod.GET, RequestMethod.POST})`.
4. Customize the method parameters to suit your needs. The provided parameters are:
    - `requestBody` (optional): The request body as a string.
    - `params`: Multi-value map of request parameters.
    - `targetSystemName`: The name of the target system.
    - `httpMethod`: The HTTP method of the original request.
    - `exchange`: The `ServerWebExchange` object representing the current exchange.
    - `requestHeaders`: The HTTP headers of the original request.
    - `cacheHeaderValue`: The cache header value (default value: "Y").
5. Within the `proxyRequest` method, extract the necessary information from the request and pass it to the `proxyGatewayService.proxyRequest` method.
6. The `proxyRequest` method returns a `Mono` of `ResponseEntity<String>`, representing the proxied response. Handle the response as needed.

## API Endpoints
### Proxy Request
- URL: `/{target-system-name}/**`
- Method: `GET`, `POST`
- Request Body: Optional
- Parameters:
    - `params`: Multi-value map of request parameters.
    - `target-system-name` (path variable): The name of the target system.
- Headers:
    - `requestHeaders`: The HTTP headers of the original request.
    - `x-proxy-tool-cache` (header): Cache header value (default value: "Y")
- Description: Proxies the request to the target system and returns the response as a `Mono` of `ResponseEntity<String>`.

**Note:** Make sure to replace `{target-system-name}` with the actual name of your target system.

That's it! You can now use this proxy controller to handle requests and proxy them to the specified target system.
