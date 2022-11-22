## v1.22.0 released on 22 Nov 2022 UTC
  * 9a9c2d4bdd8ebd4fbfa4b4a1d809b5fadb8d3033 * optional meta-mappers * call TestAware.onTestEnd() after test end
  * 43b19bc7048baf673cbe920ca348e055066b205f added jackson-datatype-jsr310 dependency for being able to work with java8 date/time classes
  * 361e2541ef17cb4852fe1241b806bc2c9f1c61fa inpertio 1.4.0
  * e20f3bceb75a6400023ea1dbb73be16bdcd92c35 removed 'plain' jar classifier
  * eddf9fdeb4119a835b4347a5a77e475916d00b47 event-bus-spring now depends on common-spring
  * b61e22234fa26977f6422e533ecd98ee9e683f4f extracted free port discovery to a dedicated utility
  * d22d107316a8b85c4b54e334b4863222d613e97e corrected a typo in bootstrap meta value mapper
  * 2e8b013146d67b1e353d44b32d6c73db5271a268 * added jackson as default json-api implementation * renamed the 'default' module
  * 588dbff439262b193ff1c104b31883aa5dc1730a updated README
  * c4b2f10ec4d3611c85ef1408209c979bd8721fba Update CNAME
  * 34e47c3488d28f5cc7a8aec430694e5a13f68da5 Update CNAME
  * ec186f6915a87c06c5fa05c77551539435d31d7d using unique configuration names in order to avoid clashing with host projects
  * cac54a5e718cea732ab987a981f95d69a6389cc4 instructing DI on HttpClientImpl constructor to use
  * df09cfa42533e072f11097f37f91f3ced3d506c6 * harmonysoft-default-dependencies - common single dependency which defines default sub-dependencies * jackson json parser is corrected
  * 70b11d30a8800a0c4a32d90f48f744d84233d6f9 corrected web port initialization
  * cb14452f3a2acd99822b87943b20a149cf63bae2 switched to recommended LocalPort annotation
  * ffda1479a806a9d2321d3ca39ce5a2679a11f8b3 spring boot plugin 2.7.2
  * 37f51b31d20926ca54f5d525f3b2cccc4319a3f3 introduced a constant for root library package
  * 3ef35506b787d49ac9f0771ac8b823ed33f4faa8 introduced a constant for library step definitions path
  * 5c6ed03e2a5b9ca7fb0f05cbdeffead0f21b8285 added cucumber-spring dependency
  * ...