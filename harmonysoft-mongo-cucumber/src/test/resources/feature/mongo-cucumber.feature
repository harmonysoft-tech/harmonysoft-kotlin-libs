Feature: Mongo cucumber feature tests

  Scenario: Binding document property on lookup

    Given mongo test collection has the following document:
      | key1   | key2   |
      | value1 | value2 |

    When mongo test collection should have the following document:
      | <bind:key1> | key2   |
      | some-key    | value2 |

    Then dynamic key 'some-key' should have value 'value1'

  Scenario: Int value in mongo document

    When mongo test collection has the following document:
      | key1      |
      | <int(10)> |

    Then mongo test collection should have the following document:
      | key1      |
      | <int(10)> |

  Scenario: Bind document property on insertion

    When mongo test collection has the following document:
      | <bind:_id> | key1   |
      | id1        | value1 |

    Then mongo test collection should have the following document:
      | _id         | key1   |
      | <bound:id1> | value1 |

  Scenario: Nested mongo documents with explicit tabular key-values

    When mongo test collection has the following document:
      | data.key     | data.subData.key    | data.nested.array[1] | data.nested.array[0] |
      | nested-value | nested-nested-value | array-value2         | array-value1         |

    Then mongo test collection should have the following document:
      | data.key     | data.subData.key    | data.nested.array[0] | data.nested.array[1] |
      | nested-value | nested-nested-value | array-value1         | array-value2         |

  Scenario: Nested mongo documents with JSON like setup

    Given dynamic key some-value is bound to value 'array-value2'

    When mongo test collection has the following JSON document:
      """
      {
        "_id": <bind:id>,
        "data": {
          "key": "nested-value",
          "subData": {
            "key": "nested-nested-value"
          },
          "nested": {
            "array": [ "array-value1", "<bound:some-value>" ]
          }
        }
      }
      """

    Then mongo test collection should have the following document:
      | _id        | data.key     | data.subData.key    | data.nested.array[0] | data.nested.array[1] |
      | <bound:id> | nested-value | nested-nested-value | array-value1         | array-value2         |

  Scenario: Verification for nested mongo documents with JSON like setup

    When mongo test collection has the following document:
      | data.key     | data.subData.key    | data.nested.array[1].subKey | data.nested.array[0] |
      | nested-value | nested-nested-value | array-value2                | array-value1         |

    Then mongo test collection should have a document with at least the following data:
      """
      {
        "data": {
          "key": "nested-value",
          "nested": {
            "array": [
              "array-value1",
              {
                "subKey": "array-value2"
              }
            ]
          }
        }
      }
      """

  Scenario: int values in mongo JSON setup

    When mongo test collection has the following JSON document:
      """
      {
        "key": 1
      }
      """

    Then mongo test collection should have a document with at least the following data:
      """
      {
        "key": 1
      }
      """

  Scenario: Comparing mongo document with null values

    When mongo test collection has the following JSON document:
      """
      {
        "_id": <bind:id>,
        "key1": 42,
        "key2": <null>
      }
      """

    Then mongo test collection should have a document with at least the following data:
      """
      {
        "_id": "<bound:id>",
        "key1": 42
      }
      """

    Scenario: Expecting a document in empty collection

      When next test verification is expected to fail

      Then mongo test collection should have a document with at least the following data:
         """
         {
           "key": "value"
         }
         """