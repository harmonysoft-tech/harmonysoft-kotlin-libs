Feature: Kafka cucumber feature tests

  Background:

    Given kafka topic 'test' is subscribed

  Scenario: Exact message match

    When the following kafka message is sent to topic 'test':
      """
      hello
      """

    Then the following message is received in kafka topic 'test':
      """
      hello
      """

  Scenario: Partial JSON message match

    When the following kafka message is sent to topic 'test':
      """
      {
        "k1": "v1",
        "k2": "v2"
      }
      """

    Then the following JSON message is received in kafka topic 'test':
      """
      {
        "k1": "v1",
        "k2": <bind:v2-key>
      }
      """

    And dynamic key 'v2-key' should have value 'v2'

  Scenario: Exact message mismatch

    When the following kafka message is sent to topic 'test':
      """
      one
      """

    Then the following message is not received in kafka topic 'test':
      """
      two
      """

  Scenario: Json message mismatch

    When the following kafka message is sent to topic 'test':
      """
      {
        "k1": "v1",
        "k2": "v2"
      }
      """

    Then the following JSON message is not received in kafka topic 'test':
      """
      {
        "k1": "v3"
      }
      """