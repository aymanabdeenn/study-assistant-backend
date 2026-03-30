package com.a3m.studyassistant.backend.features.topic;

import com.a3m.studyassistant.backend.common.exceptions.UnauthorizedException;
import com.a3m.studyassistant.backend.features.user.User;
import com.a3m.studyassistant.backend.features.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TopicService {

    private final UserService userService;
    private final TopicRepository topicRepository;

    @Autowired
    public TopicService(UserService userService, TopicRepository topicRepository) {
        this.userService = userService;
        this.topicRepository = topicRepository;
    }

    public Topic getTopicById(UUID userId, UUID topicId) {
        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new TopicNotFoundException("Topic with id " + topicId + " couldn't be found!"));
        if(!topic.getUser().getId().equals(userId)) throw new UnauthorizedException("You cannot retrieve topics you do not own.");
        return topic;
    }

    public List<Topic> getTopicsListForUser(UUID userId) {
        return topicRepository.findByUserId(userId);
    }

    @Transactional
    public Topic createTopic(UUID userId, String title, String description) {
        User user = userService.getUserById(userId);
        Topic topic = new Topic(title, description, user);
        user.addTopic(topic);
        topicRepository.save(topic);
        return topic;
    }

    @Transactional
    public void deleteTopic(UUID userId, UUID topicId) {
        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new TopicNotFoundException("Topic with id " + topicId + " couldn't be found!"));
        if(!topic.getUser().getId().equals(userId)) throw new UnauthorizedException("You do not have permission to delete this topic.");

        topic.getUser().removeTopic(topic);
        topicRepository.delete(topic);
    }

    @Transactional
    public Topic modifyTopic(UUID userId, UUID topicId, String title, String description) {
        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new TopicNotFoundException("Topic with id " + topicId + " couldn't be found!"));
        if(!topic.getUser().getId().equals(userId)) throw new UnauthorizedException("You do not have permission to modify this topic.");

        if(title != null) topic.setTitle(title);
        if(description != null) topic.setDescription(description);

        topicRepository.save(topic);
        return topic;
    }

}
