package com.rengu.cosimulation.repository;

import com.rengu.cosimulation.entity.ProDesignLinkEntity;
import com.rengu.cosimulation.entity.ProDesignLinkFilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProDesignLinkFilesRepository extends JpaRepository<ProDesignLinkFilesEntity, String> {
    boolean existsByNameAndPostfixAndProDesignLinkEntity(String name, String extension, ProDesignLinkEntity proDesignLinkEntity);

    Optional<ProDesignLinkFilesEntity> findByNameAndPostfixAndProDesignLinkEntity(String name, String postfix, ProDesignLinkEntity proDesignLinkEntity);

    List<ProDesignLinkFilesEntity> findByProDesignLinkEntity(ProDesignLinkEntity proDesignLinkEntity);
}
