package com.fasttasker.fast_tasker.domain.conversation;

import com.fasttasker.common.exception.DomainException;
import jakarta.persistence.Embeddable;
import org.springframework.util.StringUtils;
import lombok.*;

/**
  Content of the message
 */
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode
@ToString
public class MessageContent {

    /**
     * text of the message
     */
    private String text;

    /**
     * attachment url in the message, it can be an image url o video url
     */
    private String attachmentUrl;

    public MessageContent(String text, String attachmentUrl) {
        boolean hasText = StringUtils.hasText(text);
        boolean hasAttachment = StringUtils.hasText(attachmentUrl);

        if (!hasText && !hasAttachment) {
            throw new DomainException("Message content must have either text or an attachment URL.");
        }
        this.text = text;
        this.attachmentUrl = attachmentUrl;
    }

    /**
     * calculate the snippet of the message
     * @return snippet of the message
     */
    public String snippet() {
        if (text != null) {
            return text;
        }
        else if (attachmentUrl != null) {
            return "Foto";
        }
        return "";
    }
}