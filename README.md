# OpenTelemetry Spring Auto-Configuration Bug
This repository contains two sub-projects for testing B3 multi-header injection with OpenTelemetry Spring
Auto-Configuration. One project uses OpenTelemetry version 1.0.1 (which does not work), while the other project uses
OpenTelemetry version 0.16.0 (which works). To run the tests, use the provided shell script which automatically
downloads the required java agents:

```
./runTests.sh
```

The tests in the two projects are identical, apart from a few subtle API changes. Interestingly, we have observed that
no headers are injected in version 1.0.1, despite the `RestTemplateInterceptor` being correctly injected. This is the
case for all types of headers, not just B3 headers.
