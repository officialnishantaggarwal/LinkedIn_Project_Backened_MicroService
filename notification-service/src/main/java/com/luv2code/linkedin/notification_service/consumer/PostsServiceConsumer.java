package com.luv2code.linkedin.notification_service.consumer;

import com.luv2code.linkedin.notification_service.clients.ConnectionsClient;
import com.luv2code.linkedin.notification_service.dto.PersonDto;
import com.luv2code.linkedin.notification_service.service.SendNotification;
import com.luv2code.linkedin.posts_service.events.PostCreatedEvent;
import com.luv2code.linkedin.posts_service.events.PostLikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostsServiceConsumer {

    private final ConnectionsClient connectionsClient;
    private final SendNotification sendNotification;

    @KafkaListener(topics = "post-created-topic")
    public void handlePostCreated(PostCreatedEvent postCreatedEvent){
        log.info("Sending notifications: handlePostCreated: {}",postCreatedEvent);
        List<PersonDto> connections = connectionsClient.getFirstConnections(postCreatedEvent.getCreatorId());
        for (PersonDto connection: connections){
            sendNotification.send(connection.getUserId(), "Your connection "+postCreatedEvent.getCreatorId()+" has created post," +
                    " Check it out");
        }
    }

    @KafkaListener(topics = "post-liked-topic")
    public void handlePostLiked(PostLikedEvent postLikedEvent){
        log.info("Sending notifications: handlePostLiked: {}",postLikedEvent);
        String message = String.format("Your post, %d has been liked by %d"
                ,postLikedEvent.getPostId(),postLikedEvent.getLikedByUserId());
        sendNotification.send(postLikedEvent.getCreatorId(),message);
    }
}
