package inf_system.lab2.labwork.service;

import inf_system.lab2.labwork.dto.CoordinatesDto;
import inf_system.lab2.labwork.dto.LabWorkDto;
import inf_system.lab2.labwork.dto.PersonDto;
import inf_system.lab2.labwork.exception.BadRequestException;
import inf_system.lab2.labwork.exception.NotFoundException;
import inf_system.lab2.labwork.model.Coordinates;
import inf_system.lab2.labwork.model.Difficulty;
import inf_system.lab2.labwork.model.LabWork;
import inf_system.lab2.labwork.model.Person;
import inf_system.lab2.labwork.query.CriteriaBuilderSupport;
import inf_system.lab2.labwork.query.FilterCondition;
import inf_system.lab2.labwork.query.QueryParser;
import inf_system.lab2.labwork.query.SortRule;
import inf_system.lab2.labwork.repository.LabWorkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LabWorkService {

    private final LabWorkRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    public LabWorkService(LabWorkRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<LabWorkDto> list(String sort, String filter, Integer page, Integer size) {
        int pageNumber = page == null ? 1 : page;
        int pageSize = size == null ? 50 : size;
        if (pageNumber < 1) {
            throw new BadRequestException("Параметр page должен быть больше или равен 1");
        }
        if (pageSize < 1 || pageSize > 1000) {
            throw new BadRequestException("Параметр size должен быть в диапазоне [1, 1000]");
        }

        List<SortRule> sortRules = QueryParser.parseSort(sort);
        List<FilterCondition> conditions = QueryParser.parseFilter(filter);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LabWork> cq = cb.createQuery(LabWork.class);
        Root<LabWork> root = cq.from(LabWork.class);

        List<Predicate> predicates = CriteriaBuilderSupport.buildPredicates(cb, root, conditions);
        cq.where(predicates.toArray(new Predicate[0]));

        List<Order> orders = CriteriaBuilderSupport.buildOrders(cb, root, sortRules);
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        } else {
            cq.orderBy(cb.asc(root.get("id")));
        }

        TypedQuery<LabWork> query = entityManager.createQuery(cq);
        query.setFirstResult((pageNumber - 1) * pageSize);
        query.setMaxResults(pageSize);
        return query.getResultList().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public LabWorkDto getById(Long id) {
        validateId(id);
        LabWork entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Лабораторная работа не найдена"));
        return toDto(entity);
    }

    @Transactional
    public LabWorkDto create(LabWorkCreateCommand command) {
        LabWork entity = new LabWork();
        applyCommand(entity, command);
        entity.setCreationDate(ZonedDateTime.now());
        LabWork saved = repository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public LabWorkDto update(Long id, LabWorkCreateCommand command) {
        validateId(id);
        LabWork entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Лабораторная работа не найдена"));
        applyCommand(entity, command);
        LabWork saved = repository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        validateId(id);
        if (!repository.existsById(id)) {
            throw new NotFoundException("Лабораторная работа не найдена");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public LabWorkDto getWithMinimalAuthor() {
        List<LabWork> all = repository.findAll();
        LabWork best = null;
        for (LabWork lw : all) {
            if (lw.getAuthor() == null) {
                continue;
            }
            if (best == null || lw.getAuthor().compareTo(best.getAuthor()) < 0) {
                best = lw;
            }
        }
        if (best == null) {
            throw new NotFoundException("Коллекция пуста или автор отсутствует");
        }
        return toDto(best);
    }

    @Transactional(readOnly = true)
    public long countByDifficulty(Difficulty difficulty) {
        if (difficulty == null) {
            throw new BadRequestException("Некорректное значение difficulty");
        }
        return repository.findAll().stream()
                .filter(lw -> lw.getDifficulty() == difficulty)
                .count();
    }

    @Transactional(readOnly = true)
    public List<LabWorkDto> getWithMinimalPointGreaterThan(Double value) {
        if (value == null) {
            throw new BadRequestException("Заданное значение не является допустимым");
        }
        return repository.findAll().stream()
                .filter(lw -> lw.getMinimalPoint() != null && lw.getMinimalPoint() > value)
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<LabWork> findEntity(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public LabWork saveEntity(LabWork entity) {
        return repository.save(entity);
    }

    private void validateId(Long id) {
        if (id == null || id < 1) {
            throw new BadRequestException("Некорректный идентификатор");
        }
    }

    private void applyCommand(LabWork entity, LabWorkCreateCommand command) {
        entity.setName(command.name());
        entity.setCoordinates(new Coordinates(command.x(), command.y()));
        entity.setMinimalPoint(command.minimalPoint());
        entity.setPersonalQualitiesMinimum(command.personalQualitiesMinimum());
        entity.setAveragePoint(command.averagePoint());
        entity.setDifficulty(command.difficulty());
        if (command.hasAuthor()) {
            entity.setAuthor(new Person(command.authorName(), command.authorBirthday(),
                    command.authorHeight(), command.authorWeight(), command.authorPassportID()));
        } else {
            entity.setAuthor(null);
        }
    }

    private LabWorkDto toDto(LabWork entity) {
        LabWorkDto dto = new LabWorkDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (entity.getCoordinates() != null) {
            dto.setCoordinates(new CoordinatesDto(entity.getCoordinates().getX(), entity.getCoordinates().getY()));
        }
        dto.setCreationDate(entity.getCreationDate());
        dto.setMinimalPoint(entity.getMinimalPoint());
        dto.setPersonalQualitiesMinimum(entity.getPersonalQualitiesMinimum());
        dto.setAveragePoint(entity.getAveragePoint());
        dto.setDifficulty(entity.getDifficulty());
        if (entity.getAuthor() != null) {
            Person author = entity.getAuthor();
            dto.setAuthor(new PersonDto(author.getName(), author.getBirthday(), author.getHeight(),
                    author.getWeight(), author.getPassportID()));
        }
        return dto;
    }

    public record LabWorkCreateCommand(
            String name,
            Double x,
            Double y,
            Double minimalPoint,
            Double personalQualitiesMinimum,
            Double averagePoint,
            Difficulty difficulty,
            String authorName,
            LocalDate authorBirthday,
            Long authorHeight,
            Double authorWeight,
            String authorPassportID
    ) {
        public boolean hasAuthor() {
            return authorName != null || authorBirthday != null || authorHeight != null
                    || authorWeight != null || authorPassportID != null;
        }
    }
}
