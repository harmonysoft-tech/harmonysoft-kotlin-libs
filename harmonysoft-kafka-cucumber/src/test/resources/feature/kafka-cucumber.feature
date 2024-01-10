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

    Then a JSON message with at least the following data is received in kafka topic 'test':
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

    Then a JSON message with at least the following data is not received in kafka topic 'test':
      """
      {
        "k1": "v3"
      }
      """

  Scenario: Header value match

    Given header k1=v1 is used for sending all subsequent kafka messages

    When the following kafka message is sent to topic 'test':
      """
      test
      """

    Then a message with header k1=v1 is received in kafka topic 'test'