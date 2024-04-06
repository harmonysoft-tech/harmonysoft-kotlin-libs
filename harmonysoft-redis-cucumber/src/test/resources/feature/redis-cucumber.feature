Feature: Redis cucumber feature tests

  Scenario: Redis record verification

    Given we configure the following record in redis test-key=test-value

    Then redis should have record test-key=test-value

  Scenario: Redis value binding

    Given we configure the following record in redis test-key=test-value

    When redis value for key 'test-key' is stored under dynamic key 'value-marker'

    Then dynamic key 'value-marker' should have value 'test-value'

  Scenario: Arguments expansion

    Given dynamic key key-marker is bound to value 'test-key'

    And dynamic key value-marker is bound to value 'test-value'

    When we configure the following record in redis <bound:key-marker>=<bound:value-marker>

    Then redis should have record <bound:key-marker>=<bound:value-marker>

    And redis should have record test-key=test-value