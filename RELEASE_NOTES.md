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