package com.core.erp.service;

import com.core.erp.domain.PartTimerEntity;
import com.core.erp.domain.StoreEntity;
import com.core.erp.dto.PartTimerDTO;
import com.core.erp.dto.PartTimerSearchDTO;
import com.core.erp.repository.PartTimerRepository;
import com.core.erp.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartTimeService {

    private final PartTimerRepository partTimerRepository;
    private final StoreRepository storeRepository;

    private final String uploadDir = "/upload/parttimer/";

    // 역할 헬퍼 메서드
    private boolean isStore(String role) {
        return "STORE".equals(role);
    }

    private boolean isHQ(String role) {
        return role != null && role.startsWith("HQ");
    }

    private boolean isMaster(String role) {
        return "MASTER".equals(role);
    }

    public List<PartTimerDTO> searchPartTimers(String role, Integer storeId, PartTimerSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize());
        Page<PartTimerEntity> result;

        if (isHQ(role)) {
            result = partTimerRepository.searchHeadquarterSide(
                    searchDTO.getPartName(),
                    searchDTO.getPartStatus(),
                    storeId,
                    searchDTO.getPartTimerId(),
                    pageable
            );
        } else if (isStore(role) || isMaster(role)) {
            result = partTimerRepository.searchStoreSide(
                    storeId,
                    searchDTO.getPartName(),
                    searchDTO.getPartStatus(),
                    searchDTO.getPartTimerId(),
                    pageable
            );
        } else {
            throw new RuntimeException("권한이 없습니다.");
        }

        return result.map(PartTimerDTO::new).getContent();
    }

    public Page<PartTimerDTO> findAllPartTimers(String role, Integer storeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PartTimerEntity> result;
        log.info("role = {}, storeId = {}", role, storeId);

        if (isHQ(role)) {
            result = partTimerRepository.findAll(pageable);
        } else if (isStore(role) || isMaster(role)) {
            result = partTimerRepository.findByStoreStoreId(storeId, pageable);
        } else {
            throw new RuntimeException("권한이 없습니다.");
        }

        return result.map(PartTimerDTO::new);
    }

    public PartTimerDTO findPartTimerById(String role, Integer storeId, Integer partTimerId) {
        PartTimerEntity entity = partTimerRepository.findById(partTimerId)
                .orElseThrow(() -> new RuntimeException("해당 아르바이트를 찾을 수 없습니다."));

        if ((isStore(role) || isMaster(role)) &&
                !Objects.equals(entity.getStore().getStoreId(), storeId)) {
            throw new RuntimeException("본인 지점의 아르바이트만 조회할 수 있습니다.");
        }

        return new PartTimerDTO(entity);
    }

    @Transactional
    public void registerPartTimer(Integer storeId, PartTimerDTO partTimerDTO) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("지점을 찾을 수 없습니다."));

        PartTimerEntity entity = new PartTimerEntity(partTimerDTO, store);

        String uploadedPath = uploadFile(partTimerDTO.getFile());
        if (uploadedPath != null) {
            entity.setPartImg(uploadedPath);
        }

        partTimerRepository.save(entity);
    }

    @Transactional
    public void updatePartTimer(String role, Integer storeId, Integer partTimerId, PartTimerDTO partTimerDTO) {
        PartTimerEntity entity = partTimerRepository.findById(partTimerId)
                .orElseThrow(() -> new RuntimeException("해당 아르바이트를 찾을 수 없습니다."));

        if ((isStore(role) || isMaster(role)) &&
                !Objects.equals(entity.getStore().getStoreId(), storeId)) {
            throw new RuntimeException("본인 지점의 아르바이트만 수정할 수 있습니다.");
        }

        entity.setPartName(partTimerDTO.getPartName());
        entity.setPartGender(partTimerDTO.getPartGender());
        entity.setPartPhone(partTimerDTO.getPartPhone());
        entity.setPartAddress(partTimerDTO.getPartAddress());
        entity.setResignDate(partTimerDTO.getResignDate());
        entity.setSalaryType(partTimerDTO.getSalaryType());
        entity.setHourlyWage(partTimerDTO.getHourlyWage());
        entity.setAccountBank(partTimerDTO.getAccountBank());
        entity.setAccountNumber(partTimerDTO.getAccountNumber());
        entity.setPartStatus(partTimerDTO.getPartStatus());
        entity.setPosition(partTimerDTO.getPosition());
        entity.setWorkType(partTimerDTO.getWorkType());

        if (partTimerDTO.getFile() != null && !partTimerDTO.getFile().isEmpty()) {
            String uploadedPath = uploadFile(partTimerDTO.getFile());
            entity.setPartImg(uploadedPath);
        }
    }

    @Transactional
    public void deletePartTimer(String role, Integer storeId, Integer partTimerId) {
        PartTimerEntity entity = partTimerRepository.findById(partTimerId)
                .orElseThrow(() -> new RuntimeException("해당 아르바이트를 찾을 수 없습니다."));

        if ((isStore(role) || isMaster(role)) &&
                !Objects.equals(entity.getStore().getStoreId(), storeId)) {
            throw new RuntimeException("본인 지점의 아르바이트만 삭제할 수 있습니다.");
        }

        partTimerRepository.delete(entity);
    }

    private String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            File uploadFolder = new File(uploadDir);
            if (!uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedFilename = UUID.randomUUID().toString() + ext;
            file.transferTo(new File(uploadDir + savedFilename));

            return "/upload/parttimer/" + savedFilename;
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }
}
