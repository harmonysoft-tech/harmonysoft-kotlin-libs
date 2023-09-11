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

  Scenario: Nested mongo documents

    Given mongo test collection has the following document:
      | data.key     | data.subData.key    |
      | nested-value | nested-nested-value |

    Then mongo test collection should have the following document:
      | data.key     | data.subData.key    |
      | nested-value | nested-nested-value |