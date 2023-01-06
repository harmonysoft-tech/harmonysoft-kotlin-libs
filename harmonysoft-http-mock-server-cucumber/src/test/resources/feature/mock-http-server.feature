Feature: Mock HTTP server tests

  Scenario: Overwriting parameterless HTTP stub

    Given the following HTTP request is received by mock server:
      | method | path  |
      | GET    | /test |

    And the following mock HTTP response is returned:
      """
      first
      """

    And the following HTTP request is received by mock server:
      | method | path  |
      | GET    | /test |

    And the following mock HTTP response is returned:
      """
      second
      """

    When HTTP GET request to /test is made

    Then last HTTP GET request returns the following:
      """
      second
      """

  Scenario: Overwriting conditional HTTP stub

    Given the following HTTP request is received by mock server:
      | method | path  |
      | POST   | /test |

    And mock HTTP request body is a JSON with the following values:
      | query                |
      | <has-text:condition> |

    And the following mock HTTP response is returned:
      """
      first
      """

    And the following HTTP request is received by mock server:
      | method | path  |
      | POST   | /test |

    And mock HTTP request body is a JSON with the following values:
      | query                |
      | <has-text:condition> |

    And the following mock HTTP response is returned:
      """
      second
      """

    When HTTP POST request to /test is made with JSON body:
      """
      {
        "query": "condition"
      }
      """

    Then last HTTP POST request returns the following:
      """
      second
      """

  Scenario: Plain and conditional HTTP stubs to the same path

    Given the following HTTP request is received by mock server:
      | method | path  |
      | GET    | /test |

    And the following mock HTTP response is returned:
      """
      first
      """

    And the following HTTP request is received by mock server:
      | method | path  |
      | GET    | /test |

    And mock HTTP request has the following query parameter:
      | p1 |
      | v1 |

    And the following mock HTTP response is returned:
      """
      second
      """

    When HTTP GET request to /test is made

    Then last HTTP GET request returns the following:
      """
      first
      """

    And HTTP GET request to /test?p1=v2 is made

    And last HTTP GET request returns the following:
      """
      first
      """

    And HTTP GET request to /test?p1=v1 is made

    And last HTTP GET request returns the following:
      """
      second
      """

  Scenario: Non-overwriting conditional HTTP stubs

    Given the following HTTP request is received by mock server:
      | method | path  |
      | GET    | /test |

    And mock HTTP request has the following query parameter:
      | p1 |
      | v1 |

    And the following mock HTTP response is returned:
      """
      first
      """

    And the following HTTP request is received by mock server:
      | method | path  |
      | GET    | /test |

    And mock HTTP request has the following query parameter:
      | p1 |
      | v1 |

    And mock HTTP request has the following query parameter:
      | p2 |
      | v2 |

    And the following mock HTTP response is returned:
      """
      second
      """

    When HTTP GET request to /test?p1=v1 is made

    Then last HTTP GET request returns the following:
      """
      first
      """

    And HTTP GET request to /test?p1=v1&p2=v2 is made

    And last HTTP GET request returns the following:
      """
      second
      """