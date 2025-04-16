package com.luv2code.linkedin.posts_service.service;

import com.luv2code.linkedin.posts_service.dto.PostCreateRequestDto;
import com.luv2code.linkedin.posts_service.dto.PostDto;
import com.luv2code.linkedin.posts_service.entity.Post;
import com.luv2code.linkedin.posts_service.repository.PostsRepository;

import java.util.List;

public interface PostsService {

    PostDto createPost(PostCreateRequestDto postCreateRequestDto, Long userId);

    PostDto getPostById(Long postId);

    List<PostDto> getAllPostsOfUser(Long userId);
}
