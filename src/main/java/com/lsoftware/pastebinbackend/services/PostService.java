package com.lsoftware.pastebinbackend.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lsoftware.pastebinbackend.entities.ExposureEntity;
import com.lsoftware.pastebinbackend.entities.PostEntity;
import com.lsoftware.pastebinbackend.entities.UserEntity;
import com.lsoftware.pastebinbackend.repository.ExposureRepository;
import com.lsoftware.pastebinbackend.repository.PostRepository;
import com.lsoftware.pastebinbackend.repository.UserRepository;
import com.lsoftware.pastebinbackend.shared.dto.PostCreationDto;
import com.lsoftware.pastebinbackend.shared.dto.PostDto;
import com.lsoftware.pastebinbackend.util.Exposures;



@Service
public class PostService implements PostServiceInterface {

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ExposureRepository exposureRepository;

    @Autowired
    ModelMapper mapper;

    @Override
    public PostDto createPost(PostCreationDto post) {

        UserEntity userEntity = userRepository.findByEmail(post.getUserEmail());
        ExposureEntity exposureEntity = exposureRepository.findById(post.getExposureId());

        PostEntity postEntity = new PostEntity();
        postEntity.setUser(userEntity);
        postEntity.setExposure(exposureEntity);
        postEntity.setTitle(post.getTitle());
        postEntity.setContent(post.getContent());
        postEntity.setPostId(UUID.randomUUID().toString());
        postEntity.setExpiresAt(new Date(System.currentTimeMillis() + (post.getExpirationTime() * 60000)));

        PostEntity createdPost = postRepository.save(postEntity);

        PostDto postToReturn = mapper.map(createdPost, PostDto.class);

        return postToReturn;
    }

    
    @Override
    public List<PostDto> getLastPosts() {

    	
        List<PostEntity> postEntities = postRepository.getLastPublicPosts(Exposures.PUBLIC,
                new Date(System.currentTimeMillis()));

        List<PostDto> postDtos = new ArrayList<>();

        for (PostEntity post : postEntities) {
            PostDto postDto = mapper.map(post, PostDto.class);
            postDtos.add(postDto);
        }

        return postDtos;
    }

    
    @Override
    public PostDto getPost(String postId) {

        PostEntity postEntity = postRepository.findByPostId(postId);
        PostDto postDto = mapper.map(postEntity, PostDto.class);
        return postDto;
    }

    
    @Override
    public void deletePost(String postId, long userId) {
        PostEntity postEntity = postRepository.findByPostId(postId);
        if (postEntity.getUser().getId() != userId)
            throw new RuntimeException("No se puede realizar esta accion");

        postRepository.delete(postEntity);

    }

   
    @Override
    public PostDto updatePost(String postId, long userId, PostCreationDto postUpdateDto) {
        PostEntity postEntity = postRepository.findByPostId(postId);
        if (postEntity.getUser().getId() != userId)
            throw new RuntimeException("No se puede realizar esta accion");

        ExposureEntity exposureEntity = exposureRepository.findById(postUpdateDto.getExposureId());

        postEntity.setExposure(exposureEntity);
        postEntity.setTitle(postUpdateDto.getTitle());
        postEntity.setContent(postUpdateDto.getContent());
        postEntity.setExpiresAt(new Date(System.currentTimeMillis() + (postUpdateDto.getExpirationTime() * 60000)));

        PostEntity updatedPost = postRepository.save(postEntity);

        PostDto postDto = mapper.map(updatedPost, PostDto.class);

        return postDto;

    }

}
