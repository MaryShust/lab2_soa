package inf_system.lab2.labwork.web;

import inf_system.lab2.labwork.dto.LabWorkDto;
import inf_system.lab2.labwork.exception.BadRequestException;
import inf_system.lab2.labwork.model.Difficulty;
import inf_system.lab2.labwork.service.LabWorkService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/labworks")
@Validated
public class LabWorkController {

    private final LabWorkService service;

    public LabWorkController(LabWorkService service) {
        this.service = service;
    }

    @GetMapping
    public List<LabWorkDto> getLabWorks(
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "filter", required = false) String filter) {
        return service.list(sort, filter, page, size);
    }

    @PostMapping
    public ResponseEntity<LabWorkDto> addLabWork(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "x") Double x,
            @RequestParam(name = "y") Double y,
            @RequestParam(name = "minimal-point") Double minimalPoint,
            @RequestParam(name = "personal-qualities-minimum") Double personalQualitiesMinimum,
            @RequestParam(name = "average-point") Double averagePoint,
            @RequestParam(name = "difficulty") Difficulty difficulty,
            @RequestParam(name = "author-name", required = false) String authorName,
            @RequestParam(name = "author-birthday", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate authorBirthday,
            @RequestParam(name = "author-height", required = false) Long authorHeight,
            @RequestParam(name = "author-weight", required = false) Double authorWeight,
            @RequestParam(name = "author-passport-id", required = false) String authorPassportID) {

        validateCreateRequest(name, x, y, minimalPoint, personalQualitiesMinimum, averagePoint,
                authorName, authorHeight, authorWeight, authorPassportID);

        LabWorkService.LabWorkCreateCommand command = new LabWorkService.LabWorkCreateCommand(
                name, x, y, minimalPoint, personalQualitiesMinimum, averagePoint, difficulty,
                authorName, authorBirthday, authorHeight, authorWeight, authorPassportID);

        LabWorkDto created = service.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public LabWorkDto updateLabWork(
            @PathVariable("id") Long id,
            @RequestParam(name = "name") String name,
            @RequestParam(name = "x") Double x,
            @RequestParam(name = "y") Double y,
            @RequestParam(name = "minimal-point") Double minimalPoint,
            @RequestParam(name = "personal-qualities-minimum") Double personalQualitiesMinimum,
            @RequestParam(name = "average-point") Double averagePoint,
            @RequestParam(name = "difficulty") Difficulty difficulty,
            @RequestParam(name = "author-name", required = false) String authorName,
            @RequestParam(name = "author-birthday", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate authorBirthday,
            @RequestParam(name = "author-height", required = false) Long authorHeight,
            @RequestParam(name = "author-weight", required = false) Double authorWeight,
            @RequestParam(name = "author-passport-id", required = false) String authorPassportID) {

        validateCreateRequest(name, x, y, minimalPoint, personalQualitiesMinimum, averagePoint,
                authorName, authorHeight, authorWeight, authorPassportID);

        LabWorkService.LabWorkCreateCommand command = new LabWorkService.LabWorkCreateCommand(
                name, x, y, minimalPoint, personalQualitiesMinimum, averagePoint, difficulty,
                authorName, authorBirthday, authorHeight, authorWeight, authorPassportID);

        return service.update(id, command);
    }

    @GetMapping("/{id}")
    public LabWorkDto getLabWorkById(@PathVariable("id") Long id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLabWork(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-max-person-min")
    public LabWorkDto getLabWorkWithMinimalAuthor() {
        return service.getWithMinimalAuthor();
    }

    @GetMapping("/by-difficulty")
    public Long countLabWorksByDifficulty(
            @RequestParam(name = "difficulty") Difficulty difficulty) {
        return service.countByDifficulty(difficulty);
    }

    @GetMapping("/by-minimal-point-greater-than")
    public List<LabWorkDto> getLabWorksWithMinimalPointGreaterThan(
            @RequestParam(name = "minimal-point") Double minimalPoint) {
        return service.getWithMinimalPointGreaterThan(minimalPoint);
    }

    private void validateCreateRequest(String name, Double x, Double y, Double minimalPoint,
                                       Double personalQualitiesMinimum, Double averagePoint,
                                       String authorName, Long authorHeight, Double authorWeight,
                                       String authorPassportID) {
        if (name == null || name.isEmpty()) {
            throw new BadRequestException("Данные нарушают ограничения целостности");
        }
        if (x == null || x > 678) {
            throw new BadRequestException("Данные нарушают ограничения целостности");
        }
        if (y == null || y <= -963) {
            throw new BadRequestException("Данные нарушают ограничения целостности");
        }
        if (minimalPoint == null || minimalPoint <= 0) {
            throw new BadRequestException("Данные нарушают ограничения целостности");
        }
        if (personalQualitiesMinimum == null || personalQualitiesMinimum <= 0) {
            throw new BadRequestException("Данные нарушают ограничения целостности");
        }
        if (averagePoint == null || averagePoint <= 0) {
            throw new BadRequestException("Данные нарушают ограничения целостности");
        }
        if (authorName != null && authorName.isEmpty()) {
            throw new BadRequestException("Данные нарушают ограничения целостности");
        }
        if (authorHeight != null && authorHeight <= 0) {
            throw new BadRequestException("Данные нарушают ограничения целостности");
        }
        if (authorWeight != null && authorWeight <= 0) {
            throw new BadRequestException("Данные нарушают ограничения целостности");
        }
        if (authorPassportID != null && authorPassportID.length() > 34) {
            throw new BadRequestException("Данные нарушают ограничения целостности");
        }
    }
}
