Feature: HTTP client tests

  Scenario: Partial JSON match for array element

    Given the following HTTP request is received by mock server:
      | method | path  |
      | GET    | /test |

    And the following mock HTTP response is returned:
      """
      {
        "data": [
          { "id": "id1", "data": "data1" },
          { "id": "id2", "data": "data2" }
        ]
      }
      """

    When HTTP GET request to /test is made

    Then last HTTP GET request returns JSON with at least the following data:
      """
      {
        "data": [
          { "id": "id2" }
        ]
      }
      """

  Scenario: Negative JSON response match

    Given the following HTTP request is received by mock server:
      | method | path  |
      | GET    | /test |

    And the following mock HTTP response is returned:
      """
      {
        "data": [
          { "id": "id1", "data": "data1" },
          { "id": "id2", "data": "data2" }
        ]
      }
      """

    When HTTP GET request to /test is made

    Then last HTTP GET request returns JSON which does not have the following data:
      """
      {
        "data": [
          { "id": "id3" }
        ]
      }
      """

  Scenario: HTTP response dynamic data expansion

    Given the following HTTP request is received by mock server:
      | method | path  |
      | GET    | /test |

    And the following mock HTTP response is returned:
      """
      {
        "key1": "value1"
      }
      """

    And dynamic key value is bound to value 'value1'

    When HTTP GET request to /test is made

    Then last HTTP GET request returns JSON with at least the following data:
      """
      {
        "key1": "<bound:value>"
      }
      """

  Scenario: Dynamic values in HTTP request body

    Given dynamic key some-key is bound to value 'some-value'

    When HTTP POST request to /test is made with JSON body:
      """
      {
        "data": "<bound:some-key>"
      }
      """

    Then the following HTTP POST request for path /test with at least this JSON data is received:
      """
      {
        "data": "some-value"
      }
      """

  Scenario: Passed regexp match in HTTP response

    Given the following HTTP request is received by mock server:
      | method | path  |
      | GET    | /test |

    And the following mock HTTP response is returned:
      """
      {
        "data": "part1 - \"part2\" - part3"
      }
      """

    When HTTP GET request to /test is made

    Then last HTTP GET request returns JSON with at least the following data:
      """
      {
        "data": <regexp:.*"part2".*>
      }
      """

  Scenario: Failed regexp match in HTTP response

    Given the following HTTP request is received by mock server:
      | method | path  |
      | GET    | /test |

    And the following mock HTTP response is returned:
      """
      {
        "data": "part1 - \"part2\" - part3"
      }
      """

    When HTTP GET request to /test is made

    Then next test verification is expected to fail

    And last HTTP GET request returns JSON with at least the following data:
      """
      {
        "data": <regexp:.*"partX".*>
      }
      """
