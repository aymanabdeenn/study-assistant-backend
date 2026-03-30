package com.a3m.studyassistant.backend.features.resource;

import com.a3m.studyassistant.backend.common.exceptions.UnauthorizedException;
import com.a3m.studyassistant.backend.features.branch.Branch;
import com.a3m.studyassistant.backend.features.branch.BranchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ResourceService {

    private final BranchService branchService;
    private final ResourceRepository resourceRepository;

    @Autowired
    public ResourceService(BranchService branchService, ResourceRepository resourceRepository) {
        this.branchService = branchService;
        this.resourceRepository = resourceRepository;
    }

    public Resource getResourceById(UUID userId, UUID resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException("Resource with id " + resourceId + " couldn't be found!"));
        if(!resource.getBranch().getTopic().getUser().getId().equals(userId)) throw new UnauthorizedException("You cannot retrieve resources you do not own.");
        return resource;
    }

    public List<Resource> getResourcesListForBranch(UUID userId, UUID branchId) {
        Branch branch = branchService.getBranchById(userId, branchId);
        return resourceRepository.findByBranchId(branchId);
    }

    @Transactional
    public Resource createResource(UUID userId, UUID branchId, String title, String url, String type, Float size) {
        Branch branch = branchService.getBranchById(userId, branchId);
        Resource resource = new Resource(title, url, type, size, branch);
        branch.addResource(resource);
        return resourceRepository.save(resource);
    }

    @Transactional
    public void deleteResource(UUID userId, UUID resourceId) {
        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException("Resource with id " + resourceId + " couldn't be found!"));
        if(!resource.getBranch().getTopic().getUser().getId().equals(userId)) throw new UnauthorizedException("You do not have permissions to this resource.");

        resource.getBranch().removeResource(resource);
        resourceRepository.delete(resource);
    }

    @Transactional
    public Resource modifyResource(UUID userId, UUID resourceId, String title, String url, String type, Float size) {
        Resource resource = resourceRepository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException("Resource with id " + resourceId + " couldn't be found!"));
        if(!resource.getBranch().getTopic().getUser().getId().equals(userId)) throw new UnauthorizedException("You do not have permissions to modify this resource.");

        if(title != null) resource.setTitle(title);
        if(url != null) resource.setUrl(url);
        if(type != null) resource.setType(type);
        if(size != null) resource.setSize(size);

        return resourceRepository.save(resource);
    }

}
