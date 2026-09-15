package org.sonar.squidbridge.checks;

import org.sonar.squidbridge.api.CheckMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Minimal test verifier for squidbridge CheckMessage collections.
 * Replaces the original verifier which depended on removed SonarQube APIs.
 */
public class CheckMessagesVerifier {

  private final List<CheckMessage> messages;
  private int index;

  private CheckMessagesVerifier(Collection<CheckMessage> messages) {
    this.messages = new ArrayList<>(messages);
    this.index = 0;
  }

  public static CheckMessagesVerifier verify(Collection<CheckMessage> messages) {
    return new CheckMessagesVerifier(messages);
  }

  public CheckMessagesVerifier next() {
    assertThat(messages.size() > index)
      .as("Expected at least one more check message at index " + index)
      .isTrue();
    index++;
    return this;
  }

  public CheckMessagesVerifier atLine(int expectedLine) {
    assertThat(messages.size() >= index)
      .as("Expected message at line " + expectedLine)
      .isTrue();
    CheckMessage message = messages.get(index - 1);
    assertThat(message.getLine()).isEqualTo(expectedLine);
    return this;
  }

  public CheckMessagesVerifier withMessage(String expectedMessage) {
    assertThat(messages.size() >= index)
      .as("Expected message at index " + (index - 1))
      .isTrue();
    CheckMessage message = messages.get(index - 1);
    assertThat(message.getText(Locale.ENGLISH)).isEqualTo(expectedMessage);
    return this;
  }

  public void noMore() {
    assertThat(messages)
      .as("Expected no more check messages but had " + (messages.size() - index) + " extra(s)")
      .hasSize(index);
  }
}
