package com.a3m.studyassistant.backend.features.branch;

import com.a3m.studyassistant.backend.common.exceptions.UnauthorizedException;
import com.a3m.studyassistant.backend.features.topic.Topic;
import com.a3m.studyassistant.backend.features.topic.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BranchService {

    private final TopicService topicService;
    private final BranchRepository branchRepository;

    @Autowired
    public BranchService(TopicService topicService, BranchRepository branchRepository) {
        this.topicService = topicService;
        this.branchRepository = branchRepository;
    }

    public Branch getBranchById(UUID userId, UUID branchId) {
        Branch branch = branchRepository.findById(branchId).orElseThrow(() -> new BranchNotFoundException("Branch with id " + branchId + " couldn't be found!"));
        if(!branch.getTopic().getUser().getId().equals(userId)) throw new UnauthorizedException("You cannot retrieve branches to topics you do not own.");
        return branch;
    }

    public List<Branch> getBranchesListForTopic(UUID userId, UUID topicId) {
        Topic topic = topicService.getTopicById(userId, topicId);
        if(!topic.getUser().getId().equals(userId)) throw new UnauthorizedException("You do not have permission to view branches for this topic.");
        return branchRepository.findByTopicId(topicId);
    }

    @Transactional
    public Branch createBranch(UUID userId, UUID topicId, String title, String description) {
        Topic topic = topicService.getTopicById(userId, topicId);
        if(!topic.getUser().getId().equals(userId)) throw new UnauthorizedException("You cannot add branches to topics you do not own.");

        Branch branch = new Branch(title, description, topic);
        return branchRepository.save(branch);
    }

    @Transactional
    public void deleteBranch(UUID userId, UUID branchId) {
        Branch branch = branchRepository.findById(branchId).orElseThrow(() -> new BranchNotFoundException("Branch with id " + branchId + " couldn't be found!"));
        if(!branch.getTopic().getUser().getId().equals(userId)) throw new UnauthorizedException("You do not have permissions to delete this branch.");
        branchRepository.delete(branch);
    }

    @Transactional
    public Branch modifyBranch(UUID userId, UUID branchId, String title, String description) {
        Branch branch = branchRepository.findById(branchId).orElseThrow(() -> new BranchNotFoundException("Branch with id " + branchId + " couldn't be found!"));
        if(!branch.getTopic().getUser().getId().equals(userId)) throw new UnauthorizedException("You do not have permissions to modify this branch.");

        if(title != null) branch.setTitle(title);
        if(description != null) branch.setDescription(description);

        return branchRepository.save(branch);
    }

}
