## v1.101.0 released on 25 Jan 2024 UTC
  * 4aafc53d0ebab7cdef6bc4532fa9f4289372c0fa switch from junit/cucumber hooks into TestAware
  * e69d07c1a591b0925f9e88ae717944b17e77dcf1 corrected null JSON values comparison in HTTP calls
## v1.100.0 released on 20 Jan 2024 UTC
  * a668ccaf8022001e3c087e89af23730fa16f8504 corrected null JSON values comparison in HTTP calls
  * a87acdf44ee44e84100bbcb7facc0697c638b4ab more HTTP tests
  * 29ef67e929da37733fd62a27bfebcac1ae924f7b more HTTP tests
## v1.99.0 released on 17 Jan 2024 UTC
  * dd34b66c35c6aeb2a20e85ced6c47735271db932 added ability to verify that target mongo collection doesn't have a document with specific data
## v1.98.0 released on 17 Jan 2024 UTC
  * c4114fec3826c1c49ceef7a684ba76c57fada3cd corrected cucumber texting
## v1.97.0 released on 10 Jan 2024 UTC
  * 98909d45333a7efd8299f362f5965c50413454d5 more step definitions
## v1.96.0 released on 09 Jan 2024 UTC
  * 10d1812e2b353f6ca255693840157cec1154bea3 added ability to match double value with decimal128 values in mongo documents
  * d2e76b767fca895427fb4ba60d0dd99b113a5740 fix tests
## v1.95.0 released on 08 Jan 2024 UTC
  * 87e6bcf0471004da5f0c601d8eda81f48610b79b corrected DI setup
## v1.94.0 released on 08 Jan 2024 UTC
  * f8200698c120d5393ea3321da5275c228be82218 * added ability to manager kafka headers for test kafka messages * improved logging for test kafka processing
  * c27d5ae6b7c68773ed6f2781e85dd55cceffefa3 * expose active test name * clean recorded calls to http mock after test
  * 107d4b9e296803ca663a70172149864c117ecda6 * correct mongo verification in case of empty collection * extracted common functionality from CommonStepDefinitions into CommonTestManager
## v1.93.0 released on 28 Dec 2023 UTC
  * 0a766241c99d7c7ef372e2c94802b2ef5aabe34a added ability to verify that an http call to the mock server is made with a json body which doesn't have specific data
  * 84e867af624080b3d04052f28d5d0b212b69c12c use proper context for kafka fixture
## v1.92.0 released on 23 Oct 2023 UTC
  * 4ad55c5a778d3154d8904be7b94bbea4957a5297 make sure that mock http server is started before the first use
## v1.91.0 released on 26 Sep 2023 UTC
  * 717565767ee40abbaad52c1c3219e9b9182fa08c switched HTTP calls number verification to ttl mode
  * 39804917ca73e930f87837e517e0c6eac52646a0 corrected failed expectation error description
## v1.90.0 released on 20 Sep 2023 UTC
  * e0a49cc86d15f75f4950ec3fe0f3e12775bb4ec3 added ability to verify exact and minimum number of HTTP calls in test
## v1.89.0 released on 20 Sep 2023 UTC
  * c06a56e83cb62813bd29218fa20a19426b5a069f added ability to delay test HTTP responses
## v1.88.0 released on 19 Sep 2023 UTC
  * 377aa46492cefcf012b9110889d74b7def52eac2 support <null> meta-value in mongo tests
## v1.87.0 released on 18 Sep 2023 UTC
  * 78345737d1864b50026261ace4424bc693f41e18 support int values in json mongo test setup
## v1.86.0 released on 15 Sep 2023 UTC
  * 658c6975c9c18528c1166ecaaf7ea7dc3460b0bc auto expanding dynamic/meta values in HTTP client JSON body
  * a3db9bfd76646bae33cd29a2130d14d2ab2075ef added ability to create and verify mongo documents using json format
## v1.85.0 released on 11 Sep 2023 UTC
  * cc58ef5faabeec82b696303d7639348834966318 support timed verification that target HTTP call is happened
  * 69d0eb954caf34cb5c71ee392cb37292cbe9d405 support nested documents in mongo verifications
## v1.84.0 released on 08 Sep 2023 UTC
  * 6ff29deb1a5d8899b7b29137df070559caa456a8 added ability to constraint HTTP response by particular count
## v1.83.0 released on 07 Sep 2023 UTC
  * 77cbe3ddff2491e277af15d4218b0ec49e76c96b added ability to configure http response headers in tests
## v1.82.0 released on 04 Sep 2023 UTC
  * e6ea2b9f72bccffd9f5c2d73ae7e62db2b5d85d1 introduced 'http-test' module
## v1.81.0 released on 04 Sep 2023 UTC
  * c80b469a36d2f2a21f8383bbf64455f28e193cbc split mock http server into test and cucumber parts
  * 5845aa190baec214d2a6146301e8a97fd16183ca split mock http server into test and cucumber parts
## v1.80.0 released on 24 Aug 2023 UTC
  * 35e378a05ef9b17a24cb534fa4d11bddb7da9122 split mock http server into test and cucumber parts
## v1.79.0 released on 24 Aug 2023 UTC
  * 23e73bbc12bc7ead1e17cb3e154930458da06fb3 extracted utility function to wait for target condition to happen
## v1.78.0 released on 22 Aug 2023 UTC
  * 4fdd52575b02477a1db11976c03abde970472131 moved json and yaml ObjectMappers from spring context into a wrapper class
## v1.77.0 released on 21 Aug 2023 UTC
  * 1a344e2f48d85bef14b0dbed116dc4ce42ec76f6 add yaml jackson ObjectMapper
## v1.76.0 released on 31 Jul 2023 UTC
  * 59425dc5b9bc8403067120071f26d58cd7fd89b4 allow to bind mongo property name on document insertion
## v1.75.0 released on 10 Jul 2023 UTC
  * c73eef324fc458d2987ff731c5d4f7676d9c6ca0 use bigger test environment startup tll
## v1.74.0 released on 28 Jun 2023 UTC
  * 9e646dab6a772991d6d65137431fa4ebfc9b9b54 better logging
## v1.73.0 released on 26 Jun 2023 UTC
  * ce06802cb4cd3fc7fe9f5ec462df1398cad98095 minor improvements into test framework
## v1.72.0 released on 22 Jun 2023 UTC
  * 1f24ee7d79a5e4bf6d6bc06da4d4944c4e46c335 share TestKafkaConfigProviderImpl for everybody
## v1.71.0 released on 19 Jun 2023 UTC
  * 3155a868c2b8ad903343617b65af4ae234ab68a9 Added Sequence.mapFirstNotNull()
## v1.70.0 released on 19 Jun 2023 UTC
  * e44343a753adbc1538c5a1d8bf6f02262235c28e calling TestEnvironmentManagerMixin.afterStart() callback
## v1.69.0 released on 18 Jun 2023 UTC
  * c082b888c741e3254c8058d54a5f93932c1707d2 more test environment extension points
## v1.68.0 released on 16 Jun 2023 UTC
  * 0fdf8067417162a240152bfd3c9a2987a865755b move more common mongo stuff into common mongo-environment module
## v1.67.0 released on 16 Jun 2023 UTC
  * 0497708128dd3073d74d87f5d6e79eafdfcbf96e removed duplicate constant
## v1.66.0 released on 16 Jun 2023 UTC
  * 524d787acdbdbd1bed47711da52f83fd3318c064 expose test context
  * 3fefb0e23e99c43252bc60aaffdcf27bd8e00f21 manual version bump
## v1.65.0 released on 16 Jun 2023 UTC
  * 223c4dffaf858116d25ed7568007eef16df09123 adapt testcontainers environments to the changed api
## v1.64.0 released on 16 Jun 2023 UTC
  * 383ea45662a591d060b34d51e1f66a95d14361e2 add utility TestContext.prepareDirectory() method
## v1.63.0 released on 16 Jun 2023 UTC
  * 4802fe0fe97d9098e91c709a60fc8b4889fbec03 expose test context to kafka and mongo starters
## v1.62.0 released on 16 Jun 2023 UTC
  * 66cd592fac317e2a1f285c531dbe2fa5ffe4a6d5 expose test context to test environment mixin
## v1.61.0 released on 16 Jun 2023 UTC
  * 56c1959fa85af9d13280e1acfc7fd16cde95fb71 added test environment extension point
  * 4864dc7e56c3b3015371d621b76c239b11108641 don't publish internal modules to maven central
## v1.60.0 released on 15 Jun 2023 UTC
  * 2e6807e28896ebfbdc3e2d14f9239ce3410e5fe9 extract common mongo environment setup into common module
  * 1d48fed637a7adabb7b528e93be41b5488d53339 rename refactoring
  * 1fc2bc146acdc4f00a09aa0ecff0e035bf40a3db corrected mongo user creation in test environment
## v1.59.0 released on 09 Jun 2023 UTC
  * bb00fbcbf95b5f78f1b88c8ee33d6a284bff99fc fix tests
## v1.58.0 released on 09 Jun 2023 UTC
  * 067ad0572351c43bb297bd780e58d0ed70dd0feb 1. Test environment infrastructure 2. Testcontainers-based kafka and mongo environment 3. Kafka cucumber steps are expanded
## v1.57.0 released on 26 May 2023 UTC
  * 7c72cced2ab81c9aafccf2d73ff681dfcbc96a4f move common functionality form kafka-cucumber to kafka-test
## v1.56.0 released on 24 May 2023 UTC
  * dd83785d8506ba6c612f66e83267ce245bc5e387 correct projection setup during mongo documents existence verification
## v1.55.0 released on 24 May 2023 UTC
  * 858567a23d27b2fe339cee5ca78ad8b2061f4b2e move common mongo test functionality from mongo-cucumber into mongo-test
## v1.54.0 released on 15 May 2023 UTC
  * 0f09138027e15ea26235602fcc0dd67ecb96c1e4 Added ability to verify that no HTTP request to target path and HTTP method is made
## v1.53.0 released on 12 May 2023 UTC
  * fbb48929d5500ba4241ed741214a58eddb4cd456 toString() for PartialJsonMatchCondition
## v1.52.0 released on 12 May 2023 UTC
  * 913817f743081a3daacfff1977c73716cba6d7f3 correct binding regex
## v1.51.0 released on 10 May 2023 UTC
  * 7ac87a9819d7d45a9dc57ebb600cb540e1e7170a 1. Don't store dynamic bindings for JSON mismatches 2. Allow to constraint mock HTTP server request by partial JSON body match
## v1.50.0 released on 12 Apr 2023 UTC
  * bb5d038d22c472aa2dba7818ff7975e1603326de exposed mongo client
## v1.49.0 released on 23 Feb 2023 UTC
  * 06008bb648c75421cbb50f958973fe5d8c00c503 corrected mongo test
## v1.48.0 released on 23 Feb 2023 UTC
  * acdf78844bb48fa727be31655d4aaf0931ec531a meta values and meta functions can produce values of any type now
## v1.47.0 released on 23 Feb 2023 UTC
  * 313ca01640888cae424551cb265b6f5df300bd69 added mongo cucumber tests setup
  * 94ace14b80d7e341d8c3b074ebb8003c13c187ec enriching expected JSON data in HTTP client response cucumber step
## v1.46.0 released on 21 Feb 2023 UTC
  * 644ffbea1bbb3faf39f34f0f5cc68524f17c15e2 provide fine-grained mismatches in mock HTTP call expectations
## v1.45.0 released on 20 Feb 2023 UTC
  * 7ba20290e9f5203b73369cb07e1d277158fa669d corrected error indentation in lists verification failure
## v1.44.0 released on 20 Feb 2023 UTC
  * 747ec6282528f7181b687f9f915455855b9252e1 exposing HTTP responses in HttpClientStepDefinitions
## v1.43.0 released on 17 Feb 2023 UTC
  * caa945b18c3c083e54706edea29dda9010a2dfc6 added ability to verify that HTTP JSON response doesn't have particular data
  * 06516c37860d76164f910bdb15d5e2f7ba8131f2 do clean build on release
## v1.42.0 released on 15 Feb 2023 UTC
  * 0d5a235269b773fd090511c8f8f1621252c61312 added cucumber step for verifying that mock HTTP server was called with particular JSON
  * ed29e36b88725ca640c108ff365d1630123b23b2 added open-source acknowledgements into README
  * b1920ac7bfe6f999b3bff1bc00f7fe0cca503a18 added open-source acknowledgements into README
  * 8ea3c3b203f7cab886be31521801a81ac27e0a97 added open-source acknowledgements into README
  * 7ab436c099fc3c74116f798ecf3942a054782c87 added open-source acknowledgements into README
  * 20edd71af9c9416a0f9f39270f06ac215fa02997 added open-source acknowledgements into README
  * 88f05c864591f352691f1d623cbbd2e27dceaa6b added open-source acknowledgements into README
  * 3fb94c3c470c08d745338ceb2692c52b26c55ae2 added cucumber tests to verify exposed step definitions; repackaging
## v1.41.0 released on 06 Jan 2023 UTC
  * 20c7da0ea0f309ed6e608641a4a2c39054f15ded corrected mock HTTP response providers overwriting logic; added cucumber tests for mock HTTP
  * 6216bc6c12f28d1c9bd5f9d2e62b0771b4ac751a minor refactoring
  * 6156ff49b9b615da544a13bc706b3fd51c7d7cf8 HttpStepDefinitions -> HttpClientStepDefinitions
## v1.40.0 released on 05 Jan 2023 UTC
  * 83968f030b357062c21e5823247b4437d5045b1a allow replacing conditional mock HTTP response providers
## v1.39.0 released on 23 Dec 2022 UTC
  * 98d44c935ad91d5fe054c8fee44292a20c92d715 reporting all mismatches from JSON maps comparison in tests
## v1.38.0 released on 23 Dec 2022 UTC
  * 9beddc7f92d315987a2807217b76ce6668df1f99 allow replacing mock HTTP response providers
## v1.37.0 released on 22 Dec 2022 UTC
  * b2b57634a8a80600ea4fe4f64f8343f40639ce24 avoid IndexOutOfBoundsException during JSON arrays comparison in test
## v1.36.0 released on 22 Dec 2022 UTC
  * 484d731882bedf8cfbc228d5a3df9b48d957d202 JSON test comparison doesn't fail eagerly now but returns all problems instead
## v1.35.0 released on 21 Dec 2022 UTC
  * 7822a14403e08c53c579ed493bfbf5a141746f1f printing complete response in case of JSON response mismatch in tests
## v1.34.0 released on 16 Dec 2022 UTC
  * 7d8bad7991281fa2938f649d22c5b47dba38245f added ability to bind mongo documents to dynamic keys
  * cd9d7173959896d5586a07ecccf558e528665a90 added ability to provide custom response code in mock http call
  * 0d0cceff155fa3b5ade65a18949c7ede4df0ee80 added ability to find target element among given candidates
  * 1dcf219f992d6eeac4ae9474fc6902ead6a19585 cucumber input parser now expands meta values
  * 0b200205c725f8a3dccd192adf66fa57fd1fde1d kafka cucumber support
  * c9e68287c0fd74a2e7088f5a296392219785ff89 added ability to match json response without failing
## v1.33.0 released on 15 Dec 2022 UTC
  * ba721add621e6fc19d017419ac092608aea9f4bd VerificationUtil.verifyContains() now returns found candidate
  * 7402c2b2cdd41799a009e8c35c524e527b326cdb gradle-release-paperwork plugin is upgraded to 1.7
  * 861340950f4aa34f741117595448bcb96e350860 release 1.32.0
## v1.32.0 released on 30 Nov 2022 UTC
  * 30534e960c8c37e63bcbef32f4dc1edf37787ced corrected JSON comparison logic for arrays
## v1.31.0 released on 28 Nov 2022 UTC
  * a78d6d3ea7af9541ce147d6041808ca6281e0aa3 quote expected/actual values in case of JSON comparison failure
## v1.30.0 released on 28 Nov 2022 UTC
  * d499f18b67ecb6c3762319b59ab05eb4cfbab55c allow to verify that JSON doesn't have data at target path
## v1.29.0 released on 25 Nov 2022 UTC
  * 9b92f456728158b3b01761d6d6b8ad3daedc775d make sure that mock http server is started only once
## v1.28.0 released on 25 Nov 2022 UTC
  * 506379c9143886e432b3a03fe612ac578bc76694 added mongo-cucumber module
## v1.27.0 released on 24 Nov 2022 UTC
  * 3a73cc86b71e7f2c988584e9c624a836af595fb2 added mock HTTP server test module
  * 68852460df0ca93c9e9f4c7070f283b3f06cc4f4 gradle release paperwork plugin 1.5.0
## v1.26.0 released on 24 Nov 2022 UTC
  * 77ef53819460ed493f1c392f26dbd3e5b4d195d3 provided ability to send multipart HTTP requests in tests with any number of parts
## v1.25.0 released on 23 Nov 2022 UTC
  * a2eca17557770f7b9fc1e5b1887b654f56516311 added ability to specify common HTTP headers in tests
## v1.24.0 released on 22 Nov 2022 UTC
  * 1e301bd63faf7b28f4faf8721c09430857ddafcc corrected file upload step
## v1.23.0 released on 22 Nov 2022 UTC
  * 4d6fc2f6131c366e4509b32c55567900ecc529b1 configured release paperwork plugin
  * cd7f8091b0e66d51c448ff6edfdc010d3767c5fe typos bug fixing
  * 61041a6784ffcba66e7fcb577621fced8fec4e73 allow non-strict JSON comparison in tests
## v1.22.0 released on 22 Nov 2022 UTC
  * 9a9c2d4bdd8ebd4fbfa4b4a1d809b5fadb8d3033 start tracking release notes