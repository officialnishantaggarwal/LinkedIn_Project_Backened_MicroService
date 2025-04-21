package com.luv2code.linkedin.connections_service.service;
import com.luv2code.linkedin.connections_service.auth.UserContextHolder;
import com.luv2code.linkedin.connections_service.entity.Person;
import com.luv2code.linkedin.connections_service.event.AcceptConnectionRequestEvent;
import com.luv2code.linkedin.connections_service.event.SendConnectionRequestEvent;
import com.luv2code.linkedin.connections_service.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionsService {

    private final PersonRepository personRepository;
    private final KafkaTemplate<Long, SendConnectionRequestEvent> sendRequestKafkaTemplate;
    private final KafkaTemplate<Long, AcceptConnectionRequestEvent> acceptRequestKafkaTemplate;

    public List<Person> getFirstDegreeConnections(){
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Getting first degree connections for user with id: {}",userId);
        return personRepository.getFirstDegreeConnections(userId);
    }

    public Boolean sendConnectionRequest(Long receiverId) {
        Long senderId = UserContextHolder.getCurrentUserId();
        log.info("Trying to send connection request, sender: {}, receiver: {}",senderId,receiverId);

        if (senderId.equals(receiverId)){
            throw new RuntimeException("Both sender and receiver are the same");
        }

        boolean alreadySentRequest = personRepository.connectionRequestExists(senderId,receiverId);
        if(alreadySentRequest){
            throw new RuntimeException("Connection request already exists, cannot be sent again");
        }

        boolean alreadyConnected = personRepository.alreadyConnected(senderId,receiverId);
        if(alreadyConnected){
            throw new RuntimeException("Connection already exists, cannot add connection request");
        }
        log.info("Successfully sent connection request...");
        personRepository.addConnectionRequest(senderId,receiverId);

        SendConnectionRequestEvent sendConnectionRequestEvent = SendConnectionRequestEvent.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .build();

        sendRequestKafkaTemplate.send("send-connection-request-topic",sendConnectionRequestEvent);

        return true;
    }

    public Boolean acceptConnectionRequest(Long senderId) {
        Long receiverId = UserContextHolder.getCurrentUserId();
        log.info("Trying to Accept connection request, sender: {}, receiver: {}",senderId,receiverId);

        if (senderId.equals(receiverId)){
            throw new RuntimeException("Both sender and receiver are the same");
        }

        boolean connectionRequestExists = personRepository.connectionRequestExists(senderId,receiverId);
        if(!connectionRequestExists){
            throw new RuntimeException("No Connection request exists to accept");
        }

        boolean alreadyConnected = personRepository.alreadyConnected(senderId,receiverId);
        if(alreadyConnected){
            throw new RuntimeException("Connection already exists, cannot accept connection request");
        }

        personRepository.acceptConnectionRequest(senderId,receiverId);
        log.info("Successfully accepted the connection request, sender: {}, receiver: {}",senderId,receiverId);
        AcceptConnectionRequestEvent acceptConnectionRequestEvent = AcceptConnectionRequestEvent.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .build();

        acceptRequestKafkaTemplate.send("accept-connection-request-topic",acceptConnectionRequestEvent);

        return true;

    }

    public boolean rejectConnectionRequest(Long senderId) {
        Long receiverId = UserContextHolder.getCurrentUserId();

        boolean connectionRequestExists = personRepository.connectionRequestExists(senderId,receiverId);
        if(!connectionRequestExists){
            throw new RuntimeException("No Connection request exists to reject");
        }

        personRepository.rejectConnectionRequest(senderId,receiverId);

        return true;
    }
}
