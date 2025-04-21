package com.luv2code.linkedin.posts_service.service.impl;

import com.luv2code.linkedin.posts_service.auth.UserContextHolder;
import com.luv2code.linkedin.posts_service.clients.ConnectionsClient;
import com.luv2code.linkedin.posts_service.dto.PersonDto;
import com.luv2code.linkedin.posts_service.dto.PostCreateRequestDto;
import com.luv2code.linkedin.posts_service.dto.PostDto;
import com.luv2code.linkedin.posts_service.entity.Post;
import com.luv2code.linkedin.posts_service.events.PostCreatedEvent;
import com.luv2code.linkedin.posts_service.exception.ResourceNotFoundException;
import com.luv2code.linkedin.posts_service.repository.PostsRepository;
import com.luv2code.linkedin.posts_service.service.PostsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostsServiceImpl implements PostsService {

    private final PostsRepository postsRepository;
    private final ModelMapper modelMapper;
    private final ConnectionsClient connectionsClient;

    private final KafkaTemplate<Long, PostCreatedEvent> kafkaTemplate;

    @Override
    public PostDto createPost(PostCreateRequestDto postCreateRequestDto) {
        Long userId = UserContextHolder.getCurrentUserId();
        Post post = modelMapper.map(postCreateRequestDto, Post.class);
        post.setUserId(userId);

        Post savedPost = postsRepository.save(post);

        PostCreatedEvent postCreatedEvent = PostCreatedEvent.builder()
                .postId(savedPost.getId())
                .creatorId(userId)
                .content(savedPost.getContent())
                .build();

        kafkaTemplate.send("post-created-topic",postCreatedEvent);

        return modelMapper.map(savedPost,PostDto.class);
    }

    @Override
    public PostDto getPostById(Long postId) {
        log.debug("Retrieving post with ID: {}",postId);

        Post post = postsRepository.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found with id: "+postId));
        return modelMapper.map(post,PostDto.class);
    }

    @Override
    public List<PostDto> getAllPostsOfUser(Long userId) {
        List<Post> posts = postsRepository.findByUserId(userId);
        return posts
                .stream()
                .map((element) -> modelMapper.map(element, PostDto.class))
                .collect(Collectors.toList());
    }
}
