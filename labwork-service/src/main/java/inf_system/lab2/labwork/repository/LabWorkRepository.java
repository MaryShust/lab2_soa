package inf_system.lab2.labwork.repository;

import inf_system.lab2.labwork.model.LabWork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LabWorkRepository extends JpaRepository<LabWork, Long>,
        JpaSpecificationExecutor<LabWork> {
}
