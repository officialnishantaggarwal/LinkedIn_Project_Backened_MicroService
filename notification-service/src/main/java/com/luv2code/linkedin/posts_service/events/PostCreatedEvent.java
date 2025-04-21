package com.luv2code.linkedin.posts_service.events;

import lombok.Builder;
import lombok.Data;

@Data
public class PostCreatedEvent {

    Long creatorId;
    String content;
    Long postId;
}
