Feature: Mongo cucumber glue tests

  Scenario: Binding document property

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